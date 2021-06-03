package org.ebics.client.ebicsrestapi

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("banks")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsBankResource (val bankService: BankService) {
    @GetMapping("test")
    fun index(): List<Pair<String, String>> = listOf(
        Pair("1", "Hello!"),
        Pair("2", "Bonjour!"),
    )

    @GetMapping()
    fun listBanks(): List<Bank> = bankService.findBanks()

    @GetMapping("{id}")
    fun getBankById(@PathVariable bankId: Long): Bank = bankService.getBankById(bankId)

    @DeleteMapping("{id}")
    fun deleteBankById(@PathVariable bankId: Long) = bankService.deleteBankById(bankId)

    @PostMapping("")
    fun createBank(@RequestBody bank: Bank):Long = bankService.createBank(bank)

    @PutMapping("{id}")
    fun updateBank(@RequestBody bank: Bank, @PathVariable bankId:Long) = bankService.updateBankById(bankId, bank)
}