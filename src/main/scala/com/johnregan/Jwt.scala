package com.johnregan
import io.jsonwebtoken.{Claims, JwtParser, Jwts, SignatureAlgorithm}

import scala.jdk.CollectionConverters.MapHasAsJava

object Jwt extends App {

  // Secret key to sign and validate the JWT
  val secretKey = "mySecretKey"

  // Payload to be signed
  val payload: Map[String, Object] = Map("userId" -> "1234", "userName" -> "John Doe")

  // Sign the JWT using the secret key and HS256 algorithm
  val jwt = Jwts
    .builder()
    .setClaims(payload.asJava)
    .signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8"))
    .compact()

  // Print the signed JWT
  println(s"Signed JWT: $jwt")

  // Validate the JWT
  val parsedJwt = Jwts
    .parser()
    .setSigningKey(secretKey.getBytes("UTF-8"))
    .parseClaimsJws(jwt)

  // Extract the claims from the validated JWT
  val claims: Claims = parsedJwt.getBody
  // Print the extracted claims
  println(s"User ID: ${claims.get("userId", classOf[String])}")
  println(s"User Name: ${claims.get("userName", classOf[String])}")

}
