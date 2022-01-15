package org.ebics.client.api.bank

import DbTestContext
import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.getById
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class BankRepositoryTest (@Autowired private val bankRepo: BankRepository) {
    @Test
    fun testAddGetDelete() {
        val count = bankRepo.count()
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)
        bankRepo.saveAndFlush(bank)
        Assertions.assertNotNull(bank.id)
        val loadedBank = bankRepo.getById(bank.id!!, "bank")
        assertThat(loadedBank.name).isEqualTo(bank.name)
        bankRepo.deleteById(bank.id!!)
        assertThat(bankRepo.count()).isEqualTo(count)
    }
}