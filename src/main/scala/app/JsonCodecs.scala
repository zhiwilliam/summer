// app/JsonCodecs.scala
package app

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.circe.*

import cats.effect.Concurrent

import app.model.*

object JsonCodecs {

  // ----- 编解码器定义 -----

  implicit val createNoteRequestDecoder: Decoder[CreateNoteRequest] = deriveDecoder
  implicit val createNoteRequestEncoder: Encoder[CreateNoteRequest] = deriveEncoder

  implicit val createNoteResponseDecoder: Decoder[CreateNoteResponse] = deriveDecoder
  implicit val createNoteResponseEncoder: Encoder[CreateNoteResponse] = deriveEncoder

  // ----- Http4s EntityDecoder/EntityEncoder -----

  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, CreateNoteRequest] =
    jsonOf[F, CreateNoteRequest]

  implicit def entityEncoder[F[_]]: EntityEncoder[F, CreateNoteResponse] =
    jsonEncoderOf[F, CreateNoteResponse]
}
