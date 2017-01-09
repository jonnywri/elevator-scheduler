package org.elevator.scheduler

import scala.concurrent.Future

/**
  * Basic definition of an elevator control system.  Step is the complex method, especially if you wanted to treat
  * the simulation like
  */
trait ElevatorControlSystem {
  /**
    * @return current status of all elevators within the system
    */
  def status(): Iterable[ElevatorStatus]

  /**
    * Creates a new elevator with an initial capacity, assumes starting floor and direction.
    * @param capacity
    */
  def create(capacity: Int)

  /**
    * Updates an individual elevator (could be used for implementing something like a maintenance mode).
    *
    * Honestly, this is a bad mechanism to allow for elevator updates, as you really only want to support updating
    * a few attributes (such as total capacity or maintenance).  Tradeoff here is simplicity of API versus
    * clarity of API.
    *
    * @param elevatorId id of the elevator to be updated
    * @param config new attributes to be configured for a given elevator
    */
  def update(elevatorId: Int, config: ElevatorConfig)

  /**
    * Registers a request object with a floor for the call and direction to go.
    *
    * @param request elevator request object
    * @return identifier of the elevator serving the request
    */
  def pickup(request: ElevatorRequest): Future[Option[Int]]

  /**
    * Steps forward the simulation - in some implementations this method may be no-op.
    */
  def step()

  /**
    * Shutsdown the current elevator control system.
    */
  def shutdown()
}
