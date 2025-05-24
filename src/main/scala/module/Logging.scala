package module

import cats.effect.Sync

trait Logging[F[_]] {
  def info(msg: String): F[Unit]
  def error(msg: String): F[Unit]
}

object Logging {
  def apply[F[_]](implicit L: Logging[F]): Logging[F] = L

  class ConsoleLogging[F[_]: Sync] extends Logging[F] {
    def info(msg: String): F[Unit]  = Sync[F].delay(println(s"[info] $msg"))
    def error(msg: String): F[Unit] = Sync[F].delay(println(s"[error] $msg"))
  }

  implicit def makeLogging[F[_]: Sync]: core.Make[F, Logging[F]] =
    new core.Make[F, Logging[F]] {
      def make: F[Logging[F]] = Sync[F].delay(new ConsoleLogging[F])
    }
}