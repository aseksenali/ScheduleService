package kz.archimedes.scheduleservice.config

import io.camunda.zeebe.spring.client.EnableZeebeClient
import org.springframework.context.annotation.Configuration

@Configuration
@EnableZeebeClient
class ZeebeConfig