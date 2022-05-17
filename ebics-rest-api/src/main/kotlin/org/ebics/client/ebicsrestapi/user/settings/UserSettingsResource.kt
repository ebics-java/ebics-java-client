package org.ebics.client.ebicsrestapi.user.settings

import org.ebics.client.api.bankconnection.settings.UserSettings
import org.ebics.client.api.bankconnection.settings.UserSettingsData
import org.ebics.client.api.bankconnection.settings.UserSettingsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("user/settings")
@CrossOrigin(origins = ["http://localhost:8081"])
class UserSettingsResource(private val userSettingsService: UserSettingsService) {
    @GetMapping()
    fun getUserSettings(): UserSettings {
        return userSettingsService.getCurrentUserSettings()
    }

    @PutMapping
    fun updateUserSettings(@RequestBody userSettings: UserSettingsData) {
        userSettingsService.updateCurrentUserSettings(userSettings)
    }
}

