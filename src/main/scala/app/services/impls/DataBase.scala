package app.services.impls

import cats.effect.*
import doobie.*
import doobie.hikari.*
import doobie.implicits.*
import com.typesafe.config.ConfigFactory

object DataBase {
  def transactor[F[_] : Async]: Resource[F, HikariTransactor[F]] = {
    val config = ConfigFactory.load().getConfig("db")

    val url = config.getString("url")
    val user = config.getString("user")
    val password = config.getString("password")

    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        url,
        user,
        password, // The password
        ce
      )
    } yield xa
  }
}
