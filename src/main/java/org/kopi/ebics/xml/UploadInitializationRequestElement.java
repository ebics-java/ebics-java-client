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

package org.kopi.ebics.xml;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlObject;
import org.kopi.ebics.client.EbicsUploadParams;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.io.Splitter;
import org.kopi.ebics.schema.h005.BTUOrderParamsDocument;
import org.kopi.ebics.schema.h005.DataEncryptionInfoType.EncryptionPubKeyDigest;
import org.kopi.ebics.schema.h005.DataTransferRequestType;
import org.kopi.ebics.schema.h005.DataTransferRequestType.DataEncryptionInfo;
import org.kopi.ebics.schema.h005.DataTransferRequestType.SignatureData;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.kopi.ebics.schema.h005.MutableHeaderType;
import org.kopi.ebics.schema.h005.StandardOrderParamsType;
import org.kopi.ebics.schema.h005.StaticHeaderOrderDetailsType;
import org.kopi.ebics.schema.h005.StaticHeaderType;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.kopi.ebics.schema.h005.StaticHeaderType.Product;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.Utils;


/**
 * The <code>UInitializationRequestElement</code> is the common initialization
 * element for all ebics file uploads.
 *
 *
 */
public class UploadInitializationRequestElement extends InitializationRequestElement {


/**
   * Constructs a new <code>UInitializationRequestElement</code> for uploads initializations.
   * @param session the current ebics session.
   * @param orderType the upload order type
   * @param userData the user data to be uploaded
 */
    public UploadInitializationRequestElement(EbicsSession session, EbicsOrderType orderType,
        EbicsUploadParams params,
        byte[] userData) {
        super(session, orderType, generateName(orderType));
        setSaveSuggestedPrefixes("urn:org:ebics:H005", "");
        this.userData = userData;
        splitter = new Splitter(userData);
        this.uploadParams = params;
    }

    @Override
    public void buildInitialization() throws EbicsException {
        userSignature = new UserSignature(session.getUser(), generateName("UserSignature"),
            session.getConfiguration().getSignatureVersion(), userData);
        userSignature.build();
        userSignature.validate();

        splitter.readInput(session.getConfiguration().isCompressionEnabled(), keySpec);

        var mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
        var product = EbicsXmlFactory.createProduct(session.getProduct().getLanguage(),
            session.getProduct().getName());
        var authentication = EbicsXmlFactory.createAuthentication(
            session.getConfiguration().getAuthenticationVersion(),
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(session.getUser().getPartner().getBank().getX002Digest()));
        var encryption = EbicsXmlFactory.createEncryption(
            session.getConfiguration().getEncryptionVersion(),
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(session.getUser().getPartner().getBank().getE002Digest()));
        var bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);

        String nextOrderId = uploadParams.orderId();

        var type = StaticHeaderOrderDetailsType.AdminOrderType.Factory.newInstance();
        type.setStringValue(this.getType());

        var orderParamsType = (XmlObject) EbicsXmlFactory.createStandardOrderParamsType();
        var orderParamsSchema = StandardOrderParamsType.type;

        if (uploadParams.orderParams() != null) {
            var p = uploadParams.orderParams();
            orderParamsType = EbicsXmlFactory.createBTUParams(p.serviceName(), p.scope(),
                p.option(), p.messageName(), p.messageVersion(), p.signatureFlag());
            orderParamsSchema = BTUOrderParamsDocument.type;
        }

        StaticHeaderOrderDetailsType orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(
            nextOrderId, type, orderParamsType,orderParamsSchema);

        var xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), nonce,
            splitter.getSegmentNumber(), session.getUser().getPartner().getPartnerId(), product,
            session.getUser().getSecurityMedium(), session.getUser().getUserId(),
            Calendar.getInstance(), orderDetails, bankPubKeyDigests);
        var header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
        var encryptionPubKeyDigest = EbicsXmlFactory.createEncryptionPubKeyDigest(
            session.getConfiguration().getEncryptionVersion(),
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(session.getUser().getPartner().getBank().getE002Digest()));
        var signatureData = EbicsXmlFactory.createSignatureData(true,
            Utils.encrypt(Utils.zip(userSignature.prettyPrint()), keySpec));
        var dataEncryptionInfo = EbicsXmlFactory.createDataEncryptionInfo(true,
            encryptionPubKeyDigest, generateTransactionKey());

        String digest;
        try {
            // TODO: check if this is correct
            digest = Base64.encodeBase64String(MessageDigest.getInstance("SHA-256", "BC").digest(this.userData));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new EbicsException(e);
        }
        var dataTransfer = EbicsXmlFactory.createDataTransferRequestType(dataEncryptionInfo,
            signatureData, digest);
        var body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
        var request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
            session.getConfiguration().getVersion(), header, body);
        document = EbicsXmlFactory.createEbicsRequestDocument(request);
    }

  /**
   * Returns the user signature data.
   * @return the user signature data.
   */
  public UserSignature getUserSignature() {
    return userSignature;
  }

  /**
   * Returns the content of a given segment.
   * @param segment the segment number
   * @return the content of the given segment
   */
  public ContentFactory getContent(int segment) {
    return splitter.getContent(segment);
  }

  /**
   * Returns the total segment number.
   * @return the total segment number.
   */
  public int getSegmentNumber() {
    return splitter.getSegmentNumber();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------
  private final EbicsUploadParams uploadParams;
  private final byte[] userData;
  private UserSignature			userSignature;
  private final Splitter splitter;
  private static final long 		serialVersionUID = -8083183483311283608L;
}
