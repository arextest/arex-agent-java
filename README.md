- arex-agent: agent start
- arex-agent-bootstrap：Loaded by BootStrapClassLoader, including core classes such as agent initialization and Context transfer
- agent-agent-core： Instrumentation install
- arex-instrumentation-foudation：arex framework，context、api
- arex-instrumentation：Instrumentation plugins
- arex-client: Command line tool, which can be replay locally
- arex-storage-extension: Storage service for recording, replay and difference data

## Manual setup with -javaagent flag  
  
### setup-generic  
  
-javaagent:/path/to/arex-agent-0.0.1.jar -Darex.service.name=your-service-name -Darex.storage.service.host=10.3.2.42:8093 -Darex.config.service.host=10.3.2.42:8091  

### setup-local
-javaagent:/path/to/arex-agent-0.0.1.jar -Darex.service.name=your-service-name -Darex.storage.mode=local  
  
### set arex.agent.conf  
  
-javaagent:/path/to/arex-agent-0.0.1.jar -Darex.config.path=/path/to/arex.agent.conf  
  
> arex.agent.conf  
  
arex.service.name=spring-petclinc  
arex.storage.service.host=10.3.2.42:8093  
arex.config.service.host=10.3.2.42:8091