package org.ebics.client.api.bank

import org.ebics.client.api.EbicsBank
import java.net.URL

class BankData (
    override val bankURL: URL,
    override val hostId: String,
    override val name: String,
    override val httpClientConfigurationName: String = "default",
) : EbicsBank