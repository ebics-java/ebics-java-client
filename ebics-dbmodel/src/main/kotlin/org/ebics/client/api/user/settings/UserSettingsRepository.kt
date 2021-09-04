package org.ebics.client.api.user.settings

import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingsRepository : JpaRepository<UserSettings, String>