package vickram.tech.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.engine.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
import vickram.tech.controllers.blacklistedTokens
import vickram.tech.controllers.getUser
import java.util.*

fun Application.configureSecurity(
    payload: Payload
) {
    lateinit var call: ApplicationCall
    authentication {
        jwt {
            realm = payload.realm ?: ""
            verifier(
                JWT
                    .require(Algorithm.HMAC256(payload.secret))
                    .withAudience(payload.audience)
                    .withIssuer(payload.issuer)
                    .build()
            )
            validate { credential ->
                val userId = UUID.fromString(credential.payload.subject)
                val user = getUser(userId)
                val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                if (user != null && !blacklistedTokens.contains(token)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

data class Payload(
    val issuer: String,
    val audience: String,
    val secret: String,
    val realm: String?,
    val expiresAt: Long = System.currentTimeMillis()
)

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)

fun makeJwt(
    payload: Payload,
    email: String,
    userId: String,
): TokenPair {
    val jti = UUID.randomUUID().toString()
    return TokenPair(
        JWT.create()
            .withIssuer(payload.issuer)
            .withClaim("email", email)
            .withAudience(payload.audience)
            .withSubject(userId)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(Date(payload.expiresAt + (1000 * 60 * 15)))
            .withJWTId(jti)
            .sign(Algorithm.HMAC256(payload.secret)),

        JWT.create()
            .withIssuer(payload.issuer)
            .withClaim("email", email)
            .withAudience(payload.audience)
            .withSubject(userId)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(Date(payload.expiresAt + (1000 * 60 * 60 * 24 * 7)))
            .withJWTId(jti)
            .sign(Algorithm.HMAC256(payload.secret))
    )
}
