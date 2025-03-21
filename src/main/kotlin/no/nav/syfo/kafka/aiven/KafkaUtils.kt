package no.nav.syfo.kafka.aiven

import java.util.Properties
import kotlin.reflect.KClass
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig

class KafkaUtils {
    companion object {
        fun getAivenKafkaConfig(clientId: String): Properties {
            return Properties().also {
                val kafkaEnv = KafkaEnvironment()
                it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaEnv.KAFKA_BROKERS
                it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
                it[CommonClientConfigs.CLIENT_ID_CONFIG] = "${kafkaEnv.KAFKA_CLIENT_ID}-$clientId"
                it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
                it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
                it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = kafkaEnv.KAFKA_TRUSTSTORE_PATH
                it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = kafkaEnv.KAFKA_CREDSTORE_PASSWORD
                it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = kafkaEnv.KAFKA_KEYSTORE_PATH
                it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = kafkaEnv.KAFKA_CREDSTORE_PASSWORD
                it[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = kafkaEnv.KAFKA_CREDSTORE_PASSWORD
                it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
                it[ProducerConfig.ACKS_CONFIG] = "all"
                it[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "true"
            }
        }

        fun Properties.toStreamsConfig(
            applicationName: String,
            valueSerde: KClass<out Serde<out Any>>,
            keySerde: KClass<out Serde<out Any>> = Serdes.String()::class
        ): Properties =
            Properties().also {
                it.putAll(this)
                // TODO hacky workaround for kafka streams issues
                it.setProperty("acks", "1")
                it.remove("enable.idempotence")
                it[StreamsConfig.APPLICATION_ID_CONFIG] = applicationName
                it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = keySerde.java
                it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = valueSerde.java
            }
    }
}
