package org.ebics.client.api.user.settings

import org.ebics.client.api.CustomJpaRepository

interface UserSettingsRepository : CustomJpaRepository<UserSettings, String>