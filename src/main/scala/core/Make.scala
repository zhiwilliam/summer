package core

import cats._
import cats.implicits._

trait Make[F[_], A] {
  def make: F[A]
}

object Make {
  def apply[F[_], A](implicit M: Make[F, A]): Make[F, A] = M

  // 构造 Make 实例
  def instance[F[_]: FlatMap, A](fa: F[A]): Make[F, A] = new Make[F, A] {
    def make: F[A] = fa
  }

  // 自动组合两个模块为一个模块
  def make2[F[_]: FlatMap, A1, A2, R](f: (A1, A2) => R)(
    implicit M1: Make[F, A1], M2: Make[F, A2]
  ): Make[F, R] = new Make[F, R] {
    def make: F[R] = for {
      a1 <- M1.make
      a2 <- M2.make
    } yield f(a1, a2)
  }
}