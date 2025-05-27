package app.services.impls.auth

import pdi.jwt.*
import java.time.Instant
import java.util.Date

object Jwt {
  val secretKey = "xmdkayelvm632549235&*^2$)"

  def generate(email: String): String = {
    val claim = JwtClaim(
      expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = s"""{"email":"$email"}"""
    )
    JwtCirce.encode(claim, secretKey, JwtAlgorithm.HS256)
  }
}
