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

package org.kopi.ebics.io;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.utils.Utils;


/**
 * A simple mean to join downloaded segments from the
 * bank ebics server.
 *
 *
 */
public class Joiner {

  /**
   * Constructs a new <code>Joiner</code> object.
   * @param user the ebics user.
   */
  public Joiner(EbicsUser user) {
    this.user = user;
    buffer = new ByteArrayOutputStream();
  }

  public void append(byte[] data) throws EbicsException {
    try {
      buffer.write(data);
      buffer.flush();
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Writes the joined part to an output stream.
   * @param output the output stream.
   * @param transactionKey the transaction key
   * @throws EbicsException
   */
  public void writeTo(OutputStream output, byte[] transactionKey)
    throws EbicsException
  {
    try {
      byte[]		decrypted;

      buffer.close();
      decrypted = user.decrypt(buffer.toByteArray(), transactionKey);
      output.write(Utils.unzip(decrypted));
    } catch (GeneralSecurityException | IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final EbicsUser			user;
  private final ByteArrayOutputStream		buffer;
}
