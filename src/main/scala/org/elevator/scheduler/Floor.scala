package org.elevator.scheduler

import akka.actor.Actor
import akka.routing.{BroadcastRoutingLogic, Routee, Router}

import scala.collection.immutable

/**
  * Representation of a floor, from which elevator requests are generated.
  */
case class Floor(id: Int) extends Actor {
  var router: Router = Router(BroadcastRoutingLogic())

  override def receive: Actor.Receive = {
    case t => println(s"I've received a message I don't understand ${t}")
  }
}
