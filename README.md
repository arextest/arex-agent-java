# Arex Java Agent
Trace-based testing with JavaAgent：open source traffic playback test tool.
* [About](#About)
* [Installation](#Installation)
* [Getting Started](#Getting Started)
* [Contributing](#Contributing)
* [License](#License)

## About

As your application grows, the effort required to test it also grows exponentially. AREX offer you the simple idea of using your existing traffic for testing, witch makes it incredibly powerful.\
This project provides a Java agent JAR that can be attached to any Java 8+ application and dynamically injects bytecode to record your live traffic, and use it for shadowing, testing, and debugging.\
AREX offers a unique approach for shadowing. Instead of being a proxy, AREX listens in the background for traffic on your network interfaces from a number of popular libraries and frameworks, requiring no changes in your production infrastructure.

The AREX Java Agent automatically instruments various APIs, frameworks and application servers. This section lists all supported technologies:
- Java Executors
- Apache HttpAsyncClient 4.x
- Apache HttpClient 4.x
- Hibernate 5.x
- MyBatis 3.x
- Spring Boot 1.4+-2.x+, Servlet API 3+

## Installation

Download the latest binary from https://github.com/arextest/releases or compile by yourself.\
Their will be two JARs in the arex-agent-jar folder:
```
 arex-agent-<version>.jar
 arex-agent-bootstrap-<version>.jar
```
These two JARs must be placed in the same directory.

## Getting Started
You can start arex by：
### Enable the instrumentation agent using the `-javaagent` flag to the JVM, and run in local mode:
```
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
The supported commands are as follows:
- **replay** replay recorded data and view differences  
  `[option: -n/--num]` replay numbers, default the latest 10
- **watch** view replay result and differences  
  `[option: -r/--replayId]` replay id, multiple are separated by spaces
- **debug** local debugging of specific cases  
  `[option: -r/--recordId]` record id, required Option


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
