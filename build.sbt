name := "AKKAHTTP"

version := "0.1"

scalaVersion := "2.11.8"


val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.7"
val scalaTestVersion = "3.0.5"

libraryDependencies ++={
  Seq(
    /** Scala Dependency */
    "org.scala-lang" % "scala-library" % "2.11.8",

  /**akka actor and akka http Dependencies*/
    "com.typesafe.akka" %% "akka-actor" % "2.5.20",
    "com.typesafe.akka" %% "akka-http" % "10.1.7",
    "com.typesafe.akka" %% "akka-http-core" % "10.1.7",
    "ch.megard" %% "akka-http-cors" % "0.3.0",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",

    //Akka Stream Support
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion,

    /**logger Dependencies*/
    "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
    "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
    "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"

  )
}