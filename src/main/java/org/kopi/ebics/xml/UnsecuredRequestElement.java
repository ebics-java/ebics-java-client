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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.schema.h005.EmptyMutableHeaderType;
import org.kopi.ebics.schema.h005.OrderDetailsType;
import org.kopi.ebics.schema.h005.ProductElementType;
import org.kopi.ebics.schema.h005.UnsecuredRequestStaticHeaderType;
import org.kopi.ebics.schema.h005.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest;
import org.kopi.ebics.schema.h005.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import org.kopi.ebics.schema.h005.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header;
import org.kopi.ebics.schema.h005.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import org.kopi.ebics.schema.h005.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData;
import org.kopi.ebics.session.EbicsSession;

/**
 * The <code>UnsecuredRequestElement</code> is the common element
 * used for key management requests.
 *
 *
 */
public class UnsecuredRequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a Unsecured Request Element.
   * @param session the ebics session.
   * @param orderType the order type (INI | HIA).
   * @param orderId the order id, if null a random one is generated.
   */
  public UnsecuredRequestElement(EbicsSession session,
                                 EbicsOrderType orderType,
                                 String orderId,
                                 byte[] orderData)
  {
    super(session);
    this.orderType = orderType;
    this.orderId = orderId;
    this.orderData = orderData;
  }

  @Override
  public void build() throws EbicsException {
    Header 					header;
    Body 					body;
    EmptyMutableHeaderType 			mutable;
    UnsecuredRequestStaticHeaderType 		xstatic;
    ProductElementType 				productType;
    OrderDetailsType 				orderDetails;
    DataTransfer 				dataTransfer;
    OrderData 					orderData;
    EbicsUnsecuredRequest			request;

    orderDetails = EbicsXmlFactory.createOrderDetailsType(orderType.getCode());

    productType = EbicsXmlFactory.creatProductElementType(session.getProduct().getLanguage(),
	                                                  session.getProduct().getName());

    xstatic = EbicsXmlFactory.createUnsecuredRequestStaticHeaderType(session.getBankID(),
								     session.getUser().getPartner().getPartnerId(),
								     session.getUser().getUserId(),
	                                                             productType,
	                                                             orderDetails,
	                                                             session.getUser().getSecurityMedium());
    mutable = EbicsXmlFactory.createEmptyMutableHeaderType();

    header = EbicsXmlFactory.createHeader(true,
	                                  mutable,
	                                  xstatic);

    orderData = EbicsXmlFactory.createOrderData(this.orderData);
    dataTransfer = EbicsXmlFactory.createDataTransfer(orderData);
    body = EbicsXmlFactory.createBody(dataTransfer);
    request = EbicsXmlFactory.createEbicsUnsecuredRequest(header,
	                                                  body,
	                                                  session.getConfiguration().getRevision(),
	                                                  session.getConfiguration().getVersion());

    document = EbicsXmlFactory.createEbicsUnsecuredRequestDocument(request);
  }

  @Override
  public String getName() {
    return "UnsecuredRequest.xml";
  }



  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final EbicsOrderType orderType;
  private final String			orderId;
  private final byte[]			orderData;
  private static final long 		serialVersionUID = -3548730114599886711L;
}
