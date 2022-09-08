package io.arex.integrationtest.dynamic;

import io.arex.agent.ArexJavaAgent;
import io.arex.foundation.model.Constants;
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

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class DynamicClassIT extends AbstractIT  {
    @LocalServerPort
    int port;
    static TestRestTemplate restTemplate;

    @BeforeAll
    public static void before() {
        System.setProperty("arex.dynamic.class", "io.arex.integrationtest.dynamic.DynamicClassIT$TestApp#testDynamic#int");

        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(10))
                .setReadTimeout(Duration.ofMillis(10))
                .basicAuthentication("username", "password"));

        ArexJavaAgent.init(ByteBuddyAgent.install(), getAgentJar("arex-agent"), getAgentJar("arex-agent-bootstrap"));
    }

    @AfterAll
    public static void tearDown() {
        clearSystemProperties();
        System.clearProperty("arex.dynamic.class");
    }

    @Test
    void testDynamic() throws Exception {
        String requestUrl = "http://localhost:" + port + "/test";
        // test record
        ResponseEntity response = restTemplate.getForEntity(requestUrl, String.class);
        // ensure data has been saved into the database
        TimeUnit.SECONDS.sleep(1);
        assertNotNull(response);
        assertNotNull(response.getHeaders());

        String recordId = response.getHeaders().getFirst(Constants.RECORD_ID);
        String recordValue = String.valueOf(response.getBody());
        assertNotNull(recordId);

        checkDatabase(recordId);

        // test replay
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.RECORD_ID, recordId);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(headers);
        response = restTemplate.exchange(requestUrl, HttpMethod.GET, httpEntity, String.class);
        String replayValue = String.valueOf(response.getBody());
        // Normally, the value returned by each call is different, but if it is replay, the recorded value should be returned
        assertEquals(recordValue, replayValue);
    }

    static void checkDatabase(String recordId) throws Exception {
        StringBuilder sql = new StringBuilder("select * from MOCKER_INFO where category='5' and caseId='");
        sql.append(recordId).append("'");
        List<String> results = queryDB(sql.toString());
        assertTrue(results.stream().anyMatch(result -> result.contains("testDynamic")));
    }

    @RestController
    @SpringBootApplication
    public static class TestApp {

        public static void main(String[] args) {
            SpringApplication.run(TestApp.class, args);
        }

        @GetMapping("/test")
        public String test() {
            return testDynamic(1);
        }

        /**
         * the value returned by each call is different, but if replayed, the recorded value should be returned normally
         */
        private static String testDynamic(int num) {
            return UUID.randomUUID().toString();
        }
    }

}
