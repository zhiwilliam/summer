package core

import cats.data.ReaderT
import cats.data.EitherT
import cats.Monad
import cats.effect.Sync

// Eff is like ZIO[R, E, A]
object Eff {
  type Eff[F[_], R, E, A] = ReaderT[EitherT[F, E, *], R, A]

  def liftF[F[_]: Monad, R, E, A](fa: F[Either[E, A]]): Eff[F, R, E, A] =
    ReaderT.liftF(EitherT(fa))

  def succeed[F[_]: Monad, R, E, A](a: A): Eff[F, R, E, A] =
    ReaderT.liftF(EitherT.rightT[F, E](a))

  def fail[F[_]: Monad, R, E, A](e: E): Eff[F, R, E, A] =
    ReaderT.liftF(EitherT.leftT[F, A](e))

  def access[F[_]: Monad, R, E]: Eff[F, R, E, R] =
    ReaderT.ask[EitherT[F, E, *], R]
}