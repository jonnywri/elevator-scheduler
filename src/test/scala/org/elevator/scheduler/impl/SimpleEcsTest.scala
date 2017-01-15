package org.elevator.scheduler.impl

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import org.elevator.scheduler.{DOWN, ElevatorStatus, UP}
import org.scalatest.{BeforeAndAfterEach, FunSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Success

/**
  * Testing the simple elevator control system.
  */
class SimpleEcsTest extends FunSpec with MustMatchers with BeforeAndAfterEach {

  describe("A simple elevator control system") {
    it("should accept requests, responding with the correct elevator to claim the request") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      pickup1 onComplete {
        case Success(Some(0)) => // I think I win?
        case _ => fail("I expected an execution success with an id of 0.")
      }
      Await.result(pickup1, Duration.create(10000L, TimeUnit.MILLISECONDS))
      pickup1.isCompleted must be(true)
      ecs.shutdown()
    }

    it("should accept a request with two elevators created") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      pickup1 onComplete {
        case Success(Some(0)) => // I think I win?
        case Success(Some(1)) => // I also win in this case.  This is non deterministic as it is asynchronous.
        case _ => fail("I expected an execution success with an id of 0 or 1.")
      }
      Await.result(pickup1, Duration.create(10000L, TimeUnit.MILLISECONDS))
      pickup1.isCompleted must be(true)
      ecs.shutdown()
    }

    it("should be unable to serve multiple requests without steps") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      val pickup2: Future[Option[Int]] = ecs.pickup(3, UP)
      pickup1 onComplete {
        case Success(Some(0)) => // This is a win.
        case t => fail(s"I expected an execution success with an id of 0, but received ${t}.")
      }
      pickup2 onComplete {
        case Success(None) => // success case
        case _ => fail("I expected no return result.")
      }
      Await.result(pickup1, Duration.create(1000L, TimeUnit.MILLISECONDS))
      Await.result(pickup2, Duration.create(2000L, TimeUnit.MILLISECONDS))
      pickup1.isCompleted must be(true)
      pickup2.isCompleted must be(true)
      ecs.queuedRequests.size must be(1) // race condition is present that causes this to fail sometimes
      ecs.shutdown()
    }

    it("should return the current elevator status") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val elevatorStatuses = ecs.status()
      elevatorStatuses.size must be(1)
      elevatorStatuses.head must be(ElevatorStatus(0, 1, None))
    }

    it("should return the current elevator status for multiple elevators") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      Await.result(pickup1, Duration.create(1000L, TimeUnit.MILLISECONDS))
      ecs.queuedRequests.size must be(0)
      val elevatorStatuses = ecs.status()
      elevatorStatuses.size must be(2)

      pickup1 onComplete {
        case Success(Some(t)) => {
          elevatorStatuses.find((s: ElevatorStatus) => s.id.equals(t)) match {
            case Some(ElevatorStatus(t, 1, Some(UP), None, _)) =>
            case _ => fail("Failing test due to missing match.")
          }
        }
        case _ => fail("Failing the test due to no completion.")
      }
    }

    it("should be able to step") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      Await.result(pickup1, Duration.create(1000L, TimeUnit.MILLISECONDS))
      ecs.queuedRequests.size must be(0)
      val elevatorStatuses = ecs.status()
      elevatorStatuses.size must be(1)
      ecs.step

      val updatedElevatorStatuses = ecs.status()
      updatedElevatorStatuses.head.floor must be (2)
      updatedElevatorStatuses.head.direction must be (Some(UP))
    }

    it("should be able to make multiple steps") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      Await.result(pickup1, Duration.create(1000L, TimeUnit.MILLISECONDS))
      ecs.queuedRequests.size must be(0)
      val elevatorStatuses = ecs.status()
      elevatorStatuses.size must be(1)
      ecs.step
      ecs.step
      ecs.step

      val updatedElevatorStatuses = ecs.status()
      updatedElevatorStatuses.head.floor must be (4)
      updatedElevatorStatuses.head.direction must be (Some(UP))
    }

    it("should be stopped at the appropriate floor") {
      val ecs = new SimpleEcs(10)
      ecs.create(5)
      val pickup1: Future[Option[Int]] = ecs.pickup(6, DOWN)
      Await.result(pickup1, Duration.create(1000L, TimeUnit.MILLISECONDS))
      ecs.queuedRequests.size must be(0)
      val elevatorStatuses = ecs.status()
      elevatorStatuses.size must be(1)
      ecs.step
      ecs.step
      ecs.step
      ecs.step
      ecs.step
      ecs.step

      val updatedElevatorStatuses = ecs.status()
      updatedElevatorStatuses.head.floor must be (6)
      updatedElevatorStatuses.head.direction must be (None)
    }
  }

}
