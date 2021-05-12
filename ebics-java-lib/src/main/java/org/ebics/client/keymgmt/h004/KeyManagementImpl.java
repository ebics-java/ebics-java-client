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

package org.ebics.client.keymgmt.h004;

import org.ebics.client.certificate.KeyStoreManager;
import org.ebics.client.certificate.KeyUtil;
import org.ebics.client.api.HttpRequestSender;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.interfaces.ContentFactory;
import org.ebics.client.interfaces.PasswordCallback;
import org.ebics.client.io.ByteArrayContentFactory;
import org.ebics.client.keymgmt.KeyManagement;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.utils.Utils;
import org.ebics.client.xml.h004.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;


/**
 * Everything that has to do with key handling.
 * If you have a totally new account use <code>sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 * @author Hachani
 *
 */
public class KeyManagementImpl extends KeyManagement {

  /**
   * Constructs a new <code>KeyManagement</code> instance
   * with a given ebics session
   * @param session the ebics session
   */
  public KeyManagementImpl(EbicsSession session) {
    super(session);
  }

  /**
   * Sends the user's signature key (A005) to the bank.
   * After successful operation the user is in state "initialized".
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws EbicsException server generated error message
   * @throws IOException communication error
   */
  @Override
  public void sendINI(String orderId) throws EbicsException, IOException {
    INIRequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender sender;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new INIRequestElement(session);
    request.build();
    request.validate();
    session.getConfiguration().getTraceManager().trace(request, session);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new KeyManagementResponseElement(sender.getResponseBody(), "INIResponse");
    response.build();
    session.getConfiguration().getTraceManager().trace(response,session);
    response.report();
  }

  /**
   * Sends the public part of the protocol keys to the bank.
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws IOException communication error
   * @throws EbicsException server generated error message
   */
  @Override
  public void sendHIA(String orderId) throws IOException, EbicsException {
    HIARequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender			sender;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new HIARequestElement(session);
    request.build();
    request.validate();
    session.getConfiguration().getTraceManager().trace(request,session);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new KeyManagementResponseElement(sender.getResponseBody(), "HIAResponse");
    response.build();
    session.getConfiguration().getTraceManager().trace(response,session);
    response.report();
  }

  /**
   * Sends encryption and authentication keys to the bank.
   * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
   * The keys will be activated immediately after successful completion of the transfer.
   * @throws IOException communication error
   * @throws GeneralSecurityException data decryption error
   * @throws EbicsException server generated error message
   */
  @Override
  public void sendHPB(PasswordCallback passwordCallback) throws IOException, GeneralSecurityException, EbicsException {
    HPBRequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender			sender;
    HPBResponseOrderDataElement		orderData;
    ContentFactory factory;
    KeyStoreManager keystoreManager;
    String				path;
    RSAPublicKey			e002PubKey;
    RSAPublicKey			x002PubKey;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new HPBRequestElement(session);
    request.build();
    request.validate();
    session.getConfiguration().getTraceManager().trace(request,session);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new KeyManagementResponseElement(sender.getResponseBody(), "HBPResponse");
    response.build();
    session.getConfiguration().getTraceManager().trace(response,session);
    response.report();
    factory = new ByteArrayContentFactory(Utils.unzip(session.getUser().decrypt(response.getOrderData(), response.getTransactionKey())));
    orderData = new HPBResponseOrderDataElement(factory);
    orderData.build();
    session.getConfiguration().getTraceManager().trace(orderData,session);
    keystoreManager = new KeyStoreManager();
    path = session.getConfiguration().getKeystoreDirectory(session.getUser());
    keystoreManager.load("" , passwordCallback.getPassword());

    if (session.getUser().getPartner().getBank().useCertificate())
    {
        e002PubKey = keystoreManager.getPublicKey(new ByteArrayInputStream(orderData.getBankE002Certificate()));
        x002PubKey = keystoreManager.getPublicKey(new ByteArrayInputStream(orderData.getBankX002Certificate()));
        session.getUser().getPartner().getBank().setBankKeys(e002PubKey, x002PubKey);
        session.getUser().getPartner().getBank().setDigests(KeyUtil.getKeyDigest(e002PubKey), KeyUtil.getKeyDigest(x002PubKey));
        keystoreManager.setCertificateEntry(session.getBankID() + "-E002", new ByteArrayInputStream(orderData.getBankE002Certificate()));
        keystoreManager.setCertificateEntry(session.getBankID() + "-X002", new ByteArrayInputStream(orderData.getBankX002Certificate()));
        keystoreManager.save(new FileOutputStream(path + File.separator + session.getBankID() + ".p12"));
    }
    else
    {
        e002PubKey = keystoreManager.getPublicKey(new BigInteger(orderData.getBankE002PublicKeyExponent()), new BigInteger(orderData.getBankE002PublicKeyModulus()));
        x002PubKey = keystoreManager.getPublicKey(new BigInteger(orderData.getBankX002PublicKeyExponent()), new BigInteger(orderData.getBankX002PublicKeyModulus()));
        session.getUser().getPartner().getBank().setBankKeys(e002PubKey, x002PubKey);
        session.getUser().getPartner().getBank().setDigests(KeyUtil.getKeyDigest(e002PubKey), KeyUtil.getKeyDigest(x002PubKey));
        //keystoreManager.setCertificateEntry(session.getBankID() + "-E002", new ByteArrayInputStream(orderData.getBankE002Certificate()));
        //keystoreManager.setCertificateEntry(session.getBankID() + "-X002", new ByteArrayInputStream(orderData.getBankX002Certificate()));
        keystoreManager.save(new FileOutputStream(path + File.separator + session.getBankID() + ".p12"));
    }
  }

  /**
   * Sends the SPR order to the bank.
   * After that you have to start over with sending INI and HIA.
   * @throws IOException Communication exception
   * @throws EbicsException Error message generated by the bank.
   */
  @Override
  public void lockAccess() throws IOException, EbicsException {
    HttpRequestSender			sender;
    SPRRequestElement			request;
    SPRResponseElement			response;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new SPRRequestElement(session);
    request.build();
    request.validate();
    session.getConfiguration().getTraceManager().trace(request,session);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new SPRResponseElement(sender.getResponseBody());
    response.build();
    session.getConfiguration().getTraceManager().trace(response,session);
    response.report();
  }
}
