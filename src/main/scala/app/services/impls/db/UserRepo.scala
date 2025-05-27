package app.services.impls.db

import doobie.*
import doobie.implicits.*
import cats.effect.*
import app.model.User
import doobie.implicits.javatimedrivernative.JavaInstantMeta

object UserRepo {

  def findByEmail(email: String): ConnectionIO[Option[User]] =
    sql"""SELECT id, email, password, created_at FROM users WHERE email = $email"""
      .query[User]
      .option

  def create(email: String, passwordHash: String): ConnectionIO[User] =
    sql"""INSERT INTO users (email, password) VALUES ($email, $passwordHash)
          RETURNING id, email, password, created_at"""
      .query[User]
      .unique
}
