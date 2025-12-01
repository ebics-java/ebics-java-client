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
 */

package org.kopi.ebics.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.kopi.ebics.certificate.KeyStoreManager;
import org.kopi.ebics.certificate.KeyUtil;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.HIARequestElement;
import org.kopi.ebics.xml.HPBRequestElement;
import org.kopi.ebics.xml.HPBResponseOrderDataElement;
import org.kopi.ebics.xml.INIRequestElement;
import org.kopi.ebics.xml.KeyManagementResponseElement;
import org.kopi.ebics.xml.SPRRequestElement;
import org.kopi.ebics.xml.SPRResponseElement;

/**
 * Everything that has to do with key handling.
 * If you have a totally new account use <code>sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 *
 */
public class KeyManagement {

    /**
     * Constructs a new <code>KeyManagement</code> instance
     * with a given ebics session
     *
     * @param session the ebics session
     */
    public KeyManagement(EbicsSession session) {
        this.session = session;
    }

    /**
     * Sends the user's signature key (A005) to the bank.
     * After successful operation the user is in state "initialized".
     *
     * @param orderId the order ID. Let it null to generate a random one.
     * @throws EbicsException server generated error message
     * @throws IOException    communication error
     */
    public void sendINI(String orderId) throws EbicsException, IOException {
        var sender = new HttpRequestSender(session);
        var request = new INIRequestElement(session, orderId);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        int httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        var response = new KeyManagementResponseElement(sender.getResponseBody(), "INIResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @param orderId the order ID. Let it null to generate a random one.
     * @throws IOException    communication error
     * @throws EbicsException server generated error message
     */
    public void sendHIA(String orderId) throws IOException, EbicsException {
        HttpRequestSender sender = new HttpRequestSender(session);
        HIARequestElement request = new HIARequestElement(session, orderId);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        int httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        KeyManagementResponseElement response = new KeyManagementResponseElement(
            sender.getResponseBody(), "HIAResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
    }

    /**
     * Sends encryption and authentication keys to the bank.
     * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
     * The keys will be activated immediately after successful completion of the transfer.
     *
     * @throws IOException              communication error
     * @throws GeneralSecurityException data decryption error
     * @throws EbicsException           server generated error message
     */
    public void sendHPB() throws IOException, GeneralSecurityException, EbicsException {
        HttpRequestSender sender = new HttpRequestSender(session);
        HPBRequestElement request = new HPBRequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        int httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        var body = sender.getResponseBody();
        Utils.checkHttpCode(httpCode);
        KeyManagementResponseElement response = new KeyManagementResponseElement(body,
            "HBPResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
        EbicsUser user = session.getUser();
        ContentFactory factory = new ByteArrayContentFactory(
            Utils.unzip(user.decrypt(response.getOrderData(), response.getTransactionKey())));
        HPBResponseOrderDataElement orderData = new HPBResponseOrderDataElement(factory);
        orderData.build();
        session.getConfiguration().getTraceManager().trace(orderData);
        KeyStoreManager keystoreManager = new KeyStoreManager();
        var path = session.getConfiguration().getKeystoreDirectory(user);
        keystoreManager.load(null, user.getPasswordCallback().getPassword());
        EbicsBank bank = user.getPartner().getBank();
        String bankID = session.getBankID();
        var e002PubKey = keystoreManager.getPublicKey(
            new ByteArrayInputStream(orderData.getBankE002Certificate()));
        var x002PubKey = keystoreManager.getPublicKey(
            new ByteArrayInputStream(orderData.getBankX002Certificate()));
        bank.setBankKeys(e002PubKey, x002PubKey);
        bank.setDigests(KeyUtil.getKeyDigest(e002PubKey), KeyUtil.getKeyDigest(x002PubKey));
        keystoreManager.setCertificateEntry(bankID + "-E002",
            new ByteArrayInputStream(orderData.getBankE002Certificate()));
        keystoreManager.setCertificateEntry(bankID + "-X002",
            new ByteArrayInputStream(orderData.getBankX002Certificate()));
        keystoreManager.save(new FileOutputStream(new File(path, bankID + ".p12")));
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     *
     * @throws IOException    Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    public void lockAccess() throws IOException, EbicsException {
        HttpRequestSender sender;
        SPRRequestElement request;
        SPRResponseElement response;
        int httpCode;

        sender = new HttpRequestSender(session);
        request = new SPRRequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        response = new SPRResponseElement(sender.getResponseBody());
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final EbicsSession session;
}
