package kz.archimedes.scheduleservice.config

import org.axonframework.config.Configurer
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.extensions.kafka.configuration.KafkaMessageSourceConfigurer
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(value = ["axon.kafka.consumer.event-processor-mode"], havingValue = "subscribing")
class SubscribingConfiguration {
    /**
     * The [KafkaMessageSourceConfigurer] should be added to Axon's [Configurer] to ensure it will be called upon start
     * up.
     */
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    fun registerKafkaMessageSourceConfigurer(configurer: Configurer,
                                             kafkaMessageSourceConfigurer: KafkaMessageSourceConfigurer) {
        configurer.registerModule(kafkaMessageSourceConfigurer)
    }

    @Autowired
    fun configureSubscribableKafkaSource(eventProcessingConfigurer: EventProcessingConfigurer,
                                         subscribableKafkaMessageSource: SubscribableKafkaMessageSource<String, ByteArray>) {
        eventProcessingConfigurer.registerSubscribingEventProcessor("kafka-group") { subscribableKafkaMessageSource }
    }
}