package org.ebics.client.model.user

enum class EbicsUserStatusEnum {
    CREATED, //Ebics User without private/public keys
    NEW, //Ebics User with freshly generated private/public keys
    LOCKED, //User locked with SPR, can be newly initialized
    PARTLY_INITIALIZED_INI,
    PARTLY_INITIALIZED_HIA,
    INITIALIZED, //INI + HIA
    READY; //INI + HIA + HPB

    fun updateStatus(action: EbicsUserAction): EbicsUserStatusEnum {
        checkAction(action)
        return when (action) {
            EbicsUserAction.CREATE_KEYS ->
                when (this) {
                    CREATED, NEW -> NEW
                    else -> error("$action action cant be executed at user state: $this")
                }
            EbicsUserAction.INI ->
                when (this) {
                    NEW, LOCKED -> PARTLY_INITIALIZED_INI
                    PARTLY_INITIALIZED_HIA -> INITIALIZED
                    else -> error("$action action cant be executed at user state: $this")
                }
            EbicsUserAction.HIA ->
                when (this) {
                    NEW, LOCKED -> PARTLY_INITIALIZED_HIA
                    PARTLY_INITIALIZED_INI -> INITIALIZED
                    else -> error("$action action cant be executed at user state: $this")
                }
            EbicsUserAction.HPB ->
                when (this) {
                    INITIALIZED -> READY
                    else -> error("$action action cant be executed at user state: $this")
                }
            EbicsUserAction.SPR ->
                when (this) {
                    READY -> LOCKED
                    else -> error("$action action cant be executed at user state: $this")
                }
            //Can be reset in any status
            EbicsUserAction.RESET -> CREATED
            //Creating letters doesn't change state
            EbicsUserAction.CREATE_LETTERS -> this
        }
    }

    fun checkAction(action: EbicsUserAction) {
        when (action) {
            EbicsUserAction.CREATE_KEYS ->
                require(this == CREATED || this == NEW) { "$action action cant be executed at user state: $this" }
            EbicsUserAction.CREATE_LETTERS ->
                require(this != CREATED) { "$action action cant be executed at user state: $this" }
            EbicsUserAction.INI ->
                require(this == NEW || this == LOCKED || this == PARTLY_INITIALIZED_HIA) { "$action action cant be executed at user state: $this" }
            EbicsUserAction.HIA ->
                require(this == NEW || this == LOCKED || this == PARTLY_INITIALIZED_INI) { "$action action cant be executed at user state: $this" }
            EbicsUserAction.HPB ->
                require(this == INITIALIZED) { "$action action cant be executed at user state: $this" }
            EbicsUserAction.SPR ->
                require(this == READY) { "$action action cant be executed at user state: $this" }
        }
    }
}