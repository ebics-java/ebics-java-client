package org.ebics.client.api.bank.versions

import org.springframework.data.jpa.repository.JpaRepository

interface VersionSupportRepository : JpaRepository<VersionSupport, Long>