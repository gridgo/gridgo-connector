# gridgo-connector

Gridgo Connector is the I/O abstraction level of GridGo. It provides easy-to-use I/O connector for various protocols, including Kafka, ZMQ, VertX, etc. Connector consists of Producer and Consumer.

### installation

```xml
<dependency>
    <groupId>io.gridgo</groupId>
    <artifactId>gridgo-connector-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

### getting started

To start using GridGo Connector, you need to provide an *endpoint*, which is a string specifying the scheme of the protocol and its parameters. For example, following is a Kafka endpoint:

```
kafka:someTopic?brokers=127.0.0.1:1234&groupId=test
```

The syntax of each endpoint will depends on its actual connector implementation, please refer to individual connector project to find out more. To create the connector, pass the endpoint to a `ConnectorFactory`:

```java
var factory = new DefaultConnectorFactory();
var connector = factory.createConnector("kafka:someTopic?brokers=127.0.0.1:1234&groupId=test");

// start the connector
connector.start();

// do something with the Connector
...

// stop the connector when you are done.
// notes that stopping the connector will 
// also stop the producer and consumer
connector.stop();
```

### using Connector

To do something useful, you need to access Consumer and Producer from the Connector. Based on the endpoint or the connector, some might only support Consumer or Producer. For example, Vertx HTTP will only support Consumer, not Producer. Again, refer to their pages to find out which are supported and which are not.

```java
// access the consumer and producer
var consumer = connector.getConsumer().orElseThrow();
var producer = connector.getProducer().orElseThrow();
```

Consumer and Producer will expose several API to interact with the I/O layer:

To listen for and handle incoming messages from consumer:

```java
consumer.subscribe(msg -> {
    // handle the incoming message
});
```

Some consumers will require a response, or acknowledgement from handler, e.g Vertx HTTP and Kafka. To send the response or acknowledgement back to the consumer, you need to use the subscribe(java.util.function.BiConsumer) method:

```java
consumer.subscribe((msg, deferred) -> {
    // handle the incoming message and fulfill the deferred
    deferred.resolve(someResponseMessage);
});
```

The producer provides 3 different APIs to send the message:

```java
// send the message with fire-and-forget style
producer.send(someMessage);

// send the message and wait for acknowledgement
// this is to check whether the message has been sucessfully sent
producer.sendWithAck(someMessage).done(ack -> {
    // message sent successfully. handle the acknowledgement
}).fail(ex -> {
    // message sent unsuccessfully. handle the exception
});

// send the message and wait for response
// this is actually a RPC calls
producer.call(someMessage).done(response -> {
    // message sent successfully. handle the response
}).fail(ex -> {
    // message sent unsuccessfully. handle the exception
});
```

Some producers might not support RPC calls and will throw `UnsupportedOperationException` if you try to do so.

### custom connectors

Internally, `ConnectorFactory` will use a `ConnectorResolver` to resolve an endpoint. `ClasspathConnectorResolver` is the default resolver. It will scan the *io.gridgo.connector* package for any `Connector` class annotated with `@ConnectorEndpoint`. To use a custom resolver you can pass it to either the constructor of `DefaultConnectorFactory` or `createConnector` method:

```java
var factory = new DefaultConnectorFactory(myResolver);
var connector = factory.createConnector(someEndpoint);
```

or

```java
var factory = new DefaultConnectorFactory();
var connector = factory.createConnector(someEndpoint, myResolver);
```

### continuous integration

**master**

[![Build Status](https://travis-ci.com/gridgo/gridgo-connector.svg?branch=master)](https://travis-ci.com/gridgo/gridgo-connector)
[![Coverage Status](https://coveralls.io/repos/github/gridgo/gridgo-connector/badge.svg?branch=master&maxAge=86400)](https://coveralls.io/github/gridgo/gridgo-connector?branch=master)

**develop**

[![Build Status](https://travis-ci.com/gridgo/gridgo-connector.svg?branch=develop)](https://travis-ci.com/gridgo/gridgo-connector)
[![Coverage Status](https://coveralls.io/repos/github/gridgo/gridgo-connector/badge.svg?branch=develop&maxAge=86400)](https://coveralls.io/github/gridgo/gridgo-connector?branch=develop)

### license

This library is distributed under MIT license, see [LICENSE](LICENSE)
