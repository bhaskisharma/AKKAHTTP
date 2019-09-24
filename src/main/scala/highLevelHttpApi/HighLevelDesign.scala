package highLevelHttpApi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import spray.json._
import akka.http.scaladsl.server.Directives._

case class Person(pinId: Int, name: String)


import scala.concurrent.duration._


trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val personJson: RootJsonFormat[Person] = jsonFormat2(Person)
}

object HighLevelDesign extends App with PersonJsonProtocol {

  implicit val system = ActorSystem("HighLevelDesign")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  /**
    * Exercise:
    *
    * - GET /api/people: retrieve ALL the people you have registered
    * - GET /api/people/pin: retrieve the person with that PIN, return as JSON
    * - GET /api/people?pin=X (same)
    * - (harder) POST /api/people with a JSON payload denoting a Person, add that person to your database
    *   - extract the HTTP request's payload (entity)
    *     - extract the request
    *     - process the entity's data
    */


  var people = List(
    Person(1, "Alice"),
    Person(2, "Bob"),
    Person(3, "Charlie")
  )


  val personServerRoute =
    pathPrefix("api" / "people") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`application/json`,
            people.toJson.prettyPrint
          )
        )
      } ~
        get {
          (path(IntNumber) | parameter("pinId".as[Int])) { pinId =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                people.find(_.pinId == pinId).toJson.prettyPrint
              )
            )
          }
        } ~
        (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
          val entity = request.entity
          val strictEntity = entity.toStrict(2 seconds)
          val personFuture = strictEntity.map(_.data.utf8String.parseJson.convertTo[Person])
          personFuture.map(person =>
            people = people :+ person
          )
          complete(StatusCodes.OK,
            HttpEntity(
              ContentTypes.`application/json`,
              people.toJson.prettyPrint
            ))
        }
    }

  Http().bindAndHandle(personServerRoute, "localhost", 8180)

}
