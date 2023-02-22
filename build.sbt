ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "webauthnscala"
  )

libraryDependencies ++= Seq(
  "com.yubico"         % "webauthn-server-core" % "2.4.0",
  "org.bouncycastle"   % "bcprov-jdk18on"       % "1.72",
  "com.typesafe.akka" %% "akka-http"            % "10.2.7",
  "ch.megard"         %% "akka-http-cors"       % "1.1.3",
  "com.typesafe.akka" %% "akka-stream"          % "2.6.15",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.7",
  "ch.qos.logback"     % "logback-classic"      % "1.2.3",
  "io.jsonwebtoken"    % "jjwt"                 % "0.9.1",
  "javax.xml.bind"     % "jaxb-api"             % "2.2.3",
  "org.scalatest"     %% "scalatest"            % "3.2.9" % Test
)
