package lowLevelHttpAPI

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import lowLevelHttpAPI.GuitarDB.{CreateGuitar, FindAllGuitar, FindGuitar, GuitarCreated}
import spray.json._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

case class Guitar(make: String, model: String)

object GuitarDB {

  case class CreateGuitar(guitar: Guitar)

  case class GuitarCreated(id: Int)

  case class FindGuitar(id: Int)

  case object FindAllGuitar

}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case FindAllGuitar =>
      log.info("searching for all guitars")
      sender() ! guitars.values.toList
    case FindGuitar(id) =>
      log.info(s"searching guitar by id $id")
      sender() ! guitars.get(id)
    case CreateGuitar(guitar) =>
      log.info("adding  guitar ")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1
  }
}

/*
   - GET on localhost:8080/api/guitar => ALL the guitars in the store
   - GET on localhost:8080/api/guitar?id=X => fetches the guitar associated with id X
   - POST on localhost:8080/api/guitar => insert the guitar into the store
  */

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat: RootJsonFormat[Guitar] = jsonFormat2(Guitar)
}

object GuitarRestArchSystem extends App with GuitarStoreJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("GuitarSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  val simpleGuitar = Guitar("Yamaha", "F310")

  // unmarshalling
  val simpleGuitarJsonString =
    """
      |{
      |  "make": "Fender",
      |  "model": "Stratocaster",
      |  "quantity": 3
      |}
    """.stripMargin


  val guitarDb = system.actorOf(Props[GuitarDB], "GuitarDB")

  val guitarList = List(
    Guitar("Fender", "Stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach(guitar =>
    guitarDb ! CreateGuitar(guitar)
  )

  implicit val defaultTimeout: Timeout = Timeout(2 seconds)

  def getGuitar(query: Uri.Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    guitarId match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(id : Int) =>
        val guitarFuture: Future[Option[Guitar]]= (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
        guitarFuture.map{
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(guitar) => HttpResponse(StatusCodes.OK,
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitar.toJson.prettyPrint
            ))
        }
    }
  }

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitars"), _, _, _) =>

      val query = uri.query() //query object act like a Map[String, String]

      if (query.isEmpty) {
        val guitarFuture = (guitarDb ? FindAllGuitar).mapTo[List[Guitar]]
        guitarFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
      } else {
        getGuitar(query)
      }


    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitars"), _, entity, _) =>
      val strictEntityFuture = entity.toStrict(3 seconds)
      strictEntityFuture.flatMap { strictEntity =>
        val guitarJsonToString = strictEntity.data.utf8String
        val guitar = guitarJsonToString.parseJson.convertTo[Guitar]

        val guitarCreated = {
          guitarDb ? CreateGuitar(guitar)
        }.mapTo[GuitarCreated]

        guitarCreated.map { _ =>
          HttpResponse(StatusCodes.OK)
        }
      }

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future {
        HttpResponse(StatusCodes.NotFound)
      }
  }

  Http().bindAndHandleAsync(requestHandler, "localhost", 9119)

}
