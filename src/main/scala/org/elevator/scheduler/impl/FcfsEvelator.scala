package org.elevator.scheduler.impl

import akka.actor.Actor
import org.elevator.scheduler._

/**
  * Implements a basic first come, first serviced model.  The goal for this being to show the base case, as well
  * as ability to improve over the base case.
  */
case class FcfsEvelator(id: Int, capacity: Int, maxFloor: Int) extends Elevator {

  // Assumes all elevators are installed and start at floor 1.  Could be taken in as a parameter, simpler this way.
  def currentFloor: Int = 1
  def currentDestination: Option[Int] = None
  def currentDirection: Option[Direction] = None
  def currentRequest: Option[ElevatorRequest] = None

  override def receive: Actor.Receive = {
    case request: ElevatorRequest => { claim(request) }
    case config: ElevatorConfig => { println(s"${config} received")}
    case statusRequest: ElevatorStatusRequest => { report(statusRequest) }
    case message: Any => println(s"I've received a message I don't understand ${message}")
  }
  override def claim(request: ElevatorRequest): Boolean = {
    println(s"I'm attempting to claim a request ${request}")
    false
  }

  override def report(statusRequest: ElevatorStatusRequest): Unit = {

  }

  override def step = {
    currentDirection match {
      case Some(UP) => currentFloor + 1
      case Some(DOWN) => currentFloor - 1
      case None => // do nothing
    }

    if (currentFloor.equals(currentDestination)) {
      // do some stuff
    } else if (currentFloor.equals(1) && currentDirection.equals(DOWN)) {
      // do some other stuff
    }
  }
}
