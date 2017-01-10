package org.elevator.scheduler.impl

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import org.elevator.scheduler._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/**
  * Very basic elevator control system, which waits for claims to requests.  Requests are tracked centrally by the
  * elevator control system and use a system of callbacks to communicate with the underlying actors (elevators).
  */
class SimpleEcs(floors: Int) extends ElevatorControlSystem {

  /**
    * Internal way to represent autogenerated ids.
    */
  val elevatorIdGenerator = new IdGenerator
  var elevators: ArrayBuffer[ActorRef] = ArrayBuffer()

  var requestIdGenerator = new IdGenerator
  // this sequence really needs to be thread safe, but is not currently
  var requests: ArrayBuffer[ElevatorRequest] = ArrayBuffer()
  def queuedRequests = requests
  val actorSystem: ActorSystem = ActorSystem("elevators")

  val requestTimeout: Int = 1000
  val statusTimeout: Int = 1000

  override def status(): Iterable[ElevatorStatus] = {
    println(s"Creating countdown latch with ${elevators.size}")
    val countDownLatch = new ElevatorStatusCallback(elevators.size)
    actorSystem.actorSelection(actorSystem / "*") ! new ElevatorStatusRequest(countDownLatch)
    countDownLatch.await(statusTimeout, TimeUnit.MILLISECONDS)
    countDownLatch.status
  }

  override def create(capacity: Int) = {
    val elevatorId = elevatorIdGenerator.next
    val actor = actorSystem.actorOf(Props(new FcfsEvelator(elevatorId, capacity, floors)), s"elevator-${elevatorId}")
    elevators += actor
  }

  override def update(elevatorId: Int, config: ElevatorConfig) = {
    actorSystem.actorSelection(actorSystem / elevatorId.toString) ! config
  }

  override def pickup(floor: Int, direction: Direction): Future[Option[Int]] = {
    val requestId = requestIdGenerator.next
    val callback = new ElevatorRequestCallback(requestId)
    val request = ElevatorRequest(requestId, floor, direction, callback)
    requests += request
    actorSystem.actorSelection(actorSystem / "*") ! request
    findElevator(request)
  }

  def repeat(request: ElevatorRequest): Future[Option[Int]] = {
    actorSystem.actorSelection(actorSystem / "*") ! request
    findElevator(request)
  }

  /**
    * This basic implementation will have the potential to violate a "fcfs" behavior expected.  The reason for this
    * is that a request may start into a queue loop, having the potential to never be fulfilled given enough traffic,
    * but also may simply be serviced after another request which didn't have to wait.
    *
    * The simplest way to fix this is to have another scheduler which simply tries to execute requests at the top
    * in order, but this is beyond the scope of this simple implementation.
    */
  def findElevator(request: ElevatorRequest): Future[Option[Int]] = Future {
    request.callback.await(requestTimeout, TimeUnit.MILLISECONDS)
    request.callback.acceptedElevatorId match {
      case Some(t) => requests -= request
      case _ => // should log that this request will stay in the backlog
    }

    request.callback.acceptedElevatorId
  }

  override def step() = ???

  override def shutdown() = {
    actorSystem.terminate()
  }
}

class IdGenerator {
  private val id = new AtomicInteger
  def next: Int = id.getAndIncrement()
}
