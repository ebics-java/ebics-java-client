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

package org.ebics.client.xml.h005;

import org.ebics.client.exception.EbicsException;
import org.ebics.client.api.EbicsSession;
import org.ebics.schema.h005.AuthenticationPubKeyInfoType;
import org.ebics.schema.h005.EncryptionPubKeyInfoType;
import org.ebics.schema.h005.HIARequestOrderDataType;
import org.ebics.schema.xmldsig.X509DataType;


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
    X509DataType 			encryptionX509Data;
    X509DataType 			authX509Data;

    encryptionX509Data = null;
    if (session.getUser().getUseCertificate())
        encryptionX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
	                                                    session.getUserCert().getE002CertificateBytes());
    encryptionPubKeyInfo = EbicsXmlFactory.createEncryptionPubKeyInfoType(session.getConfiguration().getEncryptionVersion(),
	                                                                  encryptionX509Data);
    authX509Data = null;
    if (session.getUser().getUseCertificate())
        authX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
	                                              session.getUserCert().getX002CertificateBytes());
    authenticationPubKeyInfo = EbicsXmlFactory.createAuthenticationPubKeyInfoType(session.getConfiguration().getAuthenticationVersion(),
	                                                                          authX509Data);
    request = EbicsXmlFactory.createHIARequestOrderDataType(authenticationPubKeyInfo,
	                                                    encryptionPubKeyInfo,
	                                                    session.getUser().getPartner().getPartnerId(),
	                                                    session.getUser().getUserId());
    document = EbicsXmlFactory.createHIARequestOrderDataDocument(request);
  }

  @Override
  public byte[] toByteArray() {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
    setSaveSuggestedPrefixes("http://www.ebics.org/S002", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long 		serialVersionUID = -7333250823464659004L;
}
