package app

import cats.mtl.{Ask, Local}
import cats.syntax.all._
import cats.Applicative
import cats.effect._
import cats.effect.kernel.MonadCancelThrow

// Core type classes for the framework
trait Module[F[_]] {
  def provide[A](fa: F[A]): F[A]
}

trait HasContext[F[_], C] {
  def context: Ask[F, C]
}

// Logger abstraction
trait Logger[F[_]] {
  def info(msg: String): F[Unit]
  def error(msg: String): F[Unit]
}

object Logger {
  def apply[F[_]](implicit ev: Logger[F]): Logger[F] = ev
}

// Database abstraction
trait Database[F[_]] {
  def query(sql: String): F[List[String]]
}

object Database {
  def apply[F[_]](implicit ev: Database[F]): Database[F] = ev
}

// User service abstraction
trait UserService[F[_]] {
  def findUser(id: String): F[Option[String]]
}

object UserService {
  def apply[F[_]](implicit ev: UserService[F]): UserService[F] = ev
}

// Context for dependency injection
case class AppContext(config: String)

// Implementations
class LiveLogger[F[_]: MonadCancelThrow] extends Logger[F] {
  def info(msg: String): F[Unit] =
    MonadCancelThrow[F].pure(println(s"INFO: $msg"))
  def error(msg: String): F[Unit] =
    MonadCancelThrow[F].pure(println(s"ERROR: $msg"))
}

class LiveDatabase[F[_]: MonadCancelThrow] extends Database[F] {
  def query(sql: String): F[List[String]] =
    MonadCancelThrow[F].pure(List(s"Result for $sql"))
}

class LiveUserService[F[_]](implicit
                            F: MonadCancelThrow[F],
                            logger: Logger[F],
                            database: Database[F],
                            ask: Ask[F, AppContext]
                           ) extends UserService[F] {
  def findUser(id: String): F[Option[String]] =
    for {
      ctx <- ask.ask
      _ <- logger.info(s"Fetching user $id with config ${ctx.config}")
      result <- database.query(s"SELECT * FROM users WHERE id = $id")
      _ <- logger.info(s"Query result: $result")
    } yield result.headOption
}

// Module composition
class AppModule[F[_]: Sync: MonadCancelThrow](ctx: AppContext) extends Module[F] {
  require(ctx != null, "AppContext cannot be null")

  implicit val logger: Logger[F] = new LiveLogger[F]
  implicit val database: Database[F] = new LiveDatabase[F]

  implicit val askContext: Ask[F, AppContext] = new Ask[F, AppContext] {
    def ask[E2 >: AppContext]: F[E2] = MonadCancelThrow[F].pure(ctx)
    def local[A](fa: F[A])(f: AppContext => AppContext): F[A] = fa
    override def applicative: Applicative[F] = MonadCancelThrow[F]
  }

  implicit val userService: UserService[F] = new LiveUserService[F]

  def provide[A](fa: F[A]): F[A] = fa
}

// Program using the framework
object GrokMain extends IOApp {
  def program[F[_]: MonadCancelThrow: UserService: Logger]: F[Unit] = for {
    user <- UserService[F].findUser("123")
    _ <- Logger[F].info(s"Found user: $user")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    val ctx = AppContext("production")
    val module = new AppModule[IO](ctx)
    import module._
    Logger[IO].info(s"Module created with ctx: $ctx").flatMap { _ =>
      program[IO]
        .guarantee(Logger[IO].info("Program completed"))
        .as(ExitCode.Success)
        .handleErrorWith(e => Logger[IO].error(s"Error: $e").as(ExitCode.Error))
    }
  }
}