<h1 align="center" style="border-bottom: none">
    <b>
        <a href="">Arex</a><br>
    </b>
</h1>

##### An Open Source Testing Framework with Realistic Data

## Introduction

As your application evolves more complex, the effort required to thoroughtly test against it also becomes tremendous. Arex is a framework designed around a quite straightforward principle of leveraging your realistic data(i.e. database record, service payload, cache items etc.) for regression testing. Simple is powerful. The idea behind makes it incredibly powerful.

Arex provides an out-of-box agent file that could be attached to any applications with Java 8+ and dynamically weaves solid  bytecode into your existing code to record the relistic data of live traffic, and further use and replay it for mocking, testing, and debugging purpose.

Arex is implemented with an unique mechanism for recording. Instead of being a proxy like other similar framework, `Arex` sits in the background without awareness of your application to record realistic data in live traffic which means that no intrusive code changes are required when integrating it to your exising application.

Arex utilizes the advanced Java technique, Instrument API, and is capable of instrumenting various libraries and framworks which are widely used.

##### Libraries and frameworks supported by Arex (to be added...) #####

- ###### Java Executors ######

- ###### Apache HttpAsyncClient 4.x ######

- ###### Apache HttpClient 4.x ######

- ###### Hibernate 5.x ######

- ###### MyBatis 3.x ######

- ###### Spring Boot 1.4+-2.x+, Servlet API 3+ ######



## Installation

Simply download the latest binary from [github]( https://github.com/arextest/releases) or compile it by yourself.\
There are two agent files provided in the `arex-agent-jar` folder like below. They must be placed in the same directory.

```java
arex-agent-<version>.jar
arex-agent-bootstrap-<version>.jar
```

## Getting Started

You can get arex started by：

##### Enable the instrumentation agent by configuring a `javaagent` flag to the JVM to run arex in local mode:

```java
 java -javaagent:/path/to/arex-agent-<version>.jar
      -Darex.service.name=your-service-name (should be unique)
      -Darex.storge.model=local
      -jar your-application.jar
```
By default, Arex use the local storage with h2 save the recorded data.

### Run with Command Tool
Click the script file in the arex-agent-java/bin directory to start the command line tool, or start it through the command line tool of the system with java command:
 ```
java -cp "/path/to/arex-client/target/arex-client-<version>-jar-with-dependencies.jar" io.arex.cli.ArexCli
 ```

### Run entire arex solution, see [arex-dev-ops](https://github.com/arextest/dev-ops/wiki):

AREX Agents work in conjunction with the [AREX config service](https://github.com/arextest/arex-config), [AREX storage service](https://github.com/arextest/arex-storage).
 ```
java -javaagent:/path/to/arex-agent-<version>.jar
        -Darex.service.name=your-service-name
        -Darex.storage.service.host=[storage.service.host:port](storage.service.host:port) 
        -Darex.config.service.host=[config.service.host:port](config.service.host:port)
        -jar your-application.jar
```

Also, you can add the agent configuration at the bottom of the `arex.agent.conf` file:
```
arex.service.name=your-service-name  
arex.storage.service.host=<storage.service.host:port> 
arex.config.service.host=<config.service.host:port> 
```
Then run:
 ```
 java -javaagent:/path/to/arex-agent-<version>.jar
      -Darex.config.path=/path/to/arex.agent.conf
 ```
## Contributing

1. Fork it
2. Create your feature branch
3. Commit your changes and push to the branch
4. Create new Pull Request

## License
- Code: [Apache-2.0](https://github.com/arextest/arex-agent-java/blob/LICENSE)
