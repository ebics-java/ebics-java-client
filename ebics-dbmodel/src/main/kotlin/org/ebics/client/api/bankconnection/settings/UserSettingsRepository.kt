package org.ebics.client.api.bankconnection.settings

import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingsRepository : JpaRepository<UserSettings, String>