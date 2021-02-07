name := "music-shop-http4s-kafka-app"

version := "0.1"

scalaVersion := "2.13.4"

val cats         = "2.1.1"
val circe        = "0.13.0"
val doobie       = "0.9.0"
val flyway       = "6.0.1"
val http4s       = "0.21.3"
val postgresql   = "42.2.6"
val pureConfig   = "0.11.1"
val refined      = "0.9.13"

libraryDependencies ++= Seq(
  "org.typelevel"          %% "cats-core"            % cats,
  "io.circe"               %% "circe-core"           % circe,
  "io.circe"               %% "circe-generic"        % circe,
  "io.circe"               %% "circe-refined"        % circe,
  "io.circe"               %% "circe-parser"         % circe,
  "org.tpolecat"           %% "doobie-core"          % doobie,
  "org.tpolecat"           %% "doobie-hikari"        % doobie,
  "org.tpolecat"           %% "doobie-postgres"      % doobie,
  "org.tpolecat"           %% "doobie-refined"       % doobie,
  "org.flywaydb"           %  "flyway-core"          % flyway,
  "org.http4s"             %% "http4s-blaze-server"  % http4s,
  "org.http4s"             %% "http4s-blaze-client"  % http4s,
  "org.http4s"             %% "http4s-circe"         % http4s,
  "org.http4s"             %% "http4s-dsl"           % http4s,
  "org.postgresql"         %  "postgresql"           % postgresql,
  "com.github.pureconfig"  %% "pureconfig"           % pureConfig
)

// add kafka
resolvers += "confluent" at "https://packages.confluent.io/maven/"
libraryDependencies += "com.github.fd4s" %% "fs2-kafka" % "1.3.1"
libraryDependencies += "com.github.fd4s" %% "fs2-kafka-vulcan" % "1.3.0"
libraryDependencies += "com.github.fd4s" %% "vulcan" % "1.3.0"
