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

import java.io.Serializable;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

/**
 * Details about EBICS communication with a given bank.
 *
 *
 */
public interface EbicsBank extends Serializable {

  /**
   * Returns the URL needed for communication to the bank.
   * @return the URL needed for communication to the bank.
   */
  URL getURL();

  /**
   * 
   */
  boolean useCertificate();
  
  /**
   * 
   */
  void setUseCertificate(boolean useCertificate);
  
  /**
   * Returns the encryption key digest you have obtained from the bank.
   * Ensure that nobody was able to modify the digest on its way from the bank to you.
   * @return the encryption key digest you have obtained from the bank.
   */
  byte[] getE002Digest();

  /**
   * Returns the authentication key digest you have obtained from the bank.
   * Ensure that nobody was able to modify the digest on its way from the bank to you.
   * @return the authentication key digest you have obtained from the bank.
   */
  byte[] getX002Digest();

  /**
   * Returns the banks encryption key.
   * @return the banks encryption key.
   */
  RSAPublicKey getE002Key();

  /**
   * Returns the banks authentication key.
   * @return the banks authentication key.
   */
  RSAPublicKey getX002Key();

  /**
   * Returns the bank's id.
   * @return the bank's id.
   */
  String getHostId();

  /**
   * Returns the bank's name.
   * @return the bank's name.
   */
  String getName();

  /**
   * Keys have been fetched from the bank.
   * The getters for the appropriate attributes should return the given values from now on.
   * For the sake of performance the values should be persisted for later usage.
   *
   * @param e002Key the banks encryption key.
   * @param x002Key the banks authentication key.
   */
  void setBankKeys(RSAPublicKey e002Key, RSAPublicKey x002Key);

  /**
   * Sets the bank digests.
   * @param e002Digest encryption digest
   * @param x002Digest authentication digest
   */
  void setDigests(byte[] e002Digest, byte[] x002Digest);
}
