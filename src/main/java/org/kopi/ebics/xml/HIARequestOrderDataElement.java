/* Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
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

package org.kopi.ebics.xml;

import java.util.Calendar;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h003.AuthenticationPubKeyInfoType;
import org.kopi.ebics.schema.h003.EncryptionPubKeyInfoType;
import org.kopi.ebics.schema.h003.HIARequestOrderDataType;
import org.kopi.ebics.schema.h003.PubKeyValueType;
import org.kopi.ebics.schema.xmldsig.RSAKeyValueType;
import org.kopi.ebics.schema.xmldsig.X509DataType;
import org.kopi.ebics.session.EbicsSession;


/**
 * The <code>HIARequestOrderDataElement</code> is the element that contains
 * X002 and E002 keys information needed for a HIA request in order to send
 * the authentication and encryption user keys to the bank server.
 *
 * @author hachani
 *
 */
public class HIARequestOrderDataElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new HIA Request Order Data element
   * @param session the current ebics session
   */
  public HIARequestOrderDataElement(EbicsSession session) {
    super(session);
  }

  @Override
  public void build() throws EbicsException {
    HIARequestOrderDataType		request;
    AuthenticationPubKeyInfoType 	authenticationPubKeyInfo;
    EncryptionPubKeyInfoType 		encryptionPubKeyInfo;
    PubKeyValueType		 	encryptionPubKeyValue;
    X509DataType 			encryptionX509Data;
    RSAKeyValueType 			encryptionRsaKeyValue;
    PubKeyValueType		 	authPubKeyValue;
    X509DataType 			authX509Data;
    RSAKeyValueType 			AuthRsaKeyValue;

    encryptionX509Data = null;
    if (session.getUser().getPartner().getBank().useCertificate())
        encryptionX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDN(),
	                                                    session.getUser().getE002Certificate());
    encryptionRsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(session.getUser().getE002PublicKey().getPublicExponent().toByteArray(),
	                                                          session.getUser().getE002PublicKey().getModulus().toByteArray());
    encryptionPubKeyValue = EbicsXmlFactory.createH003PubKeyValueType(encryptionRsaKeyValue, Calendar.getInstance());
    encryptionPubKeyInfo = EbicsXmlFactory.createEncryptionPubKeyInfoType(session.getConfiguration().getEncryptionVersion(),
	                                                                  encryptionPubKeyValue,
	                                                                  encryptionX509Data);
    authX509Data = null;
    if (session.getUser().getPartner().getBank().useCertificate())
        authX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDN(),
	                                              session.getUser().getX002Certificate());
    AuthRsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(session.getUser().getX002PublicKey().getPublicExponent().toByteArray(),
							    session.getUser().getX002PublicKey().getModulus().toByteArray());
    authPubKeyValue = EbicsXmlFactory.createH003PubKeyValueType(AuthRsaKeyValue, Calendar.getInstance());
    authenticationPubKeyInfo = EbicsXmlFactory.createAuthenticationPubKeyInfoType(session.getConfiguration().getAuthenticationVersion(),
	                                                                          authPubKeyValue,
	                                                                          authX509Data);
    request = EbicsXmlFactory.createHIARequestOrderDataType(authenticationPubKeyInfo,
	                                                    encryptionPubKeyInfo,
	                                                    session.getUser().getPartner().getPartnerId(),
	                                                    session.getUser().getUserId());
    document = EbicsXmlFactory.createHIARequestOrderDataDocument(request);
  }

  @Override
  public String getName() {
    return "HIARequestOrderData.xml";
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

  private static final long 		serialVersionUID = -7333250823464659004L;
}
