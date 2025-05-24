package mtl

import module.Logging

trait MonadLogging[F[_]] {
  def info(msg: String): F[Unit]
  def error(msg: String): F[Unit]
}

object MonadLogging {
  def apply[F[_]](implicit L: MonadLogging[F]): MonadLogging[F] = L

  implicit def fromLogging[F[_]](implicit L: Logging[F]): MonadLogging[F] =
    new MonadLogging[F] {
      def info(msg: String): F[Unit] = L.info(msg)
      def error(msg: String): F[Unit] = L.error(msg)
    }
}