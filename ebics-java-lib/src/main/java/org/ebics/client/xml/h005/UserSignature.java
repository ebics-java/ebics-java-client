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
import org.ebics.client.user.base.EbicsUser;
import org.ebics.schema.s002.OrderSignatureDataType;
import org.ebics.schema.s002.UserSignatureDataSigBookType;

import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * A root EBICS element representing the user signature
 * element. The user data is signed with the user signature
 * key sent in the INI request to the EBICS bank server
 *
 * @author hachani
 *
 */
public class UserSignature extends DefaultEbicsRootElement {

  /**
   * Constructs a new <code>UserSignature</code> element for
   * an Ebics user and a data to sign
   * @param user the ebics user
   * @param signatureVersion the signature version
   * @param toSign the data to be signed
   */
  public UserSignature(EbicsUser user,
                       String name,
                       String signatureVersion,
                       byte[] toSign)
  {
    this.user = user;
    this.toSign = toSign;
    this.name = name;
    this.signatureVersion = signatureVersion;
  }

  @Override
  public void build() throws EbicsException {
    UserSignatureDataSigBookType 	userSignatureData;
    OrderSignatureDataType		orderSignatureData;
    byte[]				signature;

    try {
      signature = user.sign(toSign);
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    } catch (GeneralSecurityException e) {
      throw new EbicsException(e.getMessage());
    }

    orderSignatureData = EbicsXmlFactory.createOrderSignatureDataType(signatureVersion,
                                                                      user.getPartner().getPartnerId(),
                                                                      user.getUserInfo().getUserId(),
                                                                      signature);
    userSignatureData = EbicsXmlFactory.createUserSignatureDataSigBookType(new OrderSignatureDataType[] {orderSignatureData});
    document = EbicsXmlFactory.createUserSignatureDataDocument(userSignatureData);
  }

  @Override
  public String getName() {
    return name + ".xml";
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/S002", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsUser user;
  private String 			signatureVersion;
  private byte[]			toSign;
  private String			name;
  private static final long 		serialVersionUID = 2992372604876703738L;
}
