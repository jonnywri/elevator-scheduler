package org.elevator.scheduler

import java.util.concurrent.CountDownLatch

import akka.actor.Actor

import scala.collection.mutable.ArrayBuffer

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
  def step(stepRequest: ElevatorStepRequest): Unit
}

/**
  * Mechanisms by which elevators can report back their current status.
  *
  * In a perfect world, each of these would be a set of actors in their own right.
  */
case class ElevatorStatus(id: Int, floor: Int, direction: Option[Direction] = None,
                          destination: Option[Int] = None, request: Option[ElevatorRequest] = None)
case class ElevatorStatusRequest(callback: ElevatorStatusCallback)
// This should really be implemented as a countdown latch, but 4 hours
class ElevatorStatusCallback(expectedReporters: Int) extends CountDownLatch(expectedReporters: Int) {

  private var reportedStatuses: ArrayBuffer[ElevatorStatus] = ArrayBuffer()
  def status = reportedStatuses

  def report(elevatorStatus: ElevatorStatus): Unit = {
    reportedStatuses += elevatorStatus
    countDown()
  }
}

/**
  * Basic representation of the tools to manage messages for requests to actors and actors ability to claim a request.
  */
case class ElevatorRequest(id: Int, floor: Int, direction: Direction, callback: ElevatorRequestCallback)
class ElevatorRequestCallback(requestId: Int) extends CountDownLatch(1) {
  /**
    * Exposes the id of the elevator which accepted the request.
    */
  var acceptedElevatorId: Option[Int] = None
  def acceptedId = acceptedElevatorId

  /**
    * Accepts the pending request.
    *
    * @return whether or not this claim was accepted, as it may have already been claimed.
    */
  def accept(elevatorId: Int): Boolean = {
    this.synchronized {
       getCount match {
        case 1 => {
          acceptedElevatorId = Some(elevatorId)
          countDown()
          true
        }
        case _ => {
          false // this should really do some logging
        }
      }
    }
  }
}

case class ElevatorStepRequest(numSteps: Int)
case class ElevatorConfig(capacity: Int)

