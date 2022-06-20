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
import org.ebics.client.interfaces.ContentFactory;
import org.ebics.schema.h005.HPBResponseOrderDataDocument;
import org.ebics.schema.h005.HPBResponseOrderDataType;

/**
 * The <code>HPBResponseOrderDataElement</code> contains the public bank
 * keys in encrypted mode. The user should decrypt with his encryption
 * key to have the bank public keys.
 *
 * @author hachani
 *
 */
public class HPBResponseOrderDataElement extends DefaultResponseElement {

  /**
   * Creates a new <code>HPBResponseOrderDataElement</code> from a given
   * content factory.
   * @param factory the content factory.
   */
  public HPBResponseOrderDataElement(ContentFactory factory) {
    super(factory);
  }

  /**
   * Returns the authentication bank certificate.
   * @return the authentication bank certificate.
   */
  public byte[] getBankX002Certificate() {
    return response.getAuthenticationPubKeyInfo().getX509Data().getX509CertificateArray(0);
  }

  /**
   * Returns the encryption bank certificate.
   * @return the encryption bank certificate.
   */
  public byte[] getBankE002Certificate() {
    return response.getEncryptionPubKeyInfo().getX509Data().getX509CertificateArray(0);
  }

  @Override
  public void build() throws EbicsException {
    parse(factory);
    response = ((HPBResponseOrderDataDocument)document).getHPBResponseOrderData();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private HPBResponseOrderDataType	response;
  private static final long 		serialVersionUID = -1305363936881364049L;
}
