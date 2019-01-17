import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controller.ServiceCallBack
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.ExecutionContext

/** *RestServer Handler */
class RestServer(implicit val system: ActorSystem,
                 implicit val materialize: ActorMaterializer) extends ServiceCallBack with Logging {

  def startServer(interface: String, port: Int): Unit = {
    Http().bindAndHandle(route, interface, port)

    logger.info("Rest Server Started...........")
  }
}

object Driver {
  def main(args: Array[String]): Unit = {

    /**get the interface and port details from application.conf*/
    val interface = ConfigFactory.load().getString("http.interface")
    val port  = ConfigFactory.load().getInt("http.port")

    /** Rest Server Actor implementation */
    implicit val actorSystem: ActorSystem = ActorSystem("Rest_Server")
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    /** *SERVER START */
    val restServer = new RestServer()
    restServer.startServer(interface, port)

  }
}
