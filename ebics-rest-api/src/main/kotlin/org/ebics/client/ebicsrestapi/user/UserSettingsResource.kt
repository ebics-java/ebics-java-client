package org.ebics.client.ebicsrestapi.user

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.user.settings.UserSettings
import org.ebics.client.api.user.settings.UserSettingsService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("user/settings")
@CrossOrigin(origins = ["http://localhost:8081"])
class UserSettingsResource(private val userSettingsService: UserSettingsService) {
    @GetMapping()
    fun getUserSettings(): UserSettings {
        return userSettingsService.getUserSettings()
    }

    @PutMapping
    fun updateUserSettings(userSettings: UserSettings) {
        userSettingsService.updateUserSettings(userSettings)
    }
}

