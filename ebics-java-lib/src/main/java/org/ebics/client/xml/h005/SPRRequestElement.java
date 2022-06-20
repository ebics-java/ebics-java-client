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

package org.ebics.client.xml.h005;

import org.ebics.client.api.EbicsSession;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.order.EbicsOrder;
import org.ebics.client.utils.CryptoUtils;
import org.ebics.client.utils.Utils;
import org.ebics.schema.h005.*;
import org.ebics.schema.h005.DataEncryptionInfoType.EncryptionPubKeyDigest;
import org.ebics.schema.h005.DataTransferRequestType.DataEncryptionInfo;
import org.ebics.schema.h005.DataTransferRequestType.SignatureData;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h005.StaticHeaderOrderDetailsType.AdminOrderType;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.ebics.schema.h005.StaticHeaderType.Product;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;
import java.util.Collections;


/**
 * The <code>SPRRequestElement</code> is the request element
 * for revoking a subscriber
 *
 * @author Hachani
 *
 */
public class SPRRequestElement extends InitializationRequestElement {

  /**
   * Constructs a new SPR request element.
   * @param session the current ebics session.
   */
  public SPRRequestElement(EbicsSession session) throws EbicsException {
    super(session, new EbicsOrder(EbicsAdminOrderType.SPR, Collections.emptyMap()));
    keySpec = new SecretKeySpec(nonce, "EAS");
  }

  @Override
  public void buildInitialization() throws EbicsException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    StaticHeaderType 			xstatic;
    Product 				product;
    BankPubKeyDigests 			bankPubKeyDigests;
    Authentication 			authentication;
    Encryption 				encryption;
    DataTransferRequestType 		dataTransfer;
    DataEncryptionInfo 			dataEncryptionInfo;
    SignatureData 			signatureData;
    EncryptionPubKeyDigest 		encryptionPubKeyDigest;
    StaticHeaderOrderDetailsType 	orderDetails;
    AdminOrderType 				orderType;
    StandardOrderParamsType		standardOrderParamsType;
    UserSignature userSignature;
    DataDigestType dataDigest;

    userSignature = new UserSignature(session.getUser(), session.getUserCert(),
				      generateName("SIG"),
	                              session.getConfiguration().getSignatureVersion(),
	                              " ".getBytes());
    userSignature.build();
    userSignature.validate();

    mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
    product = EbicsXmlFactory.createProduct(session.getProduct());
    authentication = EbicsXmlFactory.createAuthentication(session.getConfiguration().getAuthenticationVersion(),
	                                                  "http://www.w3.org/2001/04/xmlenc#sha256",
	                                                  decodeHex(session.getBankCert().getX002Digest()));
    encryption = EbicsXmlFactory.createEncryption(session.getConfiguration().getEncryptionVersion(),
	                                          "http://www.w3.org/2001/04/xmlenc#sha256",
	                                          decodeHex(session.getBankCert().getE002Digest()));
    bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);
    orderType = EbicsXmlFactory.createAdminOrderType(ebicsOrder.getAdminOrderType().toString());
    standardOrderParamsType = EbicsXmlFactory.createStandardOrderParamsType();
    orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(null,
	                                                              orderType,
	                                                              standardOrderParamsType);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(),
	                                             nonce,
	                                             0,
	                                             session.getUser().getPartner().getPartnerId(),
	                                             product,
	                                             session.getUser().getSecurityMedium(),
	                                             session.getUser().getUserId(),
	                                             Calendar.getInstance(),
	                                             orderDetails,
	                                             bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    encryptionPubKeyDigest = EbicsXmlFactory.createEncryptionPubKeyDigest(session.getConfiguration().getEncryptionVersion(),
								          "http://www.w3.org/2001/04/xmlenc#sha256",
								          decodeHex(session.getBankCert().getE002Digest()));
    signatureData = EbicsXmlFactory.createSignatureData(true, CryptoUtils.encrypt(Utils.zip(userSignature.prettyPrint()), keySpec));
    dataEncryptionInfo = EbicsXmlFactory.createDataEncryptionInfo(true,
	                                                          encryptionPubKeyDigest,
	                                                          generateTransactionKey());
    dataDigest = EbicsXmlFactory.createDataDigestType(session.getConfiguration().getSignatureVersion(), null);
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(dataEncryptionInfo, signatureData, dataDigest);
    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
    request = EbicsXmlFactory.createEbicsRequest(header, body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private SecretKeySpec			keySpec;
  private static final long 		serialVersionUID = -6742241777786111337L;
}
