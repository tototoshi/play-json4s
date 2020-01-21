package com.github.tototoshi.play2.json4s.testkit

import akka.actor.ActorSystem
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait WithActorSystem extends TestSuiteMixin { self: TestSuite =>
  implicit var actorSystem: ActorSystem = _

  abstract override def withFixture(test: NoArgTest): Outcome = {
    actorSystem = ActorSystem()
    try {
      super.withFixture(test)
    } finally {
      Await.result(actorSystem.terminate(), Duration("30s"))
    }
  }
}
