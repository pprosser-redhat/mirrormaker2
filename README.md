# price
# Messing around with Kafka, Mirrormaker 2 and Truststores. 


Run with dev mode and truststore

oc get secret store-cluster-cluster-ca-cert -o jsonpath='{.data.ca\.crt}' | base64 -d > storeca.crt
oc get secret datacentre-cluster-cluster-ca-cert -o jsonpath='{.data.ca\.crt}' | base64 -d > datacentreca.crt

keytool -import -trustcacerts -alias root -file shopca.crt -keystore shopTruststore.jks -storepass password -noprompt

keytool -import -trustcacerts -alias root -file datacentreca.crt -keystore datacentrecaTruststore.jks -storepass password -noprompt


keytool -list -v -keystore shopTrustStore.jks

quarkus dev -Djavax.net.ssl.trustStore=shopTruststore.jks -Djavax.net.ssl.trustStorePassword=password

java  -Djavax.net.ssl.trustStore=shopTruststore.jks -Djavax.net.ssl.trustStorePassword=password -jar target/quarkus-app/quarkus-run.jar

oc create secret generic shoptruststore --from-file=shopTrustStore.jks

oc exec -it datacentre-cluster-kafka-0 -- /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --topic shop-source.product


add 

  ssl.truststore.location=/Users/pprosser/projects/price/shopTruststore.jks \
  ssl.truststore.password=password

  to producer.properties 
/Users/pprosser/Downloads/kafka_2.13-3.3.1.redhat-00008/bin/kafka-console-consumer.sh \
    --bootstrap-server store-cluster-kafka-bootstrap-store.apps.cluster-fqgg4.fqgg4.sandbox3159.opentlc.com:443 \
    --from-beginning \
    --topic product \
    --consumer.config /Users/pprosser/projects/price/producer.properties

oc create secret generic shopcert --from-file=storeca.crt

apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaMirrorMaker2
metadata:
  name: datacentre-mm2-cluster
  namespace: datacentre
spec:
  clusters:
    - alias: datacentre-source
      bootstrapServers: 'datacentre-cluster-kafka-bootstrap.datacentre.svc.cluster.local:9092'
    - alias: shop-target
      bootstrapServers: >-
        store-cluster-kafka-bootstrap-store.apps.cluster-fqgg4.fqgg4.sandbox3159.opentlc.com:443
      config:
        config.storage.replication.factor: -1
        offset.storage.replication.factor: -1
        status.storage.replication.factor: -1
      tls:
        trustedCertificates:
          - certificate: storeca.crt
            secretName: shopcert
  connectCluster: datacentre-source
  mirrors:
    - checkpointConnector:
        config:
          checkpoints.topic.replication.factor: 1
      groupsPattern: .*
      heartbeatConnector:
        config:
          heartbeats.topic.replication.factor: 1
      sourceCluster: datacentre-source
      sourceConnector:
        config:
          offset-syncs.topic.replication.factor: 1
          replication.factor: 1
          sync.topic.acls.enabled: 'false'
      targetCluster: shop-target
      topicsPattern: product
  replicas: 1
  version: 3.3.1


This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/price-1.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, JPA)
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A JAX-RS implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye Reactive Messaging ([guide](https://quarkus.io/guides/reactive-messaging)): Produce and consume messages and implement event driven and data streaming applications
- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- Agroal - Database connection pool ([guide](https://quarkus.io/guides/datasource)): Pool JDBC database connections (included in Hibernate ORM)

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### Reactive Messaging codestart

Use SmallRye Reactive Messaging

[Related Apache Kafka guide section...](https://quarkus.io/guides/kafka-reactive-getting-started)


### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
