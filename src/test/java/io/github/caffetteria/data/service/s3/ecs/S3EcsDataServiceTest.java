package io.github.caffetteria.data.service.s3.ecs;

import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.simple.config.ConfigParams;
import org.fugerit.java.simple.config.ConfigParamsDefault;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class S3EcsDataServiceTest {

    private static final String TEST_BUCKET = "test-bucket";

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(S3);

    private S3EcsDataService dataService;

    @BeforeEach
    void setUp() throws ConfigException {
        // Prepara la configurazione per il DataService
        Properties properties = new Properties();
        properties.setProperty(S3EcsDataService.BUCKET_NAME, TEST_BUCKET);
        properties.setProperty(S3EcsDataService.ENDPOINT, localstack.getEndpointOverride(S3).toString());
        properties.setProperty(S3EcsDataService.ACCESS_KEY, localstack.getAccessKey());
        properties.setProperty(S3EcsDataService.SECRET_KEY, localstack.getSecretKey());
        properties.setProperty(S3EcsDataService.CREATE_IF_NOT_EXISTS, "true");
        log.info( "Setting up S3EcsDataService with properties {}", properties );
        ConfigParams configParams = new ConfigParamsDefault(properties);
        // Inizializza il DataService
        dataService = S3EcsDataService.newDataService(configParams);
    }

    @Test
    @Order(1)
    @DisplayName("Test save and load")
    void testSaveLoadBasic() throws IOException {
        // Given
        String content = "Test basic write / load";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        // When
        String id = dataService.save(inputStream);
        String load = StreamIO.readString( dataService.load( id ) );
        Assertions.assertEquals(content, load);
    }

    @Test
    @Order(2)
    @DisplayName("Test save and load with resource name")
    void testSaveLoadBasicWithResourceName() throws IOException {
        // Given
        String content = "Test basic write / load with resource name";
        String resourceName = "test-resource-name";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        // When
        String id = dataService.save(inputStream, resourceName);
        String load = StreamIO.readString( dataService.load( id ) );
        Assertions.assertEquals(content, load);
    }

    @AfterEach
    void tearDown() {
        // Cleanup se necessario
    }
}