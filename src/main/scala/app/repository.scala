package app

import app.model.Note

import java.util.UUID

trait NoteRepository[F[_]] {
  def createNoteWithImages(
                            userId: UUID,
                            title: Option[String],
                            content: String,
                            images: List[String]
                          ): F[Note]
}