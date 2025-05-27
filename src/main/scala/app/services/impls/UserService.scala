package app.services.impls

import app.services.Service.UserService
import cats.effect.{Resource, Sync}
import cats.mtl.Ask
import framework.Module

object UserService {
  def makeResource[F[_]: Sync](using c: Ask[F, Config]): Resource[F, UserService[F]] =
    for {
      config <- Resource.eval(Ask[F, Config].ask[Config])
      service <- Resource.pure(new UserService[F] {
        def doSomething: F[Unit] = Sync[F].delay(println(s"User did something from ${config.appName}"))
      })
    } yield service
}