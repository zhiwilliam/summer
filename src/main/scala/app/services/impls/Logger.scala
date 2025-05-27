package app.services.impls

import app.services.Service.Logger
import cats.effect.{Resource, Sync}
import framework.Module

object Logger {
  def makeResource[F[_] : Sync]: Resource[F, Logger[F]] =
    Resource.pure(new Logger[F] {
      def info(msg: String): F[Unit] = Sync[F].delay(println(s"[info] $msg"))
    })
}