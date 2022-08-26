package kz.archimedes.scheduleservice.model.query

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable

abstract class BasePersistable<ID>(@Id @JsonIgnore open var _id: ID): Persistable<ID> {
    @Transient
    @JsonIgnore
    private var isNew = false

    @JsonIgnore
    override fun getId(): ID? = _id

    @JsonIgnore
    override fun isNew(): Boolean = isNew

    fun markNew() {
        isNew = true
    }
}