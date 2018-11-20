# gridgo-connector

[![Maven Central](https://img.shields.io/maven-central/v/io.gridgo/gridgo-connector-core.svg?maxAge=604800)](http://mvnrepository.com/artifact/io.gridgo/gridgo-connector-core)
[![Javadocs](http://javadoc.io/badge/io.gridgo/gridgo-connector-core.svg)](http://javadoc.io/doc/io.gridgo/gridgo-connector-core)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Gridgo Connector is the I/O abstraction level of Gridgo. It provides easy-to-use I/O connector for various protocols, including Kafka, ZMQ, VertX, etc. Connector consists of Producer and Consumer.

## build status

View [build status](https://github.com/gridgo/gridgo-connector/wiki/build-status) for all branches

## install

```xml
<dependency>
    <groupId>io.gridgo</groupId>
    <artifactId>gridgo-connector-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

You need to install individual connector as needed. For example to install `gridgo-kafka`:

```xml
<dependency>
    <groupId>io.gridgo</groupId>
    <artifactId>gridgo-kafka</artifactId>
    <version>SAME_AS_GRIDGO_CONNECTOR_CORE_VERSION</version>
</dependency>
```

## getting started

Refer to the [getting started guide](https://github.com/gridgo/gridgo-connector/wiki/getting-started) for instructions

## license

This library is distributed under MIT license, see [LICENSE](LICENSE)
