package akkaClient

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._
import akka.pattern.ask

import scala.concurrent.duration._

case class CreditCard(serialNumber: String, securityCode: String, account: String)

object PaymentSystemDomain {

  case class PaymentRequest(creditCard: CreditCard, receiverAccount: String, amount: Double)

  case object PaymentAccepted

  case object PaymentRejected

}

trait PaymentJsonSupport extends DefaultJsonProtocol {
  implicit val creditCardFormat: RootJsonFormat[CreditCard] = jsonFormat3(CreditCard)
  implicit val paymentRequestFormat: RootJsonFormat[PaymentSystemDomain.PaymentRequest] = jsonFormat3(PaymentSystemDomain.PaymentRequest)
}

class PaymentValidator extends Actor with ActorLogging {

  import PaymentSystemDomain._

  override def receive: Receive = {
    case PaymentRequest(CreditCard(serialNumber, securityCode, senderAccount), receiverAccount, amount) =>
      log.info(s"$senderAccount is trying to send $amount dollars to $receiverAccount")
      if (serialNumber == "1234-1234-1234-1234") sender() ! PaymentRejected
      else sender() ! PaymentAccepted
  }
}


object PaymentSystem extends App with PaymentJsonSupport with SprayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("PaymentSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import PaymentSystemDomain._
  import system.dispatcher

  val paymentActor = system.actorOf(Props[PaymentValidator], "PaymentValidator")

  implicit val timeout: Timeout = Timeout(2 seconds)

  val paymentRoute =
    pathPrefix("api" / "payments") {
      post {
        entity(as[PaymentRequest]) { paymentRequest =>
          val validatorPayment = (paymentActor ? paymentRequest).map {
            case PaymentRejected => StatusCodes.Forbidden
            case PaymentAccepted => StatusCodes.OK
            case _ => StatusCodes.BadRequest
          }
          complete(validatorPayment)
        }
      } ~
        get {
          (path(Segment)  | parameter("userId")) { userId => {
            println(s"user id $userId")
            complete("dckjncdckjndckjndckjncdkjn cdkjncdkjncd cdkjncdkjc cdknckjdncd cdkjncdkjn")
          }
          }
        }
    }

  Http().bindAndHandle(paymentRoute, "localhost", 9293)
}
