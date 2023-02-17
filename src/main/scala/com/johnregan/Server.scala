package com.johnregan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

object Server extends App {
  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher

  val route: Route = cors() {
    path("startRegistration") {
      get {
        // do something to start registration
        println("startRegistration")
        complete("Registration started")
      }
    } ~ path("endRegistration") {
      get {
        println("endRegistration")
        complete("Registration ended")
      }
    }
  }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/")
}
