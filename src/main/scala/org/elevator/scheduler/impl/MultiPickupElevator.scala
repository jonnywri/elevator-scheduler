package org.elevator.scheduler.impl

import org.elevator.scheduler._

/**
  * Implements a basic system under which elevators will check during the claim process to try seeing if they can
  * pickup the request on their way to service another request.
  *
  * This implementation will not necessarily pickup passengers in the order the request comes, but should be an
  * overall improvement in total throughput of the elevator system in most cases.
  */
case class MultiPickupElevator(id: Int, capacity: Int = 1) extends Elevator {
  override def receive = ???
  override def claim(request: ElevatorRequest) = ???
  override def report(statusRequest: ElevatorStatusRequest): Unit = ???
  override def step = ???
}