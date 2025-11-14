# data-service-s2-ecs

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](CHANGELOG.md)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.caffetteria/data-service-s3-ecs.svg)](https://central.sonatype.com/artifact/io.github.caffetteria/data-service-s3-ecs)
[![license](https://img.shields.io/badge/License-MIT%20License-teal.svg)](https://opensource.org/license/mit)
[![code of conduct](https://img.shields.io/badge/conduct-Contributor%20Covenant-purple.svg)](https://github.com/fugerit-org/fj-universe/blob/main/CODE_OF_CONDUCT.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=caffetteria_data-service-s3-ecs&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=caffetteria_data-service-s3-ecs)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=caffetteria_data-service-s3-ecs&metric=coverage)](https://sonarcloud.io/summary/new_code?id=caffetteria_data-service-s3-ecs)

[![Java runtime version](https://img.shields.io/badge/run%20on-java%208+-%23113366.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Java build version](https://img.shields.io/badge/build%20on-java%2011+-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Apache Maven](https://img.shields.io/badge/Apache%20Maven-3.9.0+-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)](https://universe.fugerit.org/src/docs/versions/maven3_9.html)

Semplice implementazione di un 
[Data Service Client](https://github.com/fugerit-org/fj-service-helper-bom/tree/main/data-service-base)
che si interfaccia con un server S3.

![Data Service S3 ECS](src/main/docs/dsecs_logo.png "Data Service S3 ECS")

## Quickstart

1. Aggiungere dipendenza : 

```xml
<dependency>
    <groupId>io.github.caffetteria</groupId>
    <artifactId>data-service-s3-ecs</artifactId>
    <version>${data-service-s3-ecs-version}</version>
</dependency>
```

2. Definire le proprietà di configurazione : 

{bucketName=, endpoint=, secretKey=test, accessKey=test, createIfNotExists=true}
[main] INFO io.github.caffetteria.data.service.s3.ecs.S3EcsDataService - setup bucketName test-bucket, endpoiny http://

```
testconfig.bucketName = test-bucket
testconfig.endpoint = http://127.0.0.1:63487
testconfig.accessKey = test
testconfig.secretKey = test
testconfig.createIfNotExists = true
```

3. Creazione e uso data service

```
        // esempio di configurazione 
        // nota : e' possibile creare implementazioni personalizzate di 
        Properties configProperties = ... caricamento configurazione ...
        String configNamespace = "testconfig.";
        ConfigParams config = new ConfigParamsDefault( configNamespace, configProperties );
        // utilizzo
        SeEcsDataService = SeEcsDataService.newDataService( config );
        String testString = "TEST";
        try (InputStream saveIs = new ByteArrayInputStream( testString.getBytes() ) ) {
            String dataId = dataService.save( saveIs );
            try (InputStream loadIs = dataService.load( dataId ) ) {
                String content = StreamIO.readString( loadIs );
                log.info( "check save/load result : {} - {}", testString, content );
            }
        }
```

## Parametri di configurazione

| parametero        | obbligatorio | default | note                         |
|-------------------|--------------|---------|------------------------------|
| bucketName        | true         |         | Nome del bucket              |
| endpoint          | true         |         | endpoint                     |
| accessKey         | true         |         | access key                   |
| secretKey         | true         |         | secret key                   |
| createIfNotExists | false        |         | crea il bucket se non esiste |
