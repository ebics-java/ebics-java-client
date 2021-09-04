package org.ebics.client.api.bank

import org.springframework.data.jpa.repository.JpaRepository

interface BankRepository : JpaRepository<Bank, Long>