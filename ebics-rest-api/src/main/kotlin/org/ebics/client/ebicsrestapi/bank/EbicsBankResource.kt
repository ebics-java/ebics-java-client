package org.ebics.client.ebicsrestapi.bank

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.versions.VersionSupport
import org.ebics.client.api.bank.versions.VersionSupportBase
import org.ebics.client.ebicsrestapi.EbicsAccessMode
import org.ebics.client.model.EbicsVersion
import org.springframework.web.bind.annotation.*
import java.net.URL

@RestController
@RequestMapping("banks")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsBankResource(val bankService: BankService, val ebicsBankAPI: EbicsBankAPI) {

    @GetMapping()
    fun listBanks(): List<Bank> = bankService.findBanks()

    @GetMapping("{bankId}")
    fun getBankById(@PathVariable bankId: Long): Bank = bankService.getBankById(bankId)

    @DeleteMapping("{bankId}")
    fun deleteBankById(@PathVariable bankId: Long) = bankService.deleteBankById(bankId)

    @PostMapping("")
    fun createBank(@RequestBody bank: BankData): Long = bankService.createBank(bank)

    @PutMapping("{bankId}")
    fun updateBank(@RequestBody bank: BankData, @PathVariable bankId: Long) = bankService.updateBankById(bankId, bank)

    @GetMapping("{bankId}/supportedVersions")
    fun getSupportedVersions(
        @PathVariable bankId: Long,
        @RequestParam(defaultValue = "OptionalOnline") mode: EbicsAccessMode
    ): List<VersionSupport> = ebicsBankAPI.getSupportedVersions(bankId, mode)

    @GetMapping("supportedVersions")
    fun getSupportedVersionsOnline(
        @RequestParam(required = false) bankId: Long?,
        @RequestParam bankURL: String,
        @RequestParam hostId: String,
        @RequestParam httpClientConfigurationName: String,
        @RequestParam(defaultValue = "OptionalOnline") mode: EbicsAccessMode
    ): List<VersionSupport> =
        ebicsBankAPI.getSupportedVersions(bankId, URL(bankURL), hostId, httpClientConfigurationName, mode)

    @PutMapping("{bankId}/supportedVersions/{ebicsVersion}")
    fun updateSupportedVersion(
        @PathVariable bankId: Long,
        @PathVariable ebicsVersion: EbicsVersion,
        @RequestBody versionSupport: VersionSupportBase
    ) =
        ebicsBankAPI.updateSupportedVersion(bankId, versionSupport)
}