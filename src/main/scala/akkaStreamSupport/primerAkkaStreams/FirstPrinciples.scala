package akkaStreamSupport.primerAkkaStreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object FirstPrinciples extends App {

  implicit val system: ActorSystem = ActorSystem("FirstPrinciple")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //source
  val source = Source(1 to 10)

  //sinks
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  graph.run()
}
