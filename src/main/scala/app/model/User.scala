package app.model

import java.time.Instant

case class User(id: Long, email: String, password: String, createdAt: Instant)
