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

import java.util.Calendar;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.kopi.ebics.client.EbicsDownloadParams;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.schema.h005.BTDOrderParamsDocument;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.kopi.ebics.schema.h005.MutableHeaderType;
import org.kopi.ebics.schema.h005.StandardOrderParamsDocument;
import org.kopi.ebics.schema.h005.StandardOrderParamsType;
import org.kopi.ebics.schema.h005.StaticHeaderOrderDetailsType;
import org.kopi.ebics.schema.h005.StaticHeaderType;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.kopi.ebics.schema.h005.StaticHeaderType.Product;
import org.kopi.ebics.session.EbicsSession;


/**
 * The <code>DInitializationRequestElement</code> is the common initialization
 * for all ebics downloads.
 *
 *
 */
public class DownloadInitializationRequestElement extends InitializationRequestElement {

  /**
   * Constructs a new <code>DInitializationRequestElement</code> for downloads initializations.
   * @param session the current ebics session
   * @param type the download order type (FDL, HTD, HPD)
   */
  public DownloadInitializationRequestElement(EbicsSession session,
                                       EbicsOrderType type) {
    this(session, type, null);
  }

  /**
   * Constructs a new <code>DInitializationRequestElement</code> for downloads initializations.
   * @param session the current ebics session
   * @param type the download order type (FDL, HTD, HPD)
   * @param downloadParams optional service parameters; with a service name set the request is
   *        sent as an EBICS 3.0 BTD order, otherwise the legacy order type is kept
   */
  public DownloadInitializationRequestElement(EbicsSession session,
                                       EbicsOrderType type,
                                       EbicsDownloadParams downloadParams) {
    super(session, type, generateName(type));
    this.downloadParams = downloadParams;
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
    StaticHeaderOrderDetailsType 	orderDetails;

    mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
    product = EbicsXmlFactory.createProduct(session.getProduct().getLanguage(), session.getProduct().getName());
    authentication = EbicsXmlFactory.createAuthentication(session.getConfiguration().getAuthenticationVersion(),
	                                                  "http://www.w3.org/2001/04/xmlenc#sha256",
	                                                  decodeHex(session.getUser().getPartner().getBank().getX002Digest()));
    encryption = EbicsXmlFactory.createEncryption(session.getConfiguration().getEncryptionVersion(),
	                                          "http://www.w3.org/2001/04/xmlenc#sha256",
	                                          decodeHex(session.getUser().getPartner().getBank().getE002Digest()));
    bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);

      var type = StaticHeaderOrderDetailsType.AdminOrderType.Factory.newInstance();

      XmlObject orderParamsType;
      SchemaType orderParamsSchema;

      if (downloadParams != null && downloadParams.isBtd()) {
          // EBICS 3.0: the business transaction goes into the service block, the admin order
          // type is always BTD.
          type.setStringValue("BTD");
          orderParamsType = EbicsXmlFactory.createBTDParams(
              downloadParams.serviceName(), downloadParams.scope(), downloadParams.option(),
              downloadParams.messageName(), downloadParams.messageVersion(),
              downloadParams.containerType(), downloadParams.startDate(),
              downloadParams.endDate());
          orderParamsSchema = BTDOrderParamsDocument.type;
      } else {
          type.setStringValue(this.getType());
          StandardOrderParamsType standardOrderParamsType =
              EbicsXmlFactory.createStandardOrderParamsType();
          // EbicsDownloadParams guarantees the range is either absent or complete.
          if (downloadParams != null && downloadParams.startDate() != null) {
              standardOrderParamsType.setDateRange(EbicsXmlFactory.createDateRange(
                  downloadParams.startDate(), downloadParams.endDate()));
          }
          orderParamsType = standardOrderParamsType;
          orderParamsSchema = StandardOrderParamsDocument.type;
      }

      //FIXME Some banks cannot handle OrderID element in download process. Add parameter in configuration!!!
      orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(null,//session.getUser().getPartner().nextOrderId(),
            type,
	                                                                orderParamsType,
          orderParamsSchema);

    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(),
                                                     nonce,
                                                     session.getUser().getPartner().getPartnerId(),
                                                     product,
                                                     session.getUser().getSecurityMedium(),
                                                     session.getUser().getUserId(),
                                                     Calendar.getInstance(),
                                                     orderDetails,
                                                     bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createEbicsRequestBody();
    request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
                                                 session.getConfiguration().getVersion(),
                                                 header,
                                                 body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  private final EbicsDownloadParams downloadParams;
  private static final long 			serialVersionUID = 3776072549761880272L;
}
