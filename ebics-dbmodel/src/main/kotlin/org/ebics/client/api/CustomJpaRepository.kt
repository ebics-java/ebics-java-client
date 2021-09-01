package org.ebics.client.api

import org.springframework.data.jpa.repository.JpaRepository

interface CustomJpaRepository<T, ID:Any> : JpaRepository<T, ID> {

    /**
     * Try to get object by its id from JPA repository
     * If failed, throws not found exception with some customized message
     */
    fun getById(id: ID, objectName: String): T {
        with (findById(id)) {
            if (isPresent)
                return get()
            else
                throw NotFoundException(id.toString(), objectName, null)
        }
    }
}