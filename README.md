# Metaheuristics in Java

This is a collection of instructive implementations and applications of metaheuristic algorithms in Java that arose from the coursework for the "_Solving Optimization Problems Using Evolutionary Computation Algorithms in Java_" course at the [Faculty of Electrical Engineering and computing](http://www.fer.unizg.hr) of the University of Zagreb in 2010.

The following algorithms are implemented:

* __Generation Elite Genetic Algorithm__: a simple, basic version that can easily be extended and applied directly to different problems with a genotypic representation.
* __Steady State Evolution Strategy__: a specific implementation using real vectors as individuals for finding the extremum of a given function in N dimensions.
* __Relevant Alleles Preserving Genetic Algorithm (RAPGA)__: a simple implementation whose operators can easily be extended. Applicable to any problem that can be represented genotypically.
* __Offspring Selection Genetic Algorithm__: a specific implementation for solving a relatively complex scheduling problem. A concurrent version is also available, for comparison and an example of how it's supposed to be done.
* __Ant Colony System (ACO) algorithm__: a specific implementation for solving the _Travelling Salesman Problem_.
* __Clonal Selection Algorithm (CLONALG)__: also a specific implementation for solving the _Travelling Salesman Problem_.
* __Particle Swarm Optimization (PSO) algorithm__: a specific implementation using real vectors as individuals for finding the extremum of a given function in N dimensions.

* __Differential Evolution__ (with 6 different strategies): _in working_
* five different __multimodal genetic algorithm models__ (_Crowding, Deterministic Crowding, Fitness Sharing, Mating Restriction, Preselection_): _in working_
* __NSGA-II__: a multiobjective genetic algorithm _in working_
* __MISA__: a multiobjective AIS algorithm _in working_

The algorithm implementations are used on specific problems in applications located in the "test" directory. Most of those applications feature both a console and a graphical interface, all whose problems are such that it makes sense to present their solutions graphically. Both the GUIs and console applications offer interactivity and various options that make it possible to play with the algorithms without rebuilding.

## This project as a library

The project has four main packages:
* ```algorithms``` - contains algorithm implementations,
* ```solutions``` - contains representations of solutions,
* ```utils``` - contains classes that define problems and can handle solutions,
* ```functions``` - contains representations of functions to find extremes of.

The central class in the ```algorithms``` package is the ```OptimisationAlgorithm``` abstract class. It provides the extending class with thread-safe interactive capabilities (run, pause, resume & stop) and a listener class that can be added for listening to any (relevant) change in the algorithm state, the finding of a new best solution and the finishing of the algorithm. These capabilities are ideal for using in a GUI setting, but also for a console application, and they enable the user to customise the output of the algorithm as it is running so only the logic is contained in the implementation code.
An implementation of an ```OptimisationAlgorithm``` only has to provide the bodies of three methods which define it completely: algorithmStart, algorithmStep & algorithmEnd.
Currently all the implementations are hardcoded, to an extent, to solve specific problems or classes of specific problems, but it is my intention to disentangle algorithms from specific problems completely in the near future, which would make this project more like a library. The idea will be for the user to extend a given algorithm implementation providing it with the necessary ```Solution``` and ```SolutionUtil``` classes and implementations of the problem-specific methods. Examples of that are the current implementations.

The central class in the ```solutions``` package is the ```Solution``` abstract class which is the tamplate class of the ```OptimisationAlgorithm```. It provides a ```fitness``` and a way to compare solutions by fitnesses, which is a basic thing all solutions share, no matter what the problem or the algorithm. There are a few basic implementations offered - BinarySolution, VectorSolution, TSPSolution - but the general idea is for the users to extend the basic class and make their own problem-specific implementation (e.g. the ```Schedule``` class).

The ```utils``` package contains the ```SolutionUtil``` interface and some standard implementations of it - e.g. BinaryUtil, VectorUtil, TSPUtil - that basically represent the problem being solved. A ```SolutionUtil``` implementation will contain all the necessary information for generating and evaluating the corresponding ```Solution``` implementation instances.

Finally, the ```functions``` package contains the ```Function``` interface which is to be implemented for the use of any function in the library. Two implementations of functions standardly used in testing optimisation/metaheuristic algorithms are provided - the Rastrigin and Schwefel functions.

## TODO

1. Add DE, multimodal, NSGA-II and MISA
1. disentangle algorithms and specific problems - make examples/applications for problems
1. make GUI for scheduling problem and max-ones
1. Give GUIs a parameter dialog and an options dialog
1. provide proper console argument handling (JCommander)
1. add Logging (instead of messages to System.out/err)
