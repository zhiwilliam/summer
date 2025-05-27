package app.route

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import app.services.impls.auth.{AuthService, Jwt}
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

object UserRoutes {

  case class RegisterReq(email: String, password: String)
  case class LoginReq(email: String, password: String)

  def routes(auth: AuthService): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "register" =>
      for {
        body <- req.as[RegisterReq]
        result <- auth.register(body.email, body.password)
        resp <- result match {
          case Right(user) => Ok(Map("message" -> "注册成功").asJson)
          case Left(err)   => BadRequest(Map("error" -> err).asJson)
        }
      } yield resp

    case req @ POST -> Root / "login" =>
      for {
        body <- req.as[LoginReq]
        result <- auth.login(body.email, body.password)
        resp <- result match {
          case Right(user) =>
            val token = Jwt.generate(user.email)
            Ok(Map("token" -> token).asJson)
          case Left(err) => NotFound(Map("error" -> err).asJson)
            //Unauthorized(Map("error" -> err).asJson)
        }
      } yield resp
  }
}
