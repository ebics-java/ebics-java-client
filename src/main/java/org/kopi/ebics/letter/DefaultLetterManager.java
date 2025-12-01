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
import org.kopi.ebics.interfaces.InitLetter;
import org.kopi.ebics.interfaces.LetterManager;


/**
 * The <code>DefaultLetterManager</code> is a simple way
 * to manage initialization letters.
 *
 *
 */
public class DefaultLetterManager implements LetterManager {

  /**
   * Constructs a new <code>LetterManager</code>
   * @param locale the application locale.
   */
  public DefaultLetterManager(Locale locale) {
    this.locale = locale;
  }

  @Override
  public InitLetter createA005Letter(EbicsUser user)
    throws GeneralSecurityException, IOException, EbicsException
  {
        A005Letter			letter;

        letter = new A005Letter(locale);
        letter.create(user);
        return letter;
  }

  @Override
  public InitLetter createE002Letter(EbicsUser user)
    throws GeneralSecurityException, IOException, EbicsException
  {
        E002Letter			letter;

        letter = new E002Letter(locale);
        letter.create(user);
        return letter;
  }

  @Override
  public InitLetter createX002Letter(EbicsUser user)
    throws GeneralSecurityException, IOException, EbicsException
  {
        X002Letter			letter;

        letter = new X002Letter(locale);
        letter.create(user);
        return letter;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final Locale				locale;
}
