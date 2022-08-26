package kz.archimedes.scheduleservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
class ScheduleServiceApplication

fun main(args: Array<String>) {
    runApplication<ScheduleServiceApplication>(*args)
}
