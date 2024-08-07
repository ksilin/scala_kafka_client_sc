package com.example

import com.example.util.SpecBase
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.config.SaslConfigs

import scala.jdk.CollectionConverters._

class CCloudProtoCSFLESpec extends SpecBase {

  val topicName = "sc-proto-serde-test"

  "must be able to serialize and deserialize specifc SRProto with CCloud SR" in {

    val configMap: Map[String, Object] = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "https://psrc-XXX.REGION.gcp.confluent.cloud",
      AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE -> "USER_INFO",
      AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG -> "<SR_API_KEY_WITH_KEY_PERMISSIONS>:<SR_API_SECRET>",
      AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION    -> "true",
      AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS -> "false",
      "rule.executors._default_.param.access.key.id"       -> "AWS_ACCESS_KEY",
      "rule.executors._default_.param.secret.access.key" -> "AWS_ACCES_KEY_SECRET",
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> "pkc-XXX.REGION.gcp.confluent.cloud:9092",
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> "SASL_SSL",
      SaslConfigs.SASL_MECHANISM                   -> "PLAIN",
      SaslConfigs.SASL_JAAS_CONFIG -> "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"BROKER_API_KEY\" password=\"BROKER_API_SECRET\";",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer"
    )

    val consumerConfigMap = configMap ++ Map(
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer",
      KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE -> classOf[
        PersonalDataOuterClass.PersonalData
      ]
    )

    val data: PersonalDataOuterClass.PersonalData = PersonalDataOuterClass.PersonalData
      .newBuilder()
      .setId("id_123")
      .setName("Egon Schiele")
      .setBirthday("12.06.1890")
      .build()

    val kafkaProducer =
      new KafkaProducer[String, PersonalDataOuterClass.PersonalData](configMap.asJava)

    val result = kafkaProducer
      .send(
        new ProducerRecord[String, PersonalDataOuterClass.PersonalData](topicName, data),
        { (m: RecordMetadata, ex: Exception) =>
          Option(ex) match {
            case Some(e) =>
              println(s"Error sending record: ${e.getMessage}")
              e.printStackTrace()
            case None =>
              println(s"Record sent successfully to ${m.topic()} partition ${m.partition()} with offset ${m.offset()}")
          }
        }
      )
      .get

    println(result)
  }

}
