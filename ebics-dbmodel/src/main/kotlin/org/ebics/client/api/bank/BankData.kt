package org.ebics.client.api.bank

import java.net.URL

class BankData (
    val bankURL: URL,
    val hostId: String,
    val name: String,
    val httpClientConfigurationName: String = "default",
)