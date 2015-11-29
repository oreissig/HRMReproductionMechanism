# HRMReproductionMechanism

[![Build Status](https://travis-ci.org/oreissig/HRMReproductionMechanism.svg)](https://travis-ci.org/oreissig/HRMReproductionMechanism)
[![Dependency Status](https://www.versioneye.com/user/projects/563a78991d47d40019000853/badge.svg?style=flat)](https://www.versioneye.com/user/projects/563a78991d47d40019000853)

An implementation of the [Human Resource Machine](http://tomorrowcorporation.com/humanresourcemachine) programming language by [Tomorrow Corporation](http://tomorrowcorporation.com).

### Usage

To interpret a HRM program just run `gradlew run -Pargs=YourSource.hrm`.

Executing `gradlew build` will generate a stand-alone distribution into `build/distributions`.

### Language Guide

* `INBOX` reads the next hand value from stdin
* `OUTBOX` writes the current hand value to stdout
* `COPYFROM #NUM` reads a hand value from the given floor tile
* `COPYTO #NUM` stores the current hand value in the given floor tile
* `ADD #NUM` adds the given floor tile's value to the current hand value
* `SUB #NUM` subtracts the given floor tile's value from the current hand value
* `BUMPUP #NUM` increses the given floor tile's value by one
* `BUMPDN #NUM` decreses the given floor tile's value by one
* `#LABEL:` defines a label you can jump to
* `JUMP #LABEL` continues execution at the given label
* `JUMPZ #LABEL` continues execution at the given label only if the current hand value is zero
* `JUMPN #LABEL` continues execution at the given label only if the current hand value is negative
* `...` marks the beginning of a new comment line

### Differences

* As replacement for the graphical representation of jump targets you can declare labels and use those to designate a jump's destination.
* Lots of tiles are available on the floor, per default there are 9000.
* You can enable _literal mode_ with `-Dliteral=true`, which preinitializes all floor tiles with their their index. This allows you to `copyfrom 23` to get _23_ into your hands, as long as you haven't put anything on tile 23.
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
