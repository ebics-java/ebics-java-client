package org.ebics.client.api.bankconnection.settings

import org.ebics.client.api.security.AuthenticationContext
import org.springframework.stereotype.Service

@Service
class UserSettingsService(private val userSettingsRepository: UserSettingsRepository) {
    fun getCurrentUserSettings(): UserSettings =
        getUserSettings(AuthenticationContext.fromSecurityContext().name)

    fun getUserSettings(userId: String): UserSettings {
        with(userSettingsRepository.findById(userId)) {
            val userSettings = if (isPresent)
                get()
            else
            //Return defaults if the settings doesn't exist yet
                UserSettings(userId, false, true, AdjustmentOptions(
                    applyAutomatically = true,
                    AdjustmentsOptionsPain00x(true, true, true, true, true, true, true, true, true, userId ),
                    AdjustmentsOptionsSwift(true, true, true, false, true, userId)
                ))
            userSettings.checkReadAccess()
            return userSettings
        }
    }

    fun updateCurrentUserSettings(userSettings: UserSettingsData) {
        val userId = AuthenticationContext.fromSecurityContext().name
        with(userSettings) {
            updateUserSettings(UserSettings(userId, uploadOnDrop, testerSettings, adjustmentOptions))
        }
    }

    fun updateUserSettings(userSettings: UserSettings) {
        userSettings.checkWriteAccess()
        userSettingsRepository.saveAndFlush(userSettings)
    }
}