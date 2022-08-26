package kz.archimedes.scheduleservice.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration
import org.axonframework.messaging.StreamableMessageSource
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.queryhandling.SimpleQueryUpdateEmitter
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AxonConfig {
    @Autowired
    fun configureInitialTrackingToken(processingConfigurer: EventProcessingConfigurer) {
        val tepConfig = TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
            .andInitialTrackingToken(StreamableMessageSource<*>::createHeadToken)
        processingConfigurer.registerTrackingEventProcessorConfiguration {
            tepConfig
        }
    }

    @Bean
    @Primary
    fun serializer(): Serializer {
        return JacksonSerializer.builder()
            .defaultTyping()
            .objectMapper(jacksonObjectMapper())
            .build()
    }

    @Bean
    fun queryUpdateEmitter(): QueryUpdateEmitter {
        return SimpleQueryUpdateEmitter.builder()
            .build()
    }
}