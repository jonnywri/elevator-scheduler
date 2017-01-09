package org.elevator.scheduler.impl

import akka.actor.{ActorRef, ActorSystem, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import org.elevator.scheduler._

import scala.concurrent.Future

/**
  * Very basic elevator control system, which waits for claims to requests.  Requests are tracked centrally by the
  * elevator control system and
  */
class SimpleEcs(floors: Int) extends ElevatorControlSystem {

  /**
    * Internal way to represent autogenerated ids.
    */
  var currentId: Int = 0
  var elevators: Seq[ActorRef] = Seq()

  val requests: Seq[ElevatorRequest] = Seq()
  val actorSystem: ActorSystem = ActorSystem("elevators")



  override def status(): Iterable[ElevatorStatus] = {
    val statuses = actorSystem.actorSelection("*") ! "status"
    Iterable()
  }

  override def create(capacity: Int) = {
    val actor = actorSystem.actorOf(Props(new FcfsEvelator(currentId, capacity, floors)), "elevator-0")
    elevators :+= actor
    currentId += 1
  }

  override def update(elevatorId: Int, config: ElevatorConfig) = {
    actorSystem.actorSelection(actorSystem / elevatorId.toString) ! config
  }

  override def pickup(request: ElevatorRequest): Future[Option[Int]] = {
    requests.+:(request)
    actorSystem.actorSelection(actorSystem / "*") ! request
    findElevator(request)
  }

  def findElevator(request: ElevatorRequest): Future[Option[Int]] = Future {
    None
  }

  override def step() = {
  }

  override def shutdown() = {
    actorSystem.terminate()
  }
}