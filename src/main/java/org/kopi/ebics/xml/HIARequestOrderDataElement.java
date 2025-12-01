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
 */

package org.kopi.ebics.xml;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h005.AuthenticationPubKeyInfoType;
import org.kopi.ebics.schema.h005.EncryptionPubKeyInfoType;
import org.kopi.ebics.schema.h005.HIARequestOrderDataType;
import org.kopi.ebics.schema.xmldsig.X509DataType;
import org.kopi.ebics.session.EbicsSession;

/**
 * The <code>HIARequestOrderDataElement</code> is the element that contains
 * X002 and E002 keys information needed for a HIA request in order to send
 * the authentication and encryption user keys to the bank server.
 *
 *
 */
public class HIARequestOrderDataElement extends DefaultEbicsRootElement {

    /**
     * Constructs a new HIA Request Order Data element
     *
     * @param session the current ebics session
     */
    public HIARequestOrderDataElement(EbicsSession session) {
        super(session);
    }

    @Override
    public void build() throws EbicsException {
        X509DataType encryptionX509Data = EbicsXmlFactory.createX509DataType(
            session.getUser().getDN(), session.getUser().getE002Certificate());

        X509DataType authX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDN(),
            session.getUser().getX002Certificate());


        var encryptionPubKeyInfo = EbicsXmlFactory.createEncryptionPubKeyInfoType(session.getConfiguration().getEncryptionVersion(),
            encryptionX509Data);
        AuthenticationPubKeyInfoType authenticationPubKeyInfo = EbicsXmlFactory.createAuthenticationPubKeyInfoType(
            session.getConfiguration().getAuthenticationVersion(), authX509Data);

        HIARequestOrderDataType request = EbicsXmlFactory.createHIARequestOrderDataType(
            authenticationPubKeyInfo, encryptionPubKeyInfo,
            session.getUser().getPartner().getPartnerId(), session.getUser().getUserId());
        document = EbicsXmlFactory.createHIARequestOrderDataDocument(request);
    }

    @Override
    public String getName() {
        return "HIARequestOrderData.xml";
    }

    @Override
    public byte[] toByteArray() {
        addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
        setSaveSuggestedPrefixes("urn:org:ebics:H005", "");

        return super.toByteArray();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private static final long serialVersionUID = -7333250823464659004L;
}
