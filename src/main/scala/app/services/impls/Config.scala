package app.services.impls

import app.services.Service.Logger
import cats.effect.{Resource, Sync}
import framework.Module
import cats.Applicative
import cats.mtl.{Ask, Local}
import cats.mtl.syntax.local.*
import cats.effect.*
import cats.syntax.all.*
import framework.*

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

case class Config(appName: String)

object Config {
  def load[F[_] : Sync]: F[Config] = Sync[F].pure(Config("MyApp"))

  given localConfig[F[_] : Sync]: Local[F, Config] with
    def applicative: Applicative[F] = Applicative[F]

    def ask[E2 >: Config]: F[E2] = Config.load[F].widen[E2]

    def local[A](fa: F[A])(f: Config => Config): F[A] = fa // 因为环境不可变

  given askConfig[F[_] : Sync]: Ask[F, Config] with
    def applicative: Applicative[F] = Applicative[F]

    def ask[E2 >: Config]: F[E2] = Config.load[F].widen[E2]
}
