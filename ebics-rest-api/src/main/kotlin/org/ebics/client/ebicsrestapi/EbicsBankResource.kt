package org.ebics.client.ebicsrestapi

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("banks")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsBankResource (val bankService: BankService) {

    @GetMapping()
    fun listBanks(): List<Bank> = bankService.findBanks()

    @GetMapping("{bankId}")
    fun getBankById(@PathVariable bankId: Long): Bank = bankService.getBankById(bankId)

    @DeleteMapping("{bankId}")
    fun deleteBankById(@PathVariable bankId: Long) = bankService.deleteBankById(bankId)

    @PostMapping("")
    fun createBank(@RequestBody bank: Bank):Long = bankService.createBank(bank)

    @PutMapping("{bankId}")
    fun updateBank(@RequestBody bank: Bank, @PathVariable bankId:Long) = bankService.updateBankById(bankId, bank)
}