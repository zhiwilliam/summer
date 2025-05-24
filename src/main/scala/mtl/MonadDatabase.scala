package mtl

import module.Database

trait MonadDatabase[F[_]] {
  def query(sql: String): F[String]
}

object MonadDatabase {
  def apply[F[_]](implicit D: MonadDatabase[F]): MonadDatabase[F] = D

  implicit def fromDatabase[F[_]](implicit D: Database[F]): MonadDatabase[F] =
    new MonadDatabase[F] {
      def query(sql: String): F[String] = D.query(sql)
    }
}