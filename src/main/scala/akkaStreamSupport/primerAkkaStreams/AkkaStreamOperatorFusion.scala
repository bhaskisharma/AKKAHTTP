package akkaStreamSupport.primerAkkaStreams

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object AkkaStreamOperatorFusion extends App {

  implicit val system: ActorSystem = ActorSystem("OperatorFusionSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val simpleSource = Source(1 to 1000)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach(println)


  //by default run on same Actor // Single CPU Core is used for every single element
//  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()


  class SimpleActor extends Actor {
    override def receive: Receive = {
      case x: Int =>
        val x2 = x + 1
        val y = x2 * 10
        println(x2)
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor], "SimpleActo")

  //  (1 to 1000).foreach(simpleActor ! _)


  val complexFlow = Flow[Int].map{x =>
    Thread.sleep(1000)
    x +1
  }

  val complexFlow2 = Flow[Int].map{x =>
    Thread.sleep(1000)
    x * 10
  }

//  simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  //async boundry
//
//  simpleSource.via(complexFlow).async // run on one actor
//    .via(complexFlow2).async // run on another actor
//    .to(simpleSink).run() //run on third actor

  // ordering guarantees
  Source(1 to 3)
    .map(element => { println(s"Flow A: $element"); element }).async
    .map(element => { println(s"Flow B: $element"); element }).async
    .map(element => { println(s"Flow C: $element"); element }).async
    .runWith(Sink.ignore)



}
