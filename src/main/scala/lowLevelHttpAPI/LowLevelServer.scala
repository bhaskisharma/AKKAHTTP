package lowLevelHttpAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object LowLevelServer extends App {

  implicit val system = ActorSystem("LowLevelServerApi")
  implicit val materialize = ActorMaterializer()

  import system.dispatcher

  val serverSource = Http().bind("localhost", 8099)

  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accepted Connection from: ${connection.remoteAddress}")
  }

  val serverBinding = serverSource.to(connectionSink).run()

  serverBinding.onComplete {
    case Success(binding) => println("Server binding successfully")
      binding.unbind()
    case Failure(ex) => println(s"Server Binding failure: $ex")
  }


  //Method 1 synchronously HTTP Response

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(StatusCodes.OK, //HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |Hello from Akka HTTP!
            |</body>
            | </html>
          """.stripMargin
        )
      )

    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
          """.stripMargin
        ))
  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithSyncHandler(requestHandler)
  }

  //  Http().bind("localhost",9999).runWith(httpSyncConnectionHandler)
  Http().bindAndHandleSync(requestHandler, "localhost", 9990)

  //Method number 2 Async handler

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET,Uri.Path("/home"), _, _, _) =>
      Future(HttpResponse(StatusCodes.OK, //HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |Hello from Akka HTTP!
            |</body>
            | </html>
          """.stripMargin
        )
      ))

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
          """.stripMargin
        )))
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithAsyncHandler(asyncRequestHandler)
  }

//  Http().bind("localhost",9191).runWith(httpAsyncConnectionHandler) //manual
    Http().bindAndHandleAsync(asyncRequestHandler,"localhost",9191)//shorthand


  //Method number 3  - async via Akka streams
  val streamBasedRequestHandler: Flow[HttpRequest,HttpResponse,_] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET,Uri.Path("/home"), _, _, _) =>
      HttpResponse(StatusCodes.OK, //HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |Hello from Akka HTTP!
            |</body>
            | </html>
          """.stripMargin
        )
      )

    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
          """.stripMargin
        ))
  }

//  Http().bind("localhost",9779).runForeach{connection =>
//    connection.handleWith(streamBasedRequestHandler)
//  }

  //shorthandVersion

  Http().bindAndHandle(streamBasedRequestHandler,"localhost",9779)


}
