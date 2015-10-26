# HRMReproductionMechanism

[![Build Status](https://travis-ci.org/oreissig/HRMReproductionMechanism.svg)](https://travis-ci.org/oreissig/HRMReproductionMechanism)

An implementation of the [Human Resource Machine](http://tomorrowcorporation.com/humanresourcemachine) programming language by [Tomorrow Corporation](http://tomorrowcorporation.com).

### Usage

To interpret a HRM program just run `gradlew run -Pargs=YourSource.hrm`.

### Language Guide

* `inbox` reads the next hand value from stdin
* `outbox` writes the current hand value to stdout
* `copyfrom #NUM` reads a hand value from the given floor tile
* `copyto #NUM` stores the current hand value in the given floor tile
* `add #NUM` adds the given floor tile's value to the current hand value
* `sub #NUM` subtracts the given floor tile's value from the current hand value
* `bump+ #NUM` increses the given floor tile's value by one
* `bump- #NUM` decreses the given floor tile's value by one
* `:#LABEL` defines a label you can jump to
* `jump #LABEL` continues execution at the given label
* `jump if zero #LABEL` continues execution at the given label only if the current hand value is zero
* `jump if negative #LABEL` continues execution at the given label only if the current hand value is negative
* `...` marks the beginning of a new comment line

### Differences

* As replacement for the graphical representation of jump targets you can declare labels and use those to designate a jump's destination.
* `inbox` and `outbox` operate on ASCII values.
* Lots of tiles are available on the floor, per default there are 9000.
