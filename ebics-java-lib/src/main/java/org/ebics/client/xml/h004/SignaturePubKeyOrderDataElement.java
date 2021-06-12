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

package org.ebics.client.xml.h004;

import org.ebics.client.exception.EbicsException;
import org.ebics.client.api.EbicsSession;
import org.ebics.schema.s001.PubKeyValueType;
import org.ebics.schema.s001.SignaturePubKeyInfoType;
import org.ebics.schema.s001.SignaturePubKeyOrderDataType;
import org.ebics.schema.xmldsig.RSAKeyValueType;
import org.ebics.schema.xmldsig.X509DataType;

import java.util.Calendar;


/**
 * The <code>SignaturePubKeyOrderDataElement</code> is the order data
 * component for the INI request.
 *
 * @author hachani
 *
 */
public class SignaturePubKeyOrderDataElement extends DefaultEbicsRootElement {

  /**
   * Creates a new Signature Public Key Order Data element.
   * @param session the current ebics session
   */
  public SignaturePubKeyOrderDataElement(EbicsSession session) {
    super(session);
  }

  @Override
  public void build() throws EbicsException {
    SignaturePubKeyInfoType		signaturePubKeyInfo;
    X509DataType 			x509Data;
    RSAKeyValueType 			rsaKeyValue;
    PubKeyValueType 			pubKeyValue;
    SignaturePubKeyOrderDataType	signaturePubKeyOrderData;

    x509Data = null;
    if (session.getUser().getPartner().getBank().getUseCertificate())
        x509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
	                                          session.getUserCert().getA005CertificateBytes());
    rsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(session.getUserCert().getA005PublicKey().getPublicExponent().toByteArray(),
	                                                session.getUserCert().getA005PublicKey().getModulus().toByteArray());
    pubKeyValue = EbicsXmlFactory.createPubKeyValueType(rsaKeyValue, Calendar.getInstance());
    signaturePubKeyInfo = EbicsXmlFactory.createSignaturePubKeyInfoType(x509Data,
	                                                                pubKeyValue,
	                                                                session.getConfiguration().getSignatureVersion());
    signaturePubKeyOrderData = EbicsXmlFactory.createSignaturePubKeyOrderData(signaturePubKeyInfo,
									      session.getUser().getPartner().getPartnerId(),
									      session.getUser().getUserId());
    document = EbicsXmlFactory.createSignaturePubKeyOrderDataDocument(signaturePubKeyOrderData);
  }

  @Override
  public String getName() {
    return "SignaturePubKeyOrderData.xml";
  }

  @Override
  public byte[] toByteArray() {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
    setSaveSuggestedPrefixes("http://www.ebics.org/S001", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long 		serialVersionUID = -5523105558015982970L;
}
