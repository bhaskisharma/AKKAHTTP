package highLevelHttpApi

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCodes}
import akka.stream.ActorMaterializer

object DirectivesApp extends App {

  implicit val system = ActorSystem("DirectivesApp")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  import akka.http.scaladsl.server.Directives._


  val simpleHTTPRoutes =
    post { // equivalent directives for get, put, patch, delete, head, options
      complete(StatusCodes.Forbidden)
    }


  val simplePathRoutes = {
    path("about") {
      complete(HttpEntity(
        ContentTypes.`application/json`,
        """
          |<html>
          | <body>
          |   Hello from the about page!
          | </body>
          |</html>
        """.stripMargin
      ))
    }
  }


  val complexPathRoutes =
    path("api" / "myEndPoint") {
      complete(StatusCodes.OK)
    }

  val dontConfues =
    path("api/myEndPoint") {
      complete(StatusCodes.OK)
    }

  val pathEndRoute =
    pathEndOrSingleSlash {
      complete(StatusCodes.OK)
    }


  //  Http().bindAndHandle(dontConfues,"localhost",9898)


  /**
    * Types 2 directives
    **/
  // GET on /api/item/42

  val pathExtractionRules =
  path("api" / "item" / IntNumber) { itemNumber: Int =>
    println(s"I've got a number from client $itemNumber")
    complete(StatusCodes.OK)
  }

  val pathMultiExtract =
    path("api" / "order" / IntNumber / IntNumber) { (id, inventory) =>
      println(s" I've got two number from client $id and $inventory")
      complete(StatusCodes.OK)
    }

  val queryParamExtraction =
    path("api" / "item") {
      parameter('id.as[Int]) {
        itemId: Int => {
          println(s"I've got a number from client $itemId")
          complete(StatusCodes.OK)
        }
      }
    }

  val extractRequestRoute =
    path("controlEndPoint") {
      extractRequest { httpRequest: HttpRequest => {
        extractLog { log: LoggingAdapter =>
          log.info(s"extract request from http request $httpRequest")
          complete(StatusCodes.OK)
        }
      }
      }
    }


  //    * Type #3: composite directives

  val simpleNestedRoutes =
    path("api" / "item") {
      get {
        complete(StatusCodes.OK)
      }
    }

  val compactSimpleNestedRoutes = (path("api" / "item") & get) {
    complete(StatusCodes.OK)
  }

  val compactExtractRequestRoute =
    (path("controlEndpoint") & extractRequest & extractLog) { (request, log) =>
      log.info(s"I got the http request: $request")
      complete(StatusCodes.OK)
    }

  // /about and /aboutUs


  val repeatedRoutes =
    path("about") {
      complete(StatusCodes.OK)
    } ~
      path("aboutUs") {
        complete(StatusCodes.OK)
      }

  val dryRoutes = (path("about") | path("aboutUs")) {
    complete(StatusCodes.OK)
  }

  // yourblog.com/42 AND yourblog.com?postId=42


  val blogRoutes =
    path(IntNumber) { blogId: Int => {
      complete(StatusCodes.OK)
    }
    }

  val anotherWayRoutes =
    parameter('postId.as[Int]) { id: Int =>
      complete(StatusCodes.OK)
    }

  val mixRoutes = (path(IntNumber) | parameter('postId.as[Int])) { blogID: Int =>
    complete(StatusCodes.OK)
  }

  /**
    * Type #4: "actionable" directives
    */

  val completeOkRoute = complete(StatusCodes.OK)

  val failedRoute =
    path("notSupported") {
      failWith(new RuntimeException("Unsupported!")) // completes with HTTP 500
    }

  val routeWithRejection =
  //    path("home") {
  //      reject
  //    } ~
    path("index") {
      completeOkRoute
    }

  /**
    * Exercise: can you spot the mistake?!
    */
  val getOrPutPath =
    path("api" / "myEndpoint") {
      get {
        completeOkRoute
      } ~
        post {
          complete(StatusCodes.Forbidden)
        }
    }


  Http().bindAndHandle(extractRequestRoute, "localhost", 9898)


}
