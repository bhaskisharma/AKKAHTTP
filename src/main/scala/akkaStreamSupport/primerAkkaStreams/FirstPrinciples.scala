package akkaStreamSupport.primerAkkaStreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {

  implicit val system: ActorSystem = ActorSystem("FirstPrinciple")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //source
  val source = Source(1 to 10)

  //sinks
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  graph.run()

  //flows

  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowToSink = flow.to(sink)

//  sourceWithFlow.to(sink).run()
//  source.to(flowToSink).run()
//  source.via(flow).to(sink).run()


//  //nulls are not allowed in reactive
//
//  val illegalSource = Source.single[String](null)
//  illegalSource.to(Sink.foreach(println)).run()
//

  //various kind of sources

  val finiteSource = Source.single(1)
  val anotherSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infinteSource = Source(Stream.from(1))

  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(1))


  //various kind of Sinks

  val theMostSinks  = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int]
  val foldSink = Sink.fold[Int,Int](0)((a,b) => a + b)


  //various kind of flows

  val mapFlow = Flow[Int].map(x => x*2)
  val takeFlow = Flow[Int].take(5)

  //drop filter
  //not have flatmap

  //source  => flow => flow => Sink

//  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink).run()

  // syntactic sugars
  val mapSource = Source(1 to 10).map(x => x * 2) // Source(1 to 10).via(Flow[Int].map(x => x * 2))
  // run streams directly
  //  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  // OPERATORS = components


  /**
    * Exercise: create a stream that takes the names of persons, then you will keep the first 2 names with length > 5 characters.
    *
    */


  val names = List("Alice", "Bob", "Charlie", "David", "Martin", "AkkaStreams")

  val personSource = Source(names)
  val longNameFlow = Flow[String].filter(name => name.length > 5)
  val limitFlow = Flow[String].take(2)
  val nameSink = Sink.foreach[String](println)

  personSource.via(longNameFlow).via(limitFlow).to(nameSink).run()

}
