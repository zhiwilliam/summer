import app.route.ProtectedRoutes
import cats.Applicative
import cats.mtl.{Ask, Local}
import cats.mtl.syntax.local.*
import cats.effect.*
import cats.syntax.all.*
import framework.*

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror
import app.services.Service.*
import app.services.impls.Config
import app.services.impls.auth.JwtAuth
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.implicits.*

final case class AppEnv(
                         logger: Logger[IO],
                         userService: UserService[IO]
                       )

given AutoLayer[IO, AppEnv] = AutoLayer.derived

object Main extends IOApp.Simple {
  val appLayer: Layer[IO, AppEnv] = AutoLayer[IO, AppEnv]

  val prog = appLayer.resource.use { env =>
    for {
      config <- Config.load[IO]
      _ <- env.logger.info(s"App name: ${config.appName}") //.using(config)
      _ <- env.userService.doSomething
    } yield {
    }
  }

  import app.route.UserRoutes
  import app.services.impls.DataBase
  import app.services.impls.auth.AuthService

  def httpApp = DataBase.transactor[IO].use { xa =>
    val authSvc = new AuthService(xa)

    val publicRoutes = UserRoutes.routes(authSvc)
    val authedRoutes = JwtAuth.middleware(ProtectedRoutes.routes)

    val allRoutes = Router(
      "/api" -> (publicRoutes <+> authedRoutes)
    ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .build
      .use(_ => IO.println("✅ 服务已启动 http://localhost:8080") *> IO.never)
      
  }

  override def run: IO[Unit] = httpApp
}