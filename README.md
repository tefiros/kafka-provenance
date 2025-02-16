# YANG Validation using Schema Registry

Sample code showcasing how JSON [[RFC7951]](https://datatracker.ietf.org/doc/html/rfc7951) and CBOR [[RFC9254]](https://datatracker.ietf.org/doc/html/rfc9254) messages can be validated using Confluent Schema Registry [[YANG-integration]](https://github.com/network-analytics/schema-registry-dev/tree/feature/schemaid-kafka-header).


## Dependencies
- Kafka (Tested with 7.5.1-post) [[Repository]](https://github.com/confluentinc/kafka/tree/7.5.0-post)
- YangKit [[Repository]](https://github.com/yang-central/yangkit)
- Confluent Schema Registry with YANG integration [[Repository (branch feature/schemaid-kafka-header)]](https://github.com/network-analytics/schema-registry-dev/tree/feature/schemaid-kafka-header)

## Quickstart

1. Compile Kafka, start a Kafka instance:
```shell
(kafka)$ ./bin/zookeeper-server-start.sh config/zookeeper.properties
```
```shell
(kafka)$ ./bin/kafka-server-start.sh config/server.properties
```

Create Kafka topic:
```shell
(kafka)$ ./bin/kafka-topics.sh --bootstrap-server=localhost:9092 --create --topic yang.tests
```

2. Compile Schema registry with YANG integration, start a schema registry instance:
```shell
(schema-registry)$ ./bin/schema-registry-start config/schema-registry.properties
```

3. Compile Schema Registry Samples (current repository):
```shell
(schema-registry-samples)$ mvn clean install -e -DskipTests
```

4. Execute Producers and Consumers.

JSON Examples (`schema-registry-samples-json`):
- `JsonProducerExample` and `JsonConsumerExample` show simple examples for producing YANG-JSON messages with the associated YANG modules.
- `JsonProducerTelemetry` and `JsonConsumerTelemetry` produce and consume YANG-JSON messages according to the model defined in [draft-aelhassany-telemetry-msg](https://github.com/network-analytics/draft-aelhassany-telemetry-msg)

CBOR Examples (`schema-registry-samples-cbor`):
- `CborProducerExample` and `CborConsumerExample` show simple examples for producing YANG-CBOR messages with the associated YANG modules.
- `CborProducerTelemetry` and `CborConsumerTelemetry` produce and consume YANG-CBOR messages according to the model defined in [draft-aelhassany-telemetry-msg](https://github.com/network-analytics/draft-aelhassany-telemetry-msg)

## Contributors
This code has been developed by Alex Huang Feng [(ahuangfeng)](https://github.com/ahuangfeng) and Vivek Boudia [(BVivek974)](https://github.com/BVivek974) from INSA-Lyon as part of the collaboration with Swisscom and Huawei (See details [here](https://github.com/network-analytics/draft-daisy-kafka-yang-integration/blob/main/draft-daisy-kafka-yang-integration-05.md) and [draft-ietf-nmop-yang-message-broker-integration](https://datatracker.ietf.org/doc/draft-ietf-nmop-yang-message-broker-integration/))
