package org.ebics.client.order

open class EbicsOrder(val adminOrderType: EbicsAdminOrderType,
                      val params: Map<String, String> = emptyMap())