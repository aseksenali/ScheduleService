package kz.archimedes.scheduleservice.model.util

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Transient
import java.time.temporal.Temporal

abstract class TimeInterval<T>(
    @Transient
    @JsonIgnore
    val start: T,
    @Transient
    @JsonIgnore
    val end: T
) where T : Comparable<T>, T : Temporal