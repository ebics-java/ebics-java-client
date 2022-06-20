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

import org.ebics.client.exception.EbicsException;
import org.ebics.client.api.EbicsSession;
import org.ebics.client.utils.Utils;
import org.ebics.schema.h005.EbicsRequestDocument;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body.TransferReceipt;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h005.MutableHeaderType;
import org.ebics.schema.h005.StaticHeaderType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


/**
 * The <code>ReceiptRequestElement</code> is the element containing the
 * receipt request to tell the server bank that all segments are received.
 *
 * @author Hachani
 *
 */
public class ReceiptRequestElement extends DefaultEbicsRootElement {

  /**
   * Construct a new <code>ReceiptRequestElement</code> element.
   * @param session the current ebics session
   * @param name the element name
   */
  public ReceiptRequestElement(EbicsSession session,
                               byte[] transactionId)
  {
    super(session);
    this.transactionId = transactionId;
  }

  @Override
  public void build() throws EbicsException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    StaticHeaderType 			xstatic;
    TransferReceipt			transferReceipt;
    SignedInfo signedInfo;

    mutable = EbicsXmlFactory.createMutableHeaderType("Receipt", null);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    transferReceipt = EbicsXmlFactory.createTransferReceipt(true, 0);
    body = EbicsXmlFactory.createEbicsRequestBody(transferReceipt);
    request = EbicsXmlFactory.createEbicsRequest(header, body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
    signedInfo = new SignedInfo(session.getUserCert(), getDigest());
    signedInfo.build();
    ((EbicsRequestDocument)document).getEbicsRequest().setAuthSignature(signedInfo.getSignatureType());
    ((EbicsRequestDocument)document).getEbicsRequest().getAuthSignature().setSignatureValue(EbicsXmlFactory.createSignatureValueType(signedInfo.sign(toByteArray())));
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/h005", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[] 			transactionId;
  private static final long 		serialVersionUID = -1969616441705744725L;
}
