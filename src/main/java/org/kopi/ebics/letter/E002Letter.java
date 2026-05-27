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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsUser;


/**
 * The <code>E002Letter</code> is the initialization letter
 * for the encryption certificate.
 *
 *
 */
public class E002Letter extends AbstractInitLetter {

  /**
   * Constructs a new <code>E002Letter</code>
   * @param locale the application locale
   */
  public E002Letter(Locale locale) {
    super(locale);
  }

    @Override
    public void create(EbicsUser user) throws GeneralSecurityException, IOException, EbicsException {
        // EBICS 3.0 (H005): the HIA letter must carry the SHA-256 hash of the
        // DER-encoded encryption certificate (spec ch. 4.4.1.2.3), matching the
        // X.509 certificate transmitted in the HIA request.
        build(user.getPartner().getBank().getHostId(),
                user.getPartner().getBank().getName(),
                user.getUserId(),
                user.getName(),
                user.getPartner().getPartnerId(),
                getString("HIALetter.e002.version"),
                getString("HIALetter.e002.certificate"),
                chunkedBase64(user.getE002Certificate()),
                getString("HIALetter.e002.digest"),
                getHash(user.getE002Certificate()));
    }

  @Override
  public String getTitle() {
    return getString("HIALetter.e002.title");
  }

  @Override
  public String getName() {
    return getString("HIALetter.e002.name") + ".txt";
  }
}
