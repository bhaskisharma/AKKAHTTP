package highLevelHttpApi

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import lowLevelHttpAPI.{Guitar, GuitarDB, GuitarStoreJsonProtocol}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

object HighLevelExample extends App with GuitarStoreJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("HighLevelApiDesign")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._
  import GuitarDB._

  /*import akka.http.scaladsl.server.Directives._

   GET /api/guitar fetches ALL the guitars in the store
   GET /api/guitar?id=x fetches the guitar with id X
   GET /api/guitar/X fetches guitar with id X
   GET /api/guitar/inventory?inStock=true
  */


  val guitarDb = system.actorOf(Props[GuitarDB], "GuitarDB")

  val guitarList = List(
    Guitar("Fender", "Stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach(guitar =>
    guitarDb ! CreateGuitar(guitar)
  )


  implicit val timeout = Timeout(2 seconds)

  val guitarServerRoute =
    path("api" / "path") {
      get {
        val guitarFuture: Future[List[Guitar]] = (guitarDb ? FindAllGuitar).mapTo[List[Guitar]]
        val entityData = guitarFuture.map { guitarOption =>
          HttpEntity(
            ContentTypes.`application/json`,
            guitarOption.toJson.prettyPrint
          )
        }
        complete(StatusCodes.OK)
      } ~
        parameter("id".as[Int]) { guitarId: Int =>
          get {
            val guitarFuture: Future[List[Guitar]] = (guitarDb ? FindGuitar(guitarId)).mapTo[List[Guitar]]
            val entityGuitar = guitarFuture.map { guitarOption =>
              HttpEntity(
                ContentTypes.`application/json`,
                guitarOption.toJson.prettyPrint
              )
            }
            complete(StatusCodes.OK)
          }
        } ~
       path("api" / "path" / IntNumber){ guitarId =>
         get {
           val guitarFuture: Future[List[Guitar]] = (guitarDb ? FindGuitar(guitarId)).mapTo[List[Guitar]]
           val entityGuitar = guitarFuture.map { guitarOption =>
             HttpEntity(
               ContentTypes.`application/json`,
               guitarOption.toJson.prettyPrint
             )
           }
           complete(StatusCodes.OK)
         }
       } ~
      path("api" / "path" / "inventory"){
        get {
          parameter("inStock".as[Int]) { inStock : Int =>
            val guitarFuture: Future[List[Guitar]] = (guitarDb ? FindGuitar(inStock)).mapTo[List[Guitar]]
            val entityGuitar = guitarFuture.map { guitarOption =>
              HttpEntity(
                ContentTypes.`application/json`,
                guitarOption.toJson.prettyPrint
              )
            }
            complete(StatusCodes.OK)
          }
        }
      }
    }

  Http().bindAndHandle(guitarServerRoute, "localhost", 8080)



}
