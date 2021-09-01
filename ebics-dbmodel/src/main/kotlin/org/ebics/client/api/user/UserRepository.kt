package org.ebics.client.api.user

import org.ebics.client.api.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CustomJpaRepository<User, Long>