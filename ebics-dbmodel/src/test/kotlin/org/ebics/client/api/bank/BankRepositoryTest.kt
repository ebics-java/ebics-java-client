package org.ebics.client.api.bank

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.getById
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
class BankRepositoryTest (@Autowired private val bankRepo: BankRepository) {
    @Test
    fun testAddGetDelete() {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)
        bankRepo.saveAndFlush(bank)
        assertThat(bank.id).isEqualTo(1)
        val loadedBank = bankRepo.getById(1, "bank")
        assertThat(loadedBank.name).isEqualTo(bank.name)
        bankRepo.deleteById(1)
        assertThat(bankRepo.count()).isEqualTo(0)
    }
}