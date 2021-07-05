package org.ebics.client.api.trace

import org.ebics.client.api.user.User
import javax.persistence.*

@Entity
data class TraceEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @Lob
    val messageBody:String,

    @ManyToOne(optional = false)
    val user:User,
)
