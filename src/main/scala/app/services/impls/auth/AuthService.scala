package app.services.impls.auth

import cats.effect.*
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import app.services.impls.db.UserRepo
import app.model.User
import org.mindrot.jbcrypt.BCrypt

class AuthService(xa: HikariTransactor[IO]) {

  def register(email: String, password: String): IO[Either[String, User]] = {
    val hash = BCrypt.hashpw(password, BCrypt.gensalt())
    UserRepo.findByEmail(email).transact(xa).flatMap {
      case Some(_) => IO.pure(Left("邮箱已注册"))
      case None =>
        UserRepo.create(email, hash).transact(xa).map(Right(_))
    }
  }

  def login(email: String, password: String): IO[Either[String, User]] =
    UserRepo.findByEmail(email).transact(xa).map {
      case Some(user) if BCrypt.checkpw(password, user.password) =>
        Right(user)
      case _ => Left("账号或密码错误")
    }
}
