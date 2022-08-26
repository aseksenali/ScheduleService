package kz.archimedes.scheduleservice.config

import org.axonframework.eventhandling.EventMessage
import org.axonframework.extensions.kafka.KafkaProperties
import org.axonframework.extensions.kafka.configuration.KafkaMessageSourceConfigurer
import org.axonframework.extensions.kafka.eventhandling.consumer.ConsumerFactory
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource
import org.axonframework.extensions.kafka.eventhandling.producer.ConfirmationMode
import org.axonframework.extensions.kafka.eventhandling.producer.DefaultProducerFactory
import org.axonframework.extensions.kafka.eventhandling.producer.ProducerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {
    @Bean
    fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, ByteArray>? {
        return DefaultProducerFactory.builder<String, ByteArray>()
            .configuration(kafkaProperties.buildProducerProperties())
            .producerCacheSize(10_000)
            .confirmationMode(ConfirmationMode.WAIT_FOR_ACK)
            .build()
    }

    /**
     * To start a [SubscribableKafkaMessageSource] at the right point in time, we should add those sources to a
     * [KafkaMessageSourceConfigurer].
     */
    @Bean
    fun kafkaMessageSourceConfigurer() = KafkaMessageSourceConfigurer()


    /**
     * The autoconfiguration currently does not create a [SubscribableKafkaMessageSource] bean because the user is
     * inclined to provide the group-id in all scenarios. Doing so provides users the option to create several
     * [org.axonframework.eventhandling.SubscribingEventProcessor] beans belonging to the same group, thus giving
     * Kafka the opportunity to balance the load.
     *
     * Additionally, this subscribable source should be added to the [KafkaMessageSourceConfigurer] to ensure it will
     * be started and stopped within the configuration lifecycle.
     */
    @Bean
    fun subscribableKafkaMessageSource(
        kafkaProperties: KafkaProperties,
        consumerFactory: ConsumerFactory<String, ByteArray>,
        fetcher: Fetcher<String, ByteArray, EventMessage<*>>,
        kafkaMessageSourceConfigurer: KafkaMessageSourceConfigurer
    ): SubscribableKafkaMessageSource<String, ByteArray> {
        val subscribableKafkaMessageSource = SubscribableKafkaMessageSource.builder<String, ByteArray>()
            .groupId("kafka-group")
            .consumerFactory(consumerFactory)
            .fetcher(fetcher)
            .build()
        kafkaMessageSourceConfigurer.configureSubscribableSource { subscribableKafkaMessageSource }
        return subscribableKafkaMessageSource
    }
}