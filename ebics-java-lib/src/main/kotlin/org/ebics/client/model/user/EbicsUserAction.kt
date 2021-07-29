package org.ebics.client.model.user

enum class EbicsUserAction {
    CREATE_KEYS,
    INI,
    HIA,
    HPB,
    SPR,   //Reset user status on client & server side
    RESET, //Reset user status without SPR request, the actual server status must be reset on server side
}