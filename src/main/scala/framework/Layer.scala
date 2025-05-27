package framework

import cats.Applicative
import cats.effect.{Resource, Sync}

final case class Layer[F[_], A](resource: Resource[F, A]) {
  def map[B](f: A => B): Layer[F, B] = Layer(resource.map(f))

  def flatMap[B](f: A => Layer[F, B]): Layer[F, B] =
    Layer(resource.flatMap(a => f(a).resource))
}

object Layer {
  def pure[F[_]: Applicative, A](a: A): Layer[F, A] =
    Layer(Resource.pure(a))

  def eval[F[_]: Sync, A](fa: F[A]): Layer[F, A] =
    Layer(Resource.eval(fa))
}