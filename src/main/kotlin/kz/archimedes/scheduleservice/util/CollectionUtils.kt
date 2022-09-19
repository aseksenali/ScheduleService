package kz.archimedes.scheduleservice.util

import kz.archimedes.scheduleservice.exception.NotSupportedOperationException
import kz.archimedes.scheduleservice.model.command.Holiday
import kz.archimedes.scheduleservice.model.command.SpecialCaseDay
import kz.archimedes.scheduleservice.model.query.HolidayEntity
import kz.archimedes.scheduleservice.model.query.SpecialCaseDayEntity
import kz.archimedes.scheduleservice.model.util.TimeInterval
import kz.archimedes.scheduleservice.model.util.WorkingHours
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.Temporal

object CollectionUtils {
    inline fun <T, reified U : TimeInterval<T>> List<U>.intersection(elem: U): List<U> where T : Comparable<T>, T : Temporal {
        return when (elem) {
            is Holiday ->
                this.filter {
                    it as Holiday
                    (elem.start >= it.start && elem.start <= it.end) || (elem.end >= it.start && elem.end <= it.end)
                }

            is HolidayEntity ->
                this.filter {
                    it as HolidayEntity
                    (elem.start >= it.start && elem.start <= it.end) || (elem.end >= it.start && elem.end <= it.end)
                }

            is SpecialCaseDay ->
                this.filter {
                    it as SpecialCaseDay
                    elem.date == it.date
                }.filter {
                    it as SpecialCaseDay
                    (elem.start >= it.start && elem.start <= it.end) || (elem.end >= it.start && elem.end <= it.end)
                }

            is SpecialCaseDayEntity -> {
                this.filter {
                    it as SpecialCaseDayEntity
                    elem.date == it.date
                }.filter {
                    it as SpecialCaseDayEntity
                    (elem.start >= it.start && elem.start <= it.end) || (elem.end >= it.start && elem.end <= it.end)
                }
            }

            else -> throw NotSupportedOperationException
        }
    }

    fun <T> List<TimeInterval<T>>.findMinAndMax(defaultValues: Pair<T, T>): Pair<T, T> where T : Comparable<T>, T : Temporal {
        return this.map {
            Pair(it.start, it.end)
        }.fold(defaultValues) { acc, pair ->
            val newStart = if (pair.first < acc.first) pair.first else acc.first
            val newEnd = if (pair.second > acc.second) pair.second else acc.second
            return Pair(newStart, newEnd)
        }
    }

    inline fun <T, reified U : TimeInterval<T>> U.copyChangingIntervals(
        newStart: T,
        newEnd: T
    ): U where T : Comparable<T>, T : Temporal {
        return when (this) {
            is Holiday -> {
                newStart as LocalDate
                newEnd as LocalDate
                this.copy(
                    startDate = newStart,
                    endDate = newEnd
                ) as U
            }

            is HolidayEntity -> {
                newStart as LocalDate
                newEnd as LocalDate
                this.copy(
                    startDate = newStart,
                    endDate = newEnd
                ) as U
            }

            is SpecialCaseDay -> {
                newStart as LocalTime
                newEnd as LocalTime
                this.copy(
                    workingHours = WorkingHours(newStart, newEnd)
                ) as U
            }

            is SpecialCaseDayEntity -> {
                newStart as LocalTime
                newEnd as LocalTime
                this.copy(
                    workingHours = WorkingHours(newStart, newEnd)
                ) as U
            }

            else -> throw NotSupportedOperationException
        }
    }

    inline fun <T, reified U : TimeInterval<T>> U.getSurroundingIntervals(
        newStart: T,
        newEnd: T
    ): Pair<U?, U?> where T : Comparable<T>, T : Temporal {
        return when (this) {
            is HolidayEntity -> {
                newStart as LocalDate
                newEnd as LocalDate
                val firstElement = if (newStart as Temporal != this.start) {
                    this.copy(
                        startDate = newStart, endDate = this.startDate.minusDays(1)
                    ) as U
                } else null
                val secondElement = if (newEnd as Temporal != this.end) {
                    this.copy(
                        startDate = this.endDate.plusDays(1), endDate = newEnd
                    ) as U
                } else null
                Pair(firstElement, secondElement)
            }

            is SpecialCaseDayEntity -> {
                newStart as LocalTime
                newEnd as LocalTime
                val firstElement = if (newStart as Temporal != this.start) {
                    this.copy(
                        workingHours = WorkingHours(newStart, this.start)
                    ) as U
                } else null
                val secondElement = if (newEnd as Temporal != this.end) {
                    this.copy(
                        workingHours = WorkingHours(this.end, newEnd)
                    ) as U
                } else null
                Pair(firstElement, secondElement)
            }

            else -> throw NotSupportedOperationException
        }
    }

    inline operator fun <T, reified U : TimeInterval<T>> List<U>.plus(other: U): List<U> where T : Comparable<T>, T : Temporal {
        val intersecting = this.intersection(other)
        val (min, max) = intersecting.findMinAndMax(Pair(other.start, other.end))
        val notIntersecting = this.filter {
            it !in intersecting
        }
        val newElement = other.copyChangingIntervals(min, max)

        return notIntersecting.plusElement(newElement)
    }

    inline operator fun <T, reified U : TimeInterval<T>> List<U>.minus(other: U): List<U> where T : Comparable<T>, T : Temporal {
        val intersecting = this.intersection(other)
        val (min, max) = intersecting.findMinAndMax(Pair(other.start, other.end))

        val result = this.filter {
            it !in intersecting
        }.toMutableList()

        val (firstElement, secondElement) = other.getSurroundingIntervals(min, max)

        firstElement?.let { result.add(it) }
        secondElement?.let { result.add(it) }

        return result.toList()
    }

    inline operator fun <T, reified U : TimeInterval<T>> MutableList<U>.plusAssign(other: U) where T : Comparable<T>, T : Temporal {
        val intersecting = this.intersection(other)
        val (min, max) = intersecting.findMinAndMax(Pair(other.start, other.end))

        val result = this.filter {
            it !in intersecting
        }
        this.removeIf {
            it !in result
        }
        val newElement = other.copyChangingIntervals(min, max)
        this.add(newElement)
    }

    inline operator fun <T, reified U : TimeInterval<T>> MutableList<U>.minusAssign(other: U) where T : Comparable<T>, T : Temporal {
        val intersecting = this.intersection(other)
        val (min, max) = intersecting.findMinAndMax(Pair(other.start, other.end))

        val result = this.filter {
            it !in intersecting
        }.toMutableList()

        this.removeIf {
            it !in result
        }

        val (firstElement, secondElement) = other.getSurroundingIntervals(min, max)

        firstElement?.let { this.add(it) }
        secondElement?.let { this.add(it) }
    }
}