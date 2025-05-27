package app.route

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import io.circe.syntax.*
import app.services.impls.auth.AuthedUser
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

object ProtectedRoutes {

  def routes: AuthedRoutes[AuthedUser, IO] = AuthedRoutes.of {
    case GET -> Root / "me" as user =>
      Ok(Map("email" -> user.email).asJson)
  }
}
