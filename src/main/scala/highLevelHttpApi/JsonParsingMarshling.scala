package highLevelHttpApi

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import spray.json.DefaultJsonProtocol
import spray.json._
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import scala.concurrent.duration._

case class Player(nickName: String, character: String, level: Int)


object GameAreaMap {

  case object GetAllPlayer

  case class GetPlayerByNickName(nickName: String)

  case class GetPlayerByChar(character: String)

  case class AddPlayer(player: Player)

  case class RemovePlayer(player: Player)

  case object OperationSuccess

}


class GameAreaMap extends Actor with ActorLogging {

  import GameAreaMap._

  var players: Map[String, Player] = Map[String, Player]()

  override def receive: Receive = {
    case GetAllPlayer =>
      log.info("getting all player")
      sender() ! players.values.toList
    case GetPlayerByNickName(nickName) =>
      log.info(s"getting player by nick name $nickName")
      sender() ! players.get(nickName)
    case GetPlayerByChar(character) =>
      log.info(s"geeting player by character $character")
      sender() ! players.values.toList.filter(_.character == character)
    case AddPlayer(player) =>
      log.info(s"adding player in Gamer $player")
      players = players + (player.nickName -> player)
      sender() ! OperationSuccess
    case RemovePlayer(player) =>
      log.info("removing player from game")
      players = players - player.nickName
      sender() ! OperationSuccess
  }
}

trait PlayerJsonSupport extends DefaultJsonProtocol {
  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat3(Player)
}

object JsonParsingMarshling extends App with PlayerJsonSupport with SprayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("JsonParsing")
  implicit val materilizer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher
  import GameAreaMap._


  val playerActor = system.actorOf(Props[GameAreaMap], "GamerAreaMap")
  val playersList = List(
    Player("martin_killz_u", "Warrior", 70),
    Player("rolandbraveheart007", "Elf", 67),
    Player("daniel_rock03", "Wizard", 30)
  )

  playersList.foreach { player =>
    playerActor ! AddPlayer(player)
  }


  /*
    - GET /api/player, returns all the players in the map, as JSON
    - GET /api/player/(nickname), returns the player with the given nickname (as JSON)
    - GET /api/player?nickname=X, does the same
    - GET /api/player/class/(charClass), returns all the players with the given character class
    - POST /api/player with JSON payload, adds the player to the map
    - (Exercise) DELETE /api/player with JSON payload, removes the player from the map
   */

  implicit val timeout = Timeout(2 seconds)

  val playerSererRoute =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { characterTake =>
          val returnAllThePlayer: Future[List[Player]] = (playerActor ? GetPlayerByChar(characterTake)).mapTo[List[Player]]
          complete(returnAllThePlayer)
        } ~
          (path(Segment) | parameter("nickname")) { nickname =>
            val returnAllThePlayer: Future[Option[Player]] = (playerActor ? GetPlayerByNickName(nickname)).mapTo[Option[Player]]
            complete(returnAllThePlayer)
          } ~
          pathEndOrSingleSlash {
            val returnAllThePlayer: Future[List[Player]] = (playerActor ? GetAllPlayer).mapTo[List[Player]]
            complete(returnAllThePlayer)
          }
      } ~
        post {
          entity(implicitly[FromRequestUnmarshaller[Player]]) { player =>
            playerActor ? AddPlayer(player)
            complete(StatusCodes.OK)
          }

        } ~
        delete {
          entity(as[Player]) { player =>
            playerActor ? RemovePlayer(player)
            complete(StatusCodes.OK)

          }

        }
    }

  Http().bindAndHandle(playerSererRoute, "localhost", 9191)

}
