package controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer


//@TODO Case Class request handler
/**
  * Trait to handel serverCallBack
  **/
trait ServiceCallBack {

  implicit val system: ActorSystem
  implicit val materialize: ActorMaterializer

  /**
    * Routes for Rest Service like path and method handler
    * method supported :- POST , GET , OPTIONS
    **/

  import Directives._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._


  /***/
  val rejectionHandler: RejectionHandler = corsRejectionHandler withFallback RejectionHandler.default


  // Your exception handler
  val exceptionHandler = ExceptionHandler {
    case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
  }

  val handleErrors: Directive[Unit] = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)


  val route: Route = handleErrors {
    cors() {
      handleErrors {
        path("confirm") {
          post {
            entity(as[String]) { json =>
              complete(StatusCodes.Accepted, json.toString)
            }
          }
        } ~
          get {
            complete(StatusCodes.Found, "Get Method Status")
          } ~
          options {
            println("Options method requested "+StatusCodes.OK)
            complete(StatusCodes.OK, "OK")
          }
      }
    }
  }
}
