name := "AKKAHTTP"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++={
  Seq(
    /** Scala Dependency */
    "org.scala-lang" % "scala-library" % "2.11.8",

  /**akka actor and akka http Dependencies*/
    "com.typesafe.akka" %% "akka-actor" % "2.5.11",
    "com.typesafe.akka" %% "akka-http" % "10.1.0",
    "com.typesafe.akka" %% "akka-http-core" % "10.1.0",
    "com.typesafe.akka" %% "akka-stream" % "2.5.11",
    "ch.megard" %% "akka-http-cors" % "0.3.0",

    /**logger Dependencies*/
    "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
    "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
    "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"

  )
}