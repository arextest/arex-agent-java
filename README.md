[![Build Status](https://github.com/arextest/arex-agent-java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/arextest/arex-agent-java/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/arextest/arex-agent-java/branch/main/graph/badge.svg)](https://app.codecov.io/gh/arextest/arex-agent-java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=arextest_arex-agent-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=arextest_arex-agent-java)

# <img src="https://avatars.githubusercontent.com/u/103105168?s=200&v=4" alt="Arex Icon" width="27" height=""> AREX

#### An Open Source Testing Framework with Real World Data

- [Introduction](#introduction)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [License](#license)

## Introduction


As your application evolves more complex, the effort required to thoroughly test against it also becomes tremendous. Arex is a framework designed around a quite straightforward principle of leveraging your real world data(i.e. database record, service payload, cache items etc.) for regression testing. Simple is powerful. The idea behind makes it incredibly powerful.

AREX provides an out-of-box agent file that could be attached to any applications with Java 8+ and dynamically weaves solid  bytecode into your existing code to record the real data of live traffic, and further use and replay it for mocking, testing, and debugging purpose.

AREX is implemented with an unique mechanism for recording. Instead of being a proxy like other similar framework, `AREX` sits in the background without awareness of your application to record realistic data in live traffic which means that no intrusive code changes are required when integrating it to your existing application.

AREX utilizes the advanced Java technique, Instrument API, and is capable of instrumenting various libraries and frameworks which are widely used.

**Libraries and frameworks supported by Arex (to be added...)**
[maven version range rules](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html)

#### Foundation
- Java Executors
- System time
- Dynamic Type
#### Spring
- Spring Boot [1.4+, 2.x+]
- Servlet API 3+、5+
#### Http Client
- Apache HttpClient [4.0,)
- OkHttp [3.0, 4.11]
- Spring WebClient [5.0,)
- Spring Template
#### Redis Client
- Jedis [2.10+, 4+]
- Redisson [3.0,)
- Lettuce [5.x, 6.x]
#### Persistence framework
- MyBatis 3.x, MyBatis-Plus, TkMyBatis
- Hibernate 5.x
#### NoSQL
- MongoDB [3.x, 4.x]
#### RPC
- Apache Dubbo [2.x, 3.x]
- Alibaba Dubbo 2.x
#### Auth
- Spring Security 5.x
- Apache Shiro 1.x
- JCasbin 1.x
- Auth0 jwt 3.x
- JWTK jjwt 0.1+、jjwt-api 0.10+
#### Netty
- Netty server 4.1+
#### Config
- Apollo Config [1.x, 2.x]
## Installation


Simply download the latest binary from [github](https://github.com/arextest/arex-agent-java/releases) 
or compile it with `mvn clean package -DskipTests` by yourself.

There are two agent files provided in the arex-agent-jar folder like below. They must be placed in the same directory.

```other
arex-agent.jar
arex-agent-bootstrap.jar
```

If you need these jar with version, you can add option: `mvn clean package -DskipTests -Pjar-with-version` 
```other
arex-agent-<version>.jar
arex-agent-bootstrap-<version>.jar
```


## Getting Started


***Enable the instrumentation agent by configuring a `javaagent` flag to the JVM to run arex：***

AREX agent works along with the [AREX storage service](https://github.com/arextest/arex-storage).

You could just configure the host and port of them respectively, like below

```other
java -javaagent:/path/to/arex-agent.jar
      -Darex.service.name=your-service-name
      -Darex.storage.service.host=[storage.service.host:port](storage.service.host:port) 
      -jar your-application.jar
```


Alternatively, you can put those configuration item in `arex.agent.conf` file, like below

```other
arex.service.name=your-service-name  
arex.storage.service.host=<storage.service.host:port> 
```


Then simply run:

```other
java -javaagent:/path/to/arex-agent.jar
      -Darex.config.path=/path/to/arex.agent.conf
      -jar your-application.jar
```


***Also, You can Run with CLI in local mode:***

Please refer to : [AREX standalone mode](https://github.com/arextest/arex-standalone).



## Contributing

1. Fork it
2. Create your feature branch
3. Commit your code changes and push to your feature branch
4. Create a new Pull Request


## License
- Code: [Apache-2.0](https://github.com/arextest/arex-agent-java/blob/main/LICENSE)
