package lowLevelHttpAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink


/**
  * Exercise: create your own HTTP server running on localhost on 8388, which replies
  *   - with a welcome message on the "front door" localhost:8388
  *   - with a proper HTML on localhost:8388/about
  *   - with a 404 message otherwise
  */
object AboutHomeApi extends App {

  implicit val system = ActorSystem("OwnHttpServer")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher


  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |Hello from front door.
          """.stripMargin
        )
      )

    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<div style="color: red">
            |Hello from the about page!
            |<div>
            |</body>
            |</html>
          """.stripMargin
        )
      )

    case HttpRequest(HttpMethods.GET, Uri.Path("/search"), _, _, _) =>
      HttpResponse(
        StatusCodes.Found,
        headers = List(Location("http://google.com"))
      )

    case request: HttpRequest => {
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "OOPS, you're in no man's land, sorry."
        )
      )
    }
  }

  val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8388)

  bindingFuture.flatMap(binding => binding.unbind()).onComplete { _ => system.terminate() }

}
