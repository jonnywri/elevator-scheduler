# elevator-scheduler
Scala elevator scheduler example.

## Overview

This project represents a Scala implementation of a basic elevator system.  This demonstrates how a control system
would integrate with a system of actors to use message passing to achieve how an elevator system might be built.

The basics of this system are the ElevatorControlSystem itself, as well as the Elevator.  These two traits define
the basis of how any system should be implemented.

### SimpleEcs

The SimpleEcs is built using a FcfsElevator which assumes the elevator will try to grab the first request that comes
through but will not service any other until they reach their destination.

## How to run

Project is built with sbt and all standard build commands will execute.

## How to develop

Currently project has been built using IntelliJ, but any IDE which supports SBT integration should be able to import.
