package kz.archimedes.scheduleservice.validation

import kz.archimedes.scheduleservice.dto.schedule.CreateScheduleDTO
import kz.archimedes.scheduleservice.dto.schedule.ScheduleDTO
import kz.archimedes.scheduleservice.exception.ValidationException
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class ScheduleValidator: Validator {
    override fun supports(clazz: Class<*>): Boolean {
        return ScheduleDTO::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        if (target !is ScheduleDTO<*>)
            throw ValidationException("Parameter should not be null and must be of type ${ScheduleDTO::class.java}")
        when (target) {
            is CreateScheduleDTO -> {

            }
            else -> {

            }
        }
    }

}