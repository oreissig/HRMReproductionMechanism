# HRMReproductionMechanism

[ ![Download](https://api.bintray.com/packages/oreissig/maven/HRMReproductionMechanism/images/download.svg) ](https://bintray.com/oreissig/maven/HRMReproductionMechanism/_latestVersion)
[![Build Status](https://travis-ci.org/oreissig/HRMReproductionMechanism.svg)](https://travis-ci.org/oreissig/HRMReproductionMechanism)
[![codecov](https://codecov.io/gh/oreissig/HRMReproductionMechanism/branch/master/graph/badge.svg)](https://codecov.io/gh/oreissig/HRMReproductionMechanism)

An implementation of the [Human Resource Machine](http://tomorrowcorporation.com/humanresourcemachine) programming language by [Tomorrow Corporation](http://tomorrowcorporation.com).

### Usage

Prerequisites:
* Java Runtime Environment 1.7 or newer
* *optional* [Human Resource Machine Soundtrack](http://tomorrowcorporation.com/human-resource-machine-soundtrack) playing in the background

Download the zip (Windows) or tgz (Linux) file from Bintray, extract it and call the start script in the `bin` directory, e. g. `HRMReproductionMechanism YourSource.hrm`.

You can also execute the interpreter directly from source: `gradlew run -Pargs=YourSource.hrm`.

### Language Guide

* `INBOX` reads the next hand value from stdin
* `OUTBOX` writes the current hand value to stdout
* `COPYFROM #ADDR` reads a hand value from the given floor tile
* `COPYTO #ADDR` stores the current hand value in the given floor tile
* `ADD #ADDR` adds the given floor tile's value to the current hand value
* `SUB #ADDR` subtracts the given floor tile's value from the current hand value
* `BUMPUP #ADDR` increses the given floor tile's value by one
* `BUMPDN #ADDR` decreses the given floor tile's value by one
* `#LABEL:` defines a label you can jump to
* `JUMP #LABEL` continues execution at the given label
* `JUMPZ #LABEL` continues execution at the given label only if the current hand value is zero
* `JUMPN #LABEL` continues execution at the given label only if the current hand value is negative
* `--` or `COMMENT` mark the beginning of a new comment line
* An address `#ADDR` may be specified either directly as a `#NUMBER` or indirectly via `[#NUMBER]`, in which case the value of floor tile `#NUMBER` will be used as address.

### Differences

* As replacement for the graphical representation of jump targets you can declare labels and use those to designate a jump's destination.
* Binary content e.g. comments and custom floor tile names are ignored.
* Lots of tiles are available on the floor, per default there are 9000.
* You can enable _literal mode_ with `-Dliteral=true`, which preinitializes all floor tiles with their their index. This allows you to `COPYFROM 23` to get _23_ into your hands, as long as you haven't put anything on tile 23.
* The instruction `DUMP` will print debugging information.

### Details

_Implemented:_
* ANTLRv4 based parser
* direct interpreter
* nice stack trace in case of error

_TODO:_
* optimizations
* compiler
* marketing brochures
