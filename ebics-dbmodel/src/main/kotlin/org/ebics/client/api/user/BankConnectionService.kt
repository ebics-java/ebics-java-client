package org.ebics.client.api.user

import org.ebics.client.api.user.permission.BankConnectionAccessType

interface BankConnectionService {
    fun findUsers(permission: BankConnectionAccessType): List<BankConnectionEntity>
    fun getUserById(userId: Long, permission: BankConnectionAccessType = BankConnectionAccessType.READ): BankConnectionEntity
    fun saveUser(bankConnection: BankConnectionEntity): Long
    fun createUserAndPartner(bankConnection: BankConnection): Long
    fun updateUserAndPartner(id: Long, bankConnection: BankConnection): Long
    fun deleteUser(userId: Long)

    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    fun resetStatus(userId: Long): Unit
}