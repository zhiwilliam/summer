package app.model.impls
import app.NoteRepository
import app.model.Note
import cats.effect.*
import doobie.*
import doobie.implicits.*
//import doobie.implicits.javasql._ // 如果用 java.sql.*
import doobie.postgres.implicits.* // ✅ 你用 PostgreSQL 必须导入这个

import java.util.UUID
import java.time.Instant

class NoteRepositoryImpl[F[_]: Sync](xa: Transactor[F]) extends NoteRepository[F] {

  override def createNoteWithImages(
                                     userId: UUID,
                                     title: Option[String],
                                     content: String,
                                     images: List[String]
                                   ): F[Note] = {

    val now = Instant.now()
    val noteId = UUID.randomUUID()

    val insertNote: ConnectionIO[Int] =
      sql"""
        INSERT INTO notes (id, user_id, title, content, created_at)
        VALUES ($noteId, $userId, $title, $content, $now)
      """.update.run

    val insertImages: ConnectionIO[Int] =
      Update[(UUID, UUID, String, Int)](
        "INSERT INTO note_images (id, note_id, url, order_index) VALUES (?, ?, ?, ?)"
      ).updateMany(
        images.zipWithIndex.map { case (url, idx) =>
          (UUID.randomUUID(), noteId, url, idx)
        }
      )

    val tx =
      for {
        _ <- insertNote
        _ <- insertImages
      } yield Note(
        id = noteId,
        userId = userId,
        title = title,
        content = content,
        createdAt = now
      )

    tx.transact(xa)
  }
}
