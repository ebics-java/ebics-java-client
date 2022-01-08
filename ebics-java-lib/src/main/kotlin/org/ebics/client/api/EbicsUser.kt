package org.ebics.client.api

interface EbicsUser : EbicsUserInfo {
    val partner: EbicsPartner
}

