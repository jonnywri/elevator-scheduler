package org.elevator.scheduler

import akka.actor.Actor

/**
  * Minimum defintion of an elevator - has a unique identifier, a list of current destinations, a list of current
  * floors to stop at for picking up, a direction, a capacity and a total load.
  *
  * All elevators must inherit from the Actor trait as well.
  */
trait Elevator extends Actor {
  /**
    * Defines the basic ability for the elevator to attempt to make a claim.  An elevator request is passed to this
    * object, and the object returns whether or not it can accept the request.
    */
  def claim(request: ElevatorRequest): Boolean

  /**
    * Reports the current status of the elevator
    */
  def report(statusRequest: ElevatorStatusRequest): Unit

  /**
    * Forces the elevator to step forward one unit (usually one floor).
    */
  def step
}

/**
  * Mechanisms by which elevators can report back their current status.
  */
class ElevatorStatusRequest(statusCallback: ElevatorStatusCallback)
class ElevatorStatusCallback(expectedReporters: Int, timeout: Int) {

  def report(elevatorStatus: ElevatorStatus): Unit = {

  }
}

case class ElevatorRequest(floor: Int, direction: Direction)
case class ElevatorConfig(capacity: Int)
case class ElevatorStatus(floor: Int, direction: Direction)
