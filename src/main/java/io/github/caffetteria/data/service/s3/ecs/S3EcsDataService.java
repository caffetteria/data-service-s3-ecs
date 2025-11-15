package io.github.caffetteria.data.service.s3.ecs;

import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.emc.object.s3.request.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.lang.helpers.BooleanUtils;
import org.fugerit.java.dsb.DataService;
import org.fugerit.java.simple.config.ConfigParams;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public class S3EcsDataService implements DataService {

    public static final String BUCKET_NAME = "bucketName";

    public static final String ENDPOINT = "endpoint";

    public static final String ACCESS_KEY = "accessKey";

    public static final String SECRET_KEY = "secretKey";

    public static final String CREATE_IF_NOT_EXISTS = "createIfNotExists";

    private S3Client s3Client;

    private Supplier<String> resourceIdProvider;

    /**
     * Crea un nuovo id di risorsa semplicemente con un UUID.randomUUID()
     */
    public static final Supplier<String> DEFAULT_RESOURCE_ID_PROVIDER = () -> UUID.randomUUID().toString();

    /**
     * Crea un nuovo id di risorsa con un UUID.randomUUID() e un timestamp
     */
    public static final Supplier<String> ALTERNATIVE_RESOURCE_ID_PROVIDER = () -> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = sdf.format(new Date(System.currentTimeMillis()));
        return String.format("%s_%s", timestamp, UUID.randomUUID());
    };


    /**
     * Assegna un nuovo resource id provider
     * @param resourceIdProvider        il nuovo resource id provider
     * @return                          il data service
     */
    public S3EcsDataService withResourceIdProvider( Supplier<String> resourceIdProvider ) {
        this.resourceIdProvider = resourceIdProvider;
        return this;
    }

    /**
     * Save a data stream in the S3, a UUID is generated as resource name.
     *
     * @param data          the data stream to be saved
     * @param resourceName  the resource name to save
     * @return              the id of the saved resources
     * @throws IOException if Input/Output issues arise
     */
    @Override
    public String save(InputStream data, String resourceName) throws IOException {
        PutObjectRequest request = new PutObjectRequest(this.bucketName, resourceName, data);
        this.s3Client.putObject(request);
        log.info( "save resource name {}", resourceName );
        return resourceName;
    }

    private String bucketName;

    /**
     * Load a data stream from the S3.
     *
     * @param id            the id of the resource to be loaded
     * @return              the loaded resource data stream
     * @throws IOException  if Input/Output issues arise
     */
    @Override
    public InputStream load(String id) throws IOException {
        return this.s3Client.getObject( this.bucketName, id ).getObject();
    }

    /**
     * Save a data stream in the S3, a UUID is generated as resource name.
     *
     * @param data          the data stream to be saved
     * @return              the id of the saved resources
     * @throws IOException if Input/Output issues arise
     */
    @Override
    public String save(InputStream data) throws IOException {
        return this.save( data,  String.format( "%s_%s", new Date( System.currentTimeMillis() ), UUID.randomUUID() ) );
    }

    /**
     * Setup this DataService, based on a OpencmisDataServiceConfig.
     *
     * Can be invoked only one on any give instance.
     *
     * @param configParams             the configuration
     * @return                   the self-configured instance
     * @throws ConfigException   if configuration issues arise
     */
    public S3EcsDataService setup( ConfigParams configParams ) throws ConfigException {
        if ( this.s3Client == null ) {
            this.bucketName = configParams.getValue( BUCKET_NAME );
            String endpoint = configParams.getValue( ENDPOINT );
            log.info("setup bucketName {}, endpoiny {}", this.bucketName, endpoint );
            String accessKey = configParams.getValue( ACCESS_KEY );
            String secretKey = configParams.getValue( SECRET_KEY );
            boolean createIfNotExists = BooleanUtils.isTrue( configParams.getValue( CREATE_IF_NOT_EXISTS ) );
            // Configurazione del client S3
            S3Config config = ConfigException.get( () ->
                    new S3Config(new URI( endpoint ))
                    .withIdentity(accessKey)
                    .withSecretKey(secretKey));
            // Creazione del client
            this.s3Client = new S3JerseyClient(config);
            if ( createIfNotExists && !this.s3Client.bucketExists( this.bucketName ) ) {
                log.info("creating bucket {}", this.bucketName);
                this.s3Client.createBucket( this.bucketName );
            }
            // resource id
            this.resourceIdProvider = DEFAULT_RESOURCE_ID_PROVIDER;
        } else {
            throw new ConfigException( "S3EcsDataService already configured!" );
        }
        return this;
    }


    /**
     * DataService factory method, based on a S3EcsDataService.
     *
     * @param config            the configuration
     * @return                  the self-configured instance
     * @throws ConfigException  if configuration issues arise
     */
    public static S3EcsDataService newDataService(ConfigParams config ) throws ConfigException {
        return new S3EcsDataService().setup( config );
    }

}
