# Scala Futures
Simple project to play around with and understand how Scala futures execute.

## How To Run The Project
Not surprisingly, this project uses [SBT](https://www.scala-sbt.org). You can build and run it using the usual SBT
comands.

* Compile - `compile`
* Run tests - `test` 
* Run the bee hive progam - `runMain hive.QeenBee` (see below for more detail)

## What Is This Project?
Recently my team at [Yoco Technologies](https://www.yoco.co.za) were working on a new server-side component that needed 
to process messages from SQS in a single-threaded manner, i.e. one at a time. This was in contrast to many other
services we've built and worked with where this kind of ordering did not work.

We had basic familiarity with doing multi-threaded coding in both Scala and Java, but we found that when we needed
to ensure that we were processing messages from SQS one at a time by polling for them, the way to do this with
standard [Scala futures](https://docs.scala-lang.org/overviews/core/futures.html) was not altogether obvious.

Thus, this project is really intended to be something that you load up into your IDE and play around with. As of this
writing there are 2 main ways you can do this.

### Java Worker Pool Sample
In the `src/main/java` tree, there is a simple worker pool application. It simulates a simple bee hive where there
is one queen bee and then N worker bees who are tasked with finding flowers for the hive. The `main` method is in
`hive.QueenBee`. There you can see concurrency can be controlled simply by using a thread pool with a single thread.
To make the hive work in parallel, simply switch to using a thread pool that allows concurrent tasks.

The intent of the bee hive is to demonstrate that in Java, concurrency control has some pretty coarse-grained

### Scala Futures Sample
This is where I wanted to show a bit more complexity. The easiest way to run the code is to run the tests in
the `test/scala/tests` tree. This code exercises what I've written in `main/scala` which is described as follows.

The Scala code simulates a simple service that uses a repository layer to persist data. 

The repository layer just uses an in-memory hashmap for storage, so nothing fancy there. This repository layer provides
create, update, and find methods. These methods return Scala futures to simulate a real persistence layer that
returns its results asynchronously.

The service layer uses the repository layer to perform "upserts", i.e. when it needs to persist an object, it first
queries the repository layer and if it finds the object, asks the repository layer to update it. If it doesn't exist,
it uses the repository layer to create the object. Note that the service layer also uses Scala futures, largely
dictated by the fact that the repository layer is asynchronous.

Note that the repository and service components each take their own `ExecutionContext` objects. These are used by
the repository and service code to control the execution of the Scala future objects.

In the tests, particularly, the `ServiceSpec` class, we then show how to correctly, and incorrectly, get the desired
single-threaded execution of the service and repository layers. These are intended to show our journey to get this to 
work correctly.

Feel free to clone/fork this code. There's nothing proprietary or, for the advanced Scala developer, perhaps even
new here. But I hope that it helps provide a way for Scala developers to experiment with its sometimes non-obvious
concurrency model.