package module

import cats.effect.Sync

trait Database[F[_]] {
  def query(sql: String): F[String]
}

object Database {
  def apply[F[_]](implicit D: Database[F]): Database[F] = D

  class DummyDatabase[F[_]: Sync] extends Database[F] {
    def query(sql: String): F[String] =
      Sync[F].delay(s"Fake result for SQL: SELECT * FROM users")
  }

  implicit def makeDatabase[F[_]: Sync]: core.Make[F, Database[F]] =
    new core.Make[F, Database[F]] {
      def make: F[Database[F]] = Sync[F].delay(new DummyDatabase[F])
    }
}