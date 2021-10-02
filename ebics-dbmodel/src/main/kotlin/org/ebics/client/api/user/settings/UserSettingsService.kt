package org.ebics.client.api.user.settings

import org.ebics.client.api.user.SecurityCtxHelper
import org.ebics.client.api.user.UserRepository
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class UserSettingsService(private val userSettingsRepository: UserSettingsRepository) {
    fun getUserSettings(): UserSettings {
        val userId = SecurityCtxHelper.getPrincipalName()
        with(userSettingsRepository.findById(userId)) {
            return if (isPresent)
                get()
            else
                //Return defaults if the settings doesn't exist yet
                UserSettings(userId, false, true, AdjustmentOptions(
                    applyAutomatically = true,
                    AdjustmentsOptionsPain00x(true, true, true, true, true, true, true, true, true, userId ),
                    AdjustmentsOptionsSwift(true, true, true, false, true, userId)
                ))
        }
    }

    fun updateUserSettings(userSettings: UserSettings) {
        SecurityCtxHelper.checkWriteAuthorization(userSettings)
        userSettingsRepository.saveAndFlush(userSettings)
    }
}