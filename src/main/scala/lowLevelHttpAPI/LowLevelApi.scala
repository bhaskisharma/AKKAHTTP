package lowLevelHttpAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.ExecutionContextExecutor

object LowLevelApi extends App {

  implicit val system: ActorSystem = ActorSystem("LowLevelApiAPP")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val matrializer: ActorMaterializer = ActorMaterializer()

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => HttpResponse(entity = HttpEntity.Strict(ContentTypes.`text/html(UTF-8)`,
      ByteString("<html><body>Hello!</body></html>")))

    case HttpRequest(GET, Uri.Path("/message"), _, _, _) => HttpResponse(entity = "message")

    case _: HttpRequest => HttpResponse(404, entity = "UnknownResource ")
  }
  Http().bindAndHandleSync(requestHandler,"localhost",1234)

}
