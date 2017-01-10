package org.elevator.scheduler.impl

import akka.actor.Actor
import org.elevator.scheduler._

/**
  * Implements a basic first come, first serviced model.  The goal for this being to show the base case, as well
  * as ability to improve over the base case.
  */
case class FcfsEvelator(id: Int, capacity: Int, maxFloor: Int) extends Elevator {

  // Assumes all elevators are installed and start at floor 1.  Could be taken in as a parameter, simpler this way.
  var currentFloor: Int = 1
  var currentDestination: Option[Int] = None // represents when someone has boarded the elevator and enters a floor
  var currentDirection: Option[Direction] = None
  var currentRequest: Option[ElevatorRequest] = None

  override def receive: Actor.Receive = {
    case request: ElevatorRequest => { claim(request) }
    case config: ElevatorConfig => { println(s"${config} received")}
    case statusRequest: ElevatorStatusRequest => { report(statusRequest) }
    case t => println(s"I've received a message I don't understand ${t}")
  }

  override def claim(request: ElevatorRequest): Boolean = {
    currentRequest match {
      case None => {
        request.callback.accept(id) match {
          case true => {
            currentFloor match {
              case request.floor => true// perform pickup
              case _ => {
                println(s"Claiming request as elevator id ${id}") // this is my cheap logging
                currentRequest = Some(request)
                currentDirection = if (currentFloor < request.floor) Some(UP) else Some(DOWN)
                true
              }
            }
          }
          case false => false // should really be logging here
        }
      }
      case Some(_) => {
        println(s"Unable to claim request ${request} as elevator is already in service.")
        false
      }
    }
  }

  override def report(statusRequest: ElevatorStatusRequest): Unit = {
    statusRequest.callback.report(ElevatorStatus(id, currentFloor, currentDirection))
  }

  override def step = {
  }
}
