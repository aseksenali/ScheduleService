package kz.archimedes.scheduleservice.router

import kz.archimedes.scheduleservice.handler.`interface`.IHolidayHandler
import kz.archimedes.scheduleservice.handler.`interface`.IScheduleHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfiguration(
    private val scheduleHandler: IScheduleHandler,
    private val holidayHandler: IHolidayHandler
) {
    @Bean
    fun router() = coRouter {
        "/relocation".nest {
            POST("/{medicId}", scheduleHandler::createRelocation)
        }
        "/schedule".nest {
            GET("", scheduleHandler::findAllSchedules)
            GET("/{medicId}", scheduleHandler::findSchedule)
            POST("", scheduleHandler::createSchedule)
        }
        "/holidays/{medicId}".nest {
            DELETE("", holidayHandler::deleteHoliday)
            GET("", holidayHandler::findHolidays)
            POST("", holidayHandler::createHoliday)
        }
        "/history/{medicId}".nest {
            GET("", scheduleHandler::findScheduleHistory)
        }
    }
}