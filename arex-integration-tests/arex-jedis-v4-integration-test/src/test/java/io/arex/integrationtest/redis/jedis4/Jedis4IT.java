package io.arex.integrationtest.redis.jedis4;

import io.arex.agent.ArexJavaAgent;
import io.arex.agent.bootstrap.model.ArexConstants;
import io.arex.integrationtest.common.AbstractIT;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class Jedis4IT extends AbstractIT {
    static GenericContainer redisContainer;
    static int redisPort;
    @LocalServerPort
    int port;
    static TestRestTemplate restTemplate;

    @BeforeAll
    public static void before() {
        redisContainer = new GenericContainer("redis:6.2.6").withExposedPorts(6379);
        redisContainer.start();
        redisContainer.waitingFor(Wait.forLogMessage("Started!", 1));
        redisPort = redisContainer.getFirstMappedPort();

        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(10))
                .setReadTimeout(Duration.ofMillis(10))
                .basicAuthentication("username", "password"));

        ArexJavaAgent.init(ByteBuddyAgent.install(), getAgentJar("arex-agent"), getAgentJar("arex-agent-bootstrap"));
    }

    @AfterAll
    public static void tearDown() {
        redisContainer.stop();
        clearSystemProperties();
    }

    @Test
    void testShardedJedis() throws Exception {
        String requestUrl = "http://localhost:" + port + "/test";
        // test record
        ResponseEntity response = restTemplate.getForEntity(requestUrl, String.class);
        assertNotNull(response);
        assertNotNull(response.getHeaders());

        String recordId = response.getHeaders().getFirst(ArexConstants.RECORD_ID);
        String recordValue = String.valueOf(response.getBody());
        assertNotNull(recordId);

        checkDatabase(recordId);

        // test replay
        HttpHeaders headers = new HttpHeaders();
        headers.add(ArexConstants.RECORD_ID, recordId);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(headers);
        response = restTemplate.exchange(requestUrl, HttpMethod.GET, httpEntity, String.class);
        String replayValue = String.valueOf(response.getBody());
        // Normally, the value returned by each call is different, but if it is replay, the recorded value should be returned
        assertEquals(recordValue, replayValue);
    }

    static void checkDatabase(String recordId) throws Exception {
        StringBuilder sql = new StringBuilder("select * from MOCKER_INFO where category='4' and caseId='");
        sql.append(recordId).append("'");
        List<String> results = queryDB(sql.toString());
        assertTrue(results.stream().anyMatch(result -> result.contains("\\\"key\\\":\\\"foo\\\"")));
    }

    @RestController
    @SpringBootApplication
    public static class MyApp {
        Jedis jedis;

        public static void main(String[] args) {
            SpringApplication.run(MyApp.class, args);
        }

        @GetMapping("/test")
        public String test() {
            jedis = new JedisPool("localhost", redisPort).getResource();
            // the value returned by each call is different, but if replayed, the recorded value should be returned normally
            jedis.set("foo", UUID.randomUUID().toString());
            return jedis.get("foo");
        }

    }
}
