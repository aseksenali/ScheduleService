package kz.archimedes.api.query

import java.time.LocalDate
import java.util.*

sealed interface HolidayQuery

data class FindHolidaysForMedicQuery(val medicId: UUID, val startDate: LocalDate, val endDate: LocalDate) :
    HolidayQuery