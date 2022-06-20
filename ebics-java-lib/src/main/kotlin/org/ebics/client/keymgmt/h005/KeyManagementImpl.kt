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
package org.ebics.client.keymgmt.h005

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.trace.h005.TraceSession
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.BankCertificateManager.Companion.createFromCertificates
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.HttpTransferSession

import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.keymgmt.KeyManagement
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h005.OrderTypeDefinition
import org.ebics.client.utils.Utils
import org.ebics.client.xml.h005.*
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Everything that has to do with key handling.
 * If you have a totally new account use `sendINI()` and `sendHIA()` to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 * @author Hachani
 */
/**
 * Constructs a new `KeyManagement` instance
 * with a given ebics session
 * @param session the ebics session
 */
class KeyManagementImpl(session: EbicsSession) : KeyManagement(session) {
    /**
     * Sends the user's signature key (A005) to the bank.
     * After successful operation the user is in state "initialized".
     * @param orderId the order ID. Let it null to generate a random one.
     * @throws EbicsException server generated error message
     * @throws IOException communication error
     */
    @Throws(EbicsException::class, IOException::class)
    override fun sendINI(orderId: String?) {
        session.user.checkAction(EbicsUserAction.INI)
        val sender = HttpTransferSession(session)
        val traceSession = TraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.INI))
        val request = INIRequestElement(session, traceSession).apply { build(); validate() }
        traceSession.trace(request)
        val responseBody = sender.send(ByteArrayContentFactory(request.prettyPrint()))
        val response = KeyManagementResponseElement(responseBody)
        response.build()
        traceSession.trace(response)
        response.report()
        session.user.updateStatus(EbicsUserAction.INI)
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     * @param orderId the order ID. Let it null to generate a random one.
     * @throws IOException communication error
     * @throws EbicsException server generated error message
     */
    @Throws(IOException::class, EbicsException::class)
    override fun sendHIA(orderId: String?) {
        session.user.checkAction(EbicsUserAction.HIA)
        val sender = HttpTransferSession(session)
        val traceSession = TraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.HIA))
        val request = HIARequestElement(session, traceSession).apply { build(); validate() }
        traceSession.trace(request)
        val responseBody = sender.send(ByteArrayContentFactory(request.prettyPrint()))
        val response = KeyManagementResponseElement(responseBody)
        response.build()
        traceSession.trace(response)
        response.report()
        session.user.updateStatus(EbicsUserAction.HIA)
    }

    /**
     * Sends encryption and authentication keys to the bank.
     * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
     * The keys will be activated immediately after successful completion of the transfer.
     * @throws IOException communication error
     * @throws GeneralSecurityException data decryption error
     * @throws EbicsException server generated error message
     */
    @Throws(IOException::class, GeneralSecurityException::class, EbicsException::class)
    override fun sendHPB(password: String): BankCertificateManager {
        session.user.checkAction(EbicsUserAction.HPB)
        val sender = HttpTransferSession(session)
        val request = HPBRequestElement(session).apply { build(); validate() }
        val traceSession = TraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.HPB))
        traceSession.trace(request)
        val responseBody = sender.send(ByteArrayContentFactory(request.prettyPrint()))
        val response = KeyManagementResponseElement(responseBody)
        response.build()
        traceSession.trace(response)
        response.report()
        val factory =
            ByteArrayContentFactory(Utils.unzip(session.userCert.decrypt(response.orderData, response.transactionKey)))
        val orderData = HPBResponseOrderDataElement(factory)
        orderData.build()
        traceSession.trace(orderData)
        val manager = createFromCertificates(orderData.bankE002Certificate, orderData.bankX002Certificate)
        session.user.updateStatus(EbicsUserAction.HPB)
        return manager
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     * @throws IOException Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    @Throws(IOException::class, EbicsException::class)
    override fun lockAccess() {
        session.user.checkAction(EbicsUserAction.SPR)
        val sender = HttpTransferSession(session)
        val request = SPRRequestElement(session).apply { build(); validate() }
        val traceSession = TraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.SPR))
        traceSession.trace(request)
        val responseBody = sender.send(ByteArrayContentFactory(request.prettyPrint()))
        val response = SPRResponseElement(responseBody)
        response.build()
        traceSession.trace(response)
        response.report()
        session.user.updateStatus(EbicsUserAction.SPR)
    }
}