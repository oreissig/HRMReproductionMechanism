# HRMReproductionMechanism

[![Build Status](https://travis-ci.org/oreissig/HRMReproductionMechanism.svg)](https://travis-ci.org/oreissig/HRMReproductionMechanism)

An implementation of the [Human Resource Machine](http://tomorrowcorporation.com/humanresourcemachine) programming language by [Tomorrow Corporation](http://tomorrowcorporation.com).

### Language Guide

* `inbox` reads the next datum from stdin
* `outbox` writes the current datum to stdout
* `copyfrom #NUM` reads a datum from the given memory cell
* `copyto #NUM` stores the current datum in a given memory cell
* `add #NUM` adds the given memory cell to the current datum
* `sub #NUM` subtracts the given memory cell from the current datum
* `bump+ #NUM` increses the given memory cell's datum by one
* `bump- #NUM` decreses the given memory cell's datum by one
* `:#LABEL` defines a label you can jump to
* `jump #LABEL` continues execution at the given label
* `jump if zero #LABEL` continues execution at the given label only if the current datum is zero
* `jump if negative #LABEL` continues execution at the given label only if the current datum is negative
* `...` marks the beginning of a new comment line

### Differences

* As replacement for the graphical representation of jump targets you can declare labels and use those to designate a jump's destination.
* Lots of memory slots are available, per default there are 9000.
