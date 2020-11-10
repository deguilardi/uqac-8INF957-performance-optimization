# Work: Performance optimization

8INF957: Advanced object programming

Original french instructions file can be found [here](https://raw.githubusercontent.com/deguilardi/uqac-8INF957-travail-3/master/assets/cas_08_optimiseur_performance.pdf).

## The scenario

A system performs the processing of requests from various supplier (F). Each supplier sends their requests asynchronously and each request is inserted and stored in a transformer (T).

The system has a simple operation (performed in a loop)
* Receive a request in Tx;
* Process Tx;
* Return an response Rx to Tx;
* Go to Tx + 1;
* ...

The problem is that Tx can only contain a certain amount of requests (buffer) and when the maximum capacity is reached, it rejects new requests.

Your goal is to create a system that will optimize the treatment according to the state of the system and therefore improve its efficiency.

## Details

### Supplier "F"

* Each supplier is a Thread.
* It performs the following operations:
     * Generate requests (variable processing time - Sleep with random);
     * Receive responses / errors;
     * Log events (ex: if launched in Debug mode, written in the console).

### The "T" transformer

* Tx is associated with Fx.
* A transformer may contain a limited buffer capacity.
     * For example, with a capacity of 10.
     * IDLE (0), OK (3), DANGER (7) AND MAX (10).
* Any request exceeding MAX is automatically rejected and returns a failure to Fx.
* Each T is an object which can perform the following functions:
     * Receive a request;
     * Return a response / errors.

### The "S" system

* Composed of a list of transformers.
* The system always starts with the S<sub>default</sub> processing strategy.
* After a certain number of treatments (to be determined), the system falls into ``maintenance`` and displays performance statistics (see below).

## Solutions

**Project # 1**: Create a project that implements the default strategy and display the statistics (run multiple times to get a significant average).

**Project # 2**: Create a project that implements strategies that optimize system performance and display statistics (run multiple times to get a significant average).

## Calculation of performance statistics

Here are the costs

* For each treatment by the system:
    * S<sub>default</sub>: 1
    * D<sub>imbalance</sub>: 2
    * S<sub>overload</sub>: 3
* For each rejection: 25

Therefore, we must calculate the cost of treatment for all the operations of the system between the moment of start-up and the stop for maintenance.

## The strategies

* Default strategy (S<sub>default</sub>): The system is running in loops. See the scenario.
* Strategy by unbalancing (D<sub>imbalance</sub>): When a Tx is fuller than the other T, then we re-establish the equilibrium then we return to S<sub>default</sub>.
* Overload strategy (S<sub>overload</sub>): When a Tx is dangerously full, it is emptied until OK then it returns to S<sub>default</sub>.
* Any other strategy that you think is appropriate.
* You must find mechanisms for managing the strategies as objectively as possible! (a state machine?).

## Constraints

* The system must be object oriented.
* The objective is to optimize the system and to be able to compare advantages with the default mode.
* The execution time is not the unit of measurement but the processing cost.
* For the development of the prototype:

    * Balance the amount of Tx and the speed of processing requests to have significant results
    * It is possible that the outputs have a significant impact on the execution of your program (with fairly low "sleeps") so it would be preferable to limit the outputs to the maximum (use a "debug" mode).
    * Watch out for "synchronized" methods (yes and no, no matter where).