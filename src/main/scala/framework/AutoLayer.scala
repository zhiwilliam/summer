package framework

import cats.effect.*
import cats.Applicative
import cats.mtl.{Ask, Local}
import cats.mtl.syntax.local.*
import cats.effect.*
import cats.syntax.all.*

import scala.compiletime.*
import scala.deriving.Mirror

trait ZLayerLike[F[_], A] {
  def layer: Layer[F, A]
}

trait AutoLayer[F[_], A] {
  def make: Layer[F, A]
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
    given fromModule[F[_], A](using m: Module[F, A]): Single[F, A] with
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
