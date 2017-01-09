package org.elevator.scheduler

/**
  * Simplest facsimile of an enumeration in Scala.
  */
sealed trait Direction { def name: String }
case object UP extends Direction { val name = "UP" }
case object DOWN extends Direction { val name = "DOWN" }