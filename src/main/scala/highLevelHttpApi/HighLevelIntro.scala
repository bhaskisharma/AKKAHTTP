package highLevelHttpApi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import lowLevelHttpAPI.HttpsContext

object HighLevelIntro extends App {

  implicit val system = ActorSystem("HighLevelIntro")
  implicit val materializer = ActorMaterializer()


  import system.dispatcher

  //Directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoutes : Route  =
    path("home"){ //Directives are builiding block for http req
      complete(StatusCodes.OK)
    }

  val pathGetRoute =
    path("home"){
      get{
        complete(StatusCodes.OK)
      }
    }

  //chaining routes

  val chainRoutes =
    path("myEndPoint"){
      get{
        complete(StatusCodes.OK)
      } ~ /**  my tild operator**/
      post{
        complete(StatusCodes.Forbidden)
      }
    } ~
    path("home") {
        complete(HttpEntity(
          ContentTypes.`application/json`,
          """
            |<html>
            | <body>
            |   Hello from the high level Akka HTTP!
            | </body>
            |</html>
          """.stripMargin
        ))
    }

  Http().bindAndHandle(chainRoutes,"localhost",9000,HttpsContext.httpsContext)

}
