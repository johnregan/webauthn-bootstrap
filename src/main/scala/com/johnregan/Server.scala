package com.johnregan

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import spray.json._

import java.time.Instant
import java.util.Date
import scala.jdk.CollectionConverters.MapHasAsJava

/** { "rawId": "tV7pv6BXRkZqvgDj+kqBCqTkoN455toXuxurMhnZgtg=", "response": { "attestationObject":
  * "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVikSZYN5YgOjGh0NBcPZHZgW4/krrmihjLHmVzzuoMdl2NFAAAAAK3OAAI1vMYKZIsLJfHwVQMAILVe6b+gV0ZGar4A4/pKgQqk5KDeOebaF7sbqzIZ2YLYpQECAyYgASFYIOkZ4dzecqceyFTnGMWrd3Brd5vU0xDztVbryABp7DxWIlgg7w4eFNC4PPq3E5pHZoBJ3BCrZdsKCe2RgV2g8h0PG2s=",
  * "clientDataJSON":
  * "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiNmVkMzc1ZDEwZTI2NGJkOTc1MmZmNzE4Iiwib3JpZ2luIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ=="
  * }, "authenticatorAttachment": "platform", "id": "tV7pv6BXRkZqvgDj-kqBCqTkoN455toXuxurMhnZgtg",
  * "type": "public-key" }
  *
  * @param name
  * @param email
  */

case class Response(attestationObject: String, clientDataJSON: String)
object Response extends DefaultJsonProtocol {
  implicit val response = jsonFormat2(Response.apply)

}
case class RegistrationData(
    rawId: String,
    response: Response,
    authenticatorAttachment: String,
    id: String,
    `type`: String,
    registerToken: String
)

object RegistrationData extends DefaultJsonProtocol {
  implicit val registration = jsonFormat6(RegistrationData.apply)
}

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val resItemFormat: RootJsonFormat[Response]         = jsonFormat2(Response.apply)
  implicit val regItemFormat: RootJsonFormat[RegistrationData] = jsonFormat6(RegistrationData.apply)

}

object Server extends App with JsonSupport {
  implicit val system           = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher
  var settings                  = CorsSettings.defaultSettings

  // Payload to be signed
  val initalPayload = Map(
    "userId"   -> "1234",
    "userName" -> "John Doe",
    "exp" -> (Date
      .from(Instant.now.plusSeconds(30))
      .getTime() / 1000)
  )

  // JWT Key
  val secretKey = "mySecretKey"

  val route: Route = cors(settings) {
    path("startRegistration") {
      get {

        // Sign the JWT using the secret key and HS256 algorithm
        val jwt = Jwts
          .builder()
          .setClaims(initalPayload.asJava)
          .signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8"))
          .compact()

        val input =
          s"""{ "registerToken" : "$jwt" }"""

        val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, input))

        println(s"startRegistration response: $input")
        complete(response)
      }
    } ~ path("endRegistration") {
      post {
        entity(as[RegistrationData]) { json =>
          Jwts
            .parser()
            .setSigningKey(secretKey.getBytes("UTF-8"))
            .parseClaimsJws(json.registerToken)

          println(json)
          // Do something with the registration data
          complete("Registration ended")
        }
      }
    }
  }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/")
}
