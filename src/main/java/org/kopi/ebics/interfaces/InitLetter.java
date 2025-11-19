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

package org.kopi.ebics.interfaces;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.kopi.ebics.exception.EbicsException;


/**
 * The <code>InitLetter</code> is an abstract initialization
 * letter. The INI, HIA and HPB letters should be an implementation
 * of the <code>InitLetter</code>
 *
 *
 */
public interface InitLetter {

  /**
   * Creates an <code>InitLetter</code> for a given <code>EbicsUser</code>
   * @param user the ebics user.
   * @throws EbicsException
   * @throws IOException
   * @throws GeneralSecurityException
   */
  void create(EbicsUser user)
    throws GeneralSecurityException, IOException, EbicsException;

  /**
   * Saves the <code>InitLetter</code> to the given output stream.
   * @param output the output stream.
   * @throws IOException Save error.
   */
  void writeTo(OutputStream output) throws IOException;

  /**
   * Returns the initialization letter title.
   * @return the letter title.
   */
  String getTitle();

  /**
   * Returns the letter name.
   * @return the letter name.
   */
  String getName();
}
