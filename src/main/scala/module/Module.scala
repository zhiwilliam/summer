package module

import cats.effect.Resource

trait Module[F[_], A] {
  def make: Resource[F, A]
}