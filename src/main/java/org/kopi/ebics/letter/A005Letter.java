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

package org.kopi.ebics.letter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsUser;


/**
 * The <code>A005Letter</code> is the initialization letter
 * for the signature certificate
 *
 *
 */
public class A005Letter extends AbstractInitLetter {

  /**
   * Constructs a new <code>A005Letter</code>
   * @param locale the application local
   */
  public A005Letter(Locale locale) {
    super(locale);
  }

    @Override
    public void create(EbicsUser user) throws GeneralSecurityException, IOException, EbicsException {
        if (user.getPartner().getBank().useCertificate()) {
            build(user.getPartner().getBank().getHostId(),
                    user.getPartner().getBank().getName(),
                    user.getUserId(),
                    user.getName(),
                    user.getPartner().getPartnerId(),
                    getString("INILetter.version"),
                    getString("INILetter.certificate"),
                    Base64.encodeBase64(user.getA005Certificate(), true),
                    getString("INILetter.digest"),
                    getHash(user.getA005Certificate()));
        } else {
            build(user.getPartner().getBank().getHostId(),
                    user.getPartner().getBank().getName(),
                    user.getUserId(),
                    user.getName(),
                    user.getPartner().getPartnerId(),
                    getString("INILetter.version"),
                    getString("INILetter.certificate"),
                    null,
                    getString("INILetter.digest"),
                    getHash(user.getA005PublicKey()));
        }
    }

  @Override
  public String getTitle() {
    return getString("INILetter.title");
  }

  @Override
  public String getName() {
    return getString("INILetter.name") + ".txt";
  }
}
