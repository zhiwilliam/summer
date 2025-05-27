package app.services.impls.auth

import cats.data.*
import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.AuthMiddleware
import pdi.jwt.*
import io.circe.parser.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.implicits.http4sHeaderSyntax

case class AuthedUser(email: String)

object JwtAuth {

  private val secretKey = "your_super_secret_key" // 与 Jwt.scala 中保持一致

  val authUser: Kleisli[[X] =>> OptionT[IO, X], Request[IO], AuthedUser] =
    Kleisli { req =>
      OptionT {
        IO {
          for {
            header <- req.headers.get[headers.Authorization]
            token = header.value.stripPrefix("Bearer ").trim
            claim <- JwtCirce.decode(token, secretKey, Seq(JwtAlgorithm.HS256)).toOption
            json <- parse(claim.content).toOption
            email <- json.hcursor.get[String]("email").toOption
          } yield AuthedUser(email)
        }
      }
    }


  val middleware: AuthMiddleware[IO, AuthedUser] = AuthMiddleware(authUser)
}