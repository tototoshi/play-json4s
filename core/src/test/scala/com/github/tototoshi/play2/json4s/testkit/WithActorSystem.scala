package com.github.tototoshi.play2.json4s.testkit

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait WithActorSystem extends TestSuiteMixin { self: TestSuite =>
  implicit var actorSystem: ActorSystem = _
  implicit var actorMaterializer: Materializer = _

  abstract override def withFixture(test: NoArgTest): Outcome = {
    actorSystem = ActorSystem()
    actorMaterializer = ActorMaterializer()
    try {
      super.withFixture(test)
    } finally {
      Await.result(actorSystem.terminate(), Duration("30s"))
    }
  }
}
