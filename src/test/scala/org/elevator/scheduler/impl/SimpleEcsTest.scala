package org.elevator.scheduler.impl

import scala.concurrent.ExecutionContext.Implicits.global
import org.elevator.scheduler.{DOWN, ElevatorRequest}
import org.scalatest.{BeforeAndAfterEach, FunSpec, MustMatchers}

import scala.concurrent.Future
import scala.util.{Success}

/**
  * Testing the simple elevator control system.
  */
class SimpleEcsTest extends FunSpec with MustMatchers with BeforeAndAfterEach {

  var ecs: SimpleEcs = new SimpleEcs(10)

  describe("A simple elevator control system") {
    it("should accept requests, responding with the correct elevator to claim the request") {

      ecs.create(5)
      val actualElevator: Future[Option[Int]] = ecs.pickup(ElevatorRequest(6, DOWN))
      actualElevator onComplete {
        case Success(Some(0)) => // I think I win?
        case _ => fail("I expected an execution success with an id of 0.")
      }
    }
  }

}
