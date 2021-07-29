/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.api

/**
 * Information about an EBICS customer.
 *
 * @author Hachani
 */
interface EbicsPartner {
    /**
     * Returns the bank we are customer of.
     * @return the bank we are customer of
     */
    val bank: EbicsBank

    /**
     * Returns the customers id at the bank.
     * @return the customers id at the bank.
     */
    val partnerId: String

    /**
     * Actual order ID
     * @return order ID
     */
    var orderId: Int

    /**
     * In EBICS XSD schema - ebics_types.xsd, The order ID pattern
     * is defined as following: **pattern value="[A-Z][A-Z0-9]{3}"**.
     *
     * This means that the order ID should start with a letter
     * followed by three alphanumeric characters.
     *
     *
     *  The `nextOrderId()` aims to generate orders from
     * **A000** to **ZZZZ**. The sequence cycle is performed infinitely.
     *
     *  The order index [this.orderId] is saved whenever it changes.
     */
    fun nextOrderId(): String {
        val chars = CharArray(4)
        orderId += 1
        if (orderId > 36 * 36 * 36 * 36 - 1 || orderId < 10 * 36 * 36 * 36) {
            // ensure that orderId starts with a letter
            orderId = 10 * 36 * 36 * 36
        }
        chars[3] = ALPHA_NUM_CHARS[orderId % 36]
        chars[2] = ALPHA_NUM_CHARS[orderId / 36 % 36]
        chars[1] = ALPHA_NUM_CHARS[orderId / 36 / 36 % 36]
        chars[0] = ALPHA_NUM_CHARS[orderId / 36 / 36 / 36]
        return String(chars)
    }

    companion object {
        private const val ALPHA_NUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }
}