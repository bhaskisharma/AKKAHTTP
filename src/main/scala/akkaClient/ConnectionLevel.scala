package akkaClient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}


object ConnectionLevel extends App with PaymentJsonSupport {

  implicit val system: ActorSystem = ActorSystem("ClientApp")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  //  val connecitonFlow = Http().outgoingConnection("www.google.com") //same server request

  //  def onOffRequest(httpRequest: HttpRequest) = {
  //    Source.single(httpRequest).via(connecitonFlow).runWith(Sink.head)
  //  }

  //  onOffRequest(HttpRequest()).onComplete {
  //    case Success(value) => println(s"request successful $value")
  //    case Failure(ex) => println(s"exception $ex")
  //  }

  // Payment system


  import PaymentSystemDomain._

  val creditCards = List(
    CreditCard("4242-4242-4242-4242", "424", "tx-test-account"),
    CreditCard("1234-1234-1234-1234", "123", "tx-daniels-account"),
    CreditCard("1234-1234-4321-4321", "321", "my-awesome-account"),
    CreditCard("1234-1234-4321-4321", "321", "my-awesome-account")
  )

  val paymentRequests = creditCards.map(creditCard => PaymentRequest(creditCard, "rtjvm-store-account", 99))


  val serverHttpReq: HttpRequest = HttpRequest(HttpMethods.GET,
    uri = Uri("/api/payments?userId=abc")
  )

  val serverHttpRequest = paymentRequests.map(paymentRequest =>
    HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        paymentRequest.toJson.prettyPrint
      )
    )
  )

  val dataHttpReq = Http

  val data: Future[HttpResponse] = Source.single(serverHttpReq)
    .via(Http()
      .outgoingConnection("localhost", 9293))
    .runWith(Sink.head)


  data.onComplete{
    case Success(value) => value.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach{body =>
      println("response i got in body " + body.utf8String)

      Source(serverHttpRequest)
        .via(Http().outgoingConnection("localhost", 9293))
        .to(Sink.foreach[HttpResponse](println))
        .run()
    }
    case Failure(exception) => println(exception)
  }
}
