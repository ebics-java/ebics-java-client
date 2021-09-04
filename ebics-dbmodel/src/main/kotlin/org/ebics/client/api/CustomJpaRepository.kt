package org.ebics.client.api

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Try to get object by its id from JPA repository
 * If failed, throws not found exception with some customized message
 * Is implemented as extension function, rather than subclass of JpaRepository,
 *  - because otherwise would Spring Data automatically try to generate appropriate query for .getById, with one parameter only
 */
fun <T, ID : Any> JpaRepository<T, ID>.getById(id: ID, objectName: String): T {
    with(findById(id)) {
        if (isPresent)
            return get()
        else
            throw NotFoundException(id.toString(), objectName, null)
    }
}