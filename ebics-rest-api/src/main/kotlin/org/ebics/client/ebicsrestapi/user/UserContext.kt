package org.ebics.client.ebicsrestapi.user

data class UserContext(val name:String, val roles:List<String>, val appVersion:String, val appBuildTimestamp:String)
