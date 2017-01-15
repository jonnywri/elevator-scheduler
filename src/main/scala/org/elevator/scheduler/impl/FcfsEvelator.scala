package org.elevator.scheduler.impl

import akka.actor.Actor
import org.elevator.scheduler._

/**
  * Implements a basic first come, first serviced model.  The goal for this being to show the base case, as well
  * as ability to improve over the base case.
  */
case class FcfsEvelator(id: Int, capacity: Int, maxFloor: Int) extends Elevator {

  // Assumes all elevators are installed and start at floor 1.  Could be taken in as a parameter, simpler this way.
  var status: ElevatorStatus = ElevatorStatus(id, 1)

  override def receive: Actor.Receive = {
    case request: ElevatorRequest => { claim(request) }
    case statusRequest: ElevatorStatusRequest => { report(statusRequest) }
    case stepRequest: ElevatorStepRequest => { step(stepRequest) }
    case t => println(s"I've received a message I don't understand ${t}")
  }

  override def claim(request: ElevatorRequest): Boolean = {
    status.request match {
      case None => {
        request.callback.accept(id) match {
          case true => {
            status.floor match {
              case request.floor => true// perform pickup
              case _ => {
                println(s"Claiming request as elevator id ${id}") // this is my cheap logging
                status = status.copy(request = Some(request),
                  direction = (if (status.floor < request.floor) Some(UP) else Some(DOWN)))
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
    statusRequest.callback.report(status)
  }

  override def step(elevatorStepRequest: ElevatorStepRequest) = {
    status.direction match {
      case Some(UP) => status = moveFloor(1)
      case Some(DOWN) => status = moveFloor(-1)
      case None => // do nothing
    }
  }

  def moveFloor(adjustment: Int): ElevatorStatus = {
    val newFloor: Int = status.floor + adjustment
    status match {
      case ElevatorStatus(`id`, _, _, Some(`newFloor`), _) => {
        status.copy(floor = newFloor, direction = None, destination = None, request = None)
      }
      case ElevatorStatus(`id`, _, _, _, Some(ElevatorRequest(_, `newFloor`, _, _))) => {
        status.copy(floor = newFloor, direction = None)
      }
      case _ => status.copy(floor = newFloor)
    }
  }
}
