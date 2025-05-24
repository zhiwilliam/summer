package app

import module._
import mtl._
import cats._
import cats.implicits._
import cats.effect.IO
import cats.effect._
import core.Make

object Main extends IOApp {
  case class AppEnv[F[_]](
                           logging: Logging[F],
                           database: Database[F]
                         )

  implicit def makeAppEnv[F[_]: Sync]: Make[F, AppEnv[F]] =
    Make.make2[F, Logging[F], Database[F], AppEnv[F]](AppEnv(_, _))

  def program[F[_]: Monad: MonadLogging: MonadDatabase]: F[Unit] = for {
    _ <- MonadLogging[F].info("Starting the program...")
    res <- MonadDatabase[F].query("SELECT * FROM users")
    _ <- MonadLogging[F].info(s"Got: $res")
  } yield ()

  def run(args: List[String]): IO[ExitCode] = for {
    env <- Make[IO, AppEnv[IO]].make
    implicit0(logging: Logging[IO])  = env.logging
    implicit0(database: Database[IO]) = env.database
    _   <- program[IO]
  } yield ExitCode.Success

}
