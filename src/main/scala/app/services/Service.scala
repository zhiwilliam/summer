package app.services

import app.services.impls.UserService.makeResource
import app.services.impls.Config
import cats.Applicative
import cats.effect.*
import cats.mtl.{Ask, Local}
import framework.Module
import cats.syntax.all.*

object Service {
  trait Logger[F[_]] {
    def info(msg: String): F[Unit]
  }

  trait UserService[F[_]] {
    def doSomething: F[Unit]
  }

  given loggerModule[F[_] : Sync]: Module[F, Logger[F]] with
    def make: Resource[F, Logger[F]] = impls.Logger.makeResource[F]


  given userServiceModule[F[_]: Sync](using c: Ask[F, Config]): Module[F, UserService[F]] with
    def make: Resource[F, UserService[F]] = impls.UserService.makeResource[F]

  
}
