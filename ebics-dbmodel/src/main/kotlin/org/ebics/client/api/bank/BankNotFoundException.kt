package org.ebics.client.api.bank

import java.lang.RuntimeException

class BankNotFoundException(val bankId:Long) : RuntimeException ("Can't find bank $bankId")