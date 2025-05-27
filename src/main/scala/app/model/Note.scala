package app.model

import java.time.Instant
import java.util.UUID

final case class CreateNoteRequest(
                                    title: Option[String],
                                    content: String,
                                    images: List[String] // 图片 URL
                                  )

final case class CreateNoteResponse(
                                     id: UUID,
                                     createdAt: Instant
                                   )
final case class Note(
                       id: UUID,
                       userId: UUID,
                       title: Option[String],
                       content: String,
                       createdAt: Instant
                     )

final case class NoteImage(
                            id: UUID,
                            noteId: UUID,
                            url: String,
                            order: Int
                          )
