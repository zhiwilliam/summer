import cats.Applicative
import cats.mtl.{Ask, Local}
import cats.mtl.syntax.local.*
import cats.effect.*
import cats.syntax.all.*

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

// 假设的类型定义
case class Config(appName: String)

trait Logger[F[_]] {
  def info(msg: String): F[Unit]
}

trait UserService[F[_]] {
  def doSomething: F[Unit]
}

final case class Layer[F[_], A](resource: Resource[F, A]) {
  def map[B](f: A => B): Layer[F, B] = Layer(resource.map(f))
  def flatMap[B](f: A => Layer[F, B]): Layer[F, B] =
    Layer(resource.flatMap(a => f(a).resource))
}

trait ZLayerLike[F[_], A] {
  def layer: Layer[F, A]
}

trait AutoLayer[F[_], A] {
  def make: Layer[F, A]
}
import macros.Module
object Logger {
  @macros.Module
  def makeResource[F[_] : Sync]: Resource[F, Logger[F]] =
    Resource.pure(new Logger[F] {
      def info(msg: String): F[Unit] = Sync[F].delay(println(s"[info] $msg"))
    })

  //  given loggerModule[F[_]: Sync]: module.Module[F, Logger[F]] with
  //    def make: Resource[F, Logger[F]] = makeResource[F]
}

object Config {
  def load[F[_]: Sync]: F[Config] = Sync[F].pure(Config("MyApp"))

  given localConfig[F[_] : Sync]: Local[F, Config] with
    def applicative: Applicative[F] = Applicative[F]

    def ask[E2 >: Config]: F[E2] = Config.load[F].widen[E2]

    def local[A](fa: F[A])(f: Config => Config): F[A] = fa // 因为环境不可变

  given askConfig[F[_]: Sync]: Ask[F, Config] with
    def applicative: Applicative[F] = Applicative[F]
    def ask[E2 >: Config]: F[E2] = load[F].widen[E2]
}

object UserService {
  def makeResource[F[_]: Sync](using c: Ask[F, Config]): Resource[F, UserService[F]] =
    for {
      config <- Resource.eval(Ask[F, Config].ask[Config])
      service <- Resource.pure(new UserService[F] {
        def doSomething: F[Unit] = Sync[F].delay(println(s"User did something from ${config.appName}"))
      })
    } yield service

  given userServiceModule[F[_]: Sync](using c: Ask[F, Config]): module.Module[F, UserService[F]] with
    def make: Resource[F, UserService[F]] = makeResource[F]
}

object Layer {
  def pure[F[_]: Applicative, A](a: A): Layer[F, A] =
    Layer(Resource.pure(a))

  def eval[F[_]: Sync, A](fa: F[A]): Layer[F, A] =
    Layer(Resource.eval(fa))
}

object AutoLayer {

  def apply[F[_], A](using auto: AutoLayer[F, A]): Layer[F, A] = auto.make

  // 顶层派生，优先单例，后产品组合
  inline given derived[F[_], A](using n: MonadCancel[F, Throwable]): AutoLayer[F, A] =
    summonFrom {
      case single: AutoLayer.Single[F, A] => single
      case product: AutoLayer.Product[F, A] => product
      case _ =>
        compiletime.error("Cannot derive AutoLayer for the given type")
    }

  // ---------- 单个类型派生模块 ----------
  trait Single[F[_], A] extends AutoLayer[F, A]

  object Single {

    // 自动从 Module 派生
    given fromModule[F[_], A](using m: module.Module[F, A]): Single[F, A] with
      def make: Layer[F, A] = Layer(m.make)

    // 自动从 ZLayerLike 派生
    given fromZLayerLike[F[_], A](using z: ZLayerLike[F, A]): Single[F, A] with
      def make: Layer[F, A] = z.layer

    // 这里可以继续加其他单例派生的 given 实例
  }

  // ---------- 产品类型组合派生模块 ----------
  trait Product[F[_], A] extends AutoLayer[F, A]

  object Product {

    inline given derived[F[_], A](using
                                  n: MonadCancel[F, Throwable],
                                  m: Mirror.ProductOf[A]
                                 ): Product[F, A] =
      new Product[F, A] {
        def make: Layer[F, A] =
          makeFromTuple[F, m.MirroredElemTypes].map(m.fromProduct)
      }

    private inline def makeFromTuple[F[_], Elems <: Tuple](using MonadCancel[F, Throwable]): Layer[F, Tuple] =
      reduceLayers(summonAllLayers[F, Elems])

    private inline def summonAllLayers[F[_], T <: Tuple](using mc: MonadCancel[F, Throwable]): List[Layer[F, ?]] =
      inline erasedValue[T] match {
        case _: EmptyTuple => Nil
        case _: (h *: t) =>
          val headLayer: Layer[F, h] = summonZLayerOrAuto[F, h]
          headLayer.asInstanceOf[Layer[F, ?]] :: summonAllLayers[F, t]
      }

    private inline def summonZLayerOrAuto[F[_], A](using mc: MonadCancel[F, Throwable]): Layer[F, A] =
      summonInline[AutoLayer[F, A]].make

    private def reduceLayers[F[_]](layers: List[Layer[F, ?]])(using MonadCancel[F, Throwable]): Layer[F, Tuple] =
      layers match
        case Nil => Layer(Resource.pure(EmptyTuple.asInstanceOf[Tuple]))
        case head :: tail =>
          tail.foldLeft(head.map(Tuple1(_))) { (acc, next) =>
            acc.flatMap(t1 => next.map(t2 => t1 ++ Tuple1(t2)))
          }
  }

}


final case class AppEnv(
                         logger: Logger[IO],
                         userService: UserService[IO]
                       )

given AutoLayer[IO, AppEnv] = AutoLayer.derived

object Main extends IOApp.Simple {
  val appLayer: Layer[IO, AppEnv] = AutoLayer[IO, AppEnv]

  override def run: IO[Unit] =
    appLayer.resource.use { env =>
      for {
        config <- Config.load[IO]
        _ <- env.logger.info(s"App name: ${config.appName}")//.using(config)
        _ <- env.userService.doSomething
      } yield ()
    }
}