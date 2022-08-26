package kz.archimedes.scheduleservice.repository

import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ScheduleEntityRepository: CoroutineSortingRepository<ScheduleEntity, UUID>