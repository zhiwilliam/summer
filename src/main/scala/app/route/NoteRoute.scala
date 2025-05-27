package app.route


import app.NoteRepository
import app.model.{CreateNoteRequest, CreateNoteResponse}
import cats.implicits.*

import java.util.UUID
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import app.services.impls.auth.{AuthService, Jwt}
import cats.effect.Concurrent
import org.http4s.circe.CirceEntityCodec.{circeEntityEncoder, circeEntityDecoder}
import org.http4s.dsl.Http4sDsl


class NoteRoutes[F[_]: Concurrent](noteRepo: NoteRepository[F]) {

  val dsl = Http4sDsl[F]
  import dsl.*

  def authedRoutes: AuthedRoutes[UUID, F] = AuthedRoutes.of {
    case req @ POST -> Root / "api" / "notes" as userId =>
      for {
        body <- req.req.as[CreateNoteRequest]
        note <- noteRepo.createNoteWithImages(
          userId = userId,
          title = body.title,
          content = body.content,
          images = body.images
        )
        res <- Created(CreateNoteResponse(note.id, note.createdAt))
      } yield res
  }
}
