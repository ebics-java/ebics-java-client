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
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import org.kopi.ebics.exception.EbicsException;


/**
 * Things an EBICS user must be able to perform.
 *
 *
 */
public interface EbicsUser {

  /**
   * Returns the public part of the signature key.
   * @return the public part of the signature key.
   */
  RSAPublicKey getA005PublicKey();

  /**
   * Returns the public part of the encryption key.
   * @return the public part of the encryption key.
   */
  RSAPublicKey getE002PublicKey();

  /**
   * Return the public part of the transport authentication key.
   * @return the public part of the transport authentication key.
   */
  RSAPublicKey getX002PublicKey();

  /**
   * Returns the signature certificate.
   * @return the encryption certificate.
   * @throws EbicsException
   */
  byte[] getA005Certificate() throws EbicsException;

  /**
   * Returns the authentication certificate.
   * @return the encryption certificate.
   * @throws EbicsException
   */
  byte[] getX002Certificate() throws EbicsException;

  /**
   * Returns the encryption certificate.
   * @return the encryption certificate.
   * @throws EbicsException
   */
  byte[] getE002Certificate() throws EbicsException;

  /**
   * Sets the signature certificate.
   * @param a005certificate the signature certificate.
   */
  void setA005Certificate(X509Certificate a005certificate);

  /**
   * Sets the authentication certificate.
   * @param x002certificate the authentication certificate.
   */
  void setX002Certificate(X509Certificate x002certificate);

  /**
   * Sets the encryption certificate.
   * @param e002certificate the encryption certificate.
   */
  void setE002Certificate(X509Certificate e002certificate);

  /**
   * Sets the signature private key
   * @param a005Key the signature private key
   */
  void setA005PrivateKey(PrivateKey a005Key);

  /**
   * Sets the authentication private key
   * @param x002Key the authentication private key
   */
  void setX002PrivateKey(PrivateKey x002Key);

  /**
   * Sets the encryption private key
   * @param e002Key the encryption private key
   */
  void setE002PrivateKey(PrivateKey e002Key);

  /**
   * Returns the type to security medium used to store the A005 key.
   * @return the type to security medium used to store the A005 key.
   */
  String getSecurityMedium();

  /**
   * Returns the customer in whose name we operate.
   * @return the customer in whose name we operate.
   */
  EbicsPartner getPartner();

  /**
   * Returns the (bank provided) user id.
   * @return the (bank provided) user id.
   */
  String getUserId();

  /**
   * Returns the user name.
   * @return the user name.
   */
  String getName();

  /**
   * Returns the distinguished name
   * @return the distinguished name
   */
  String getDN();

  /**
   * Returns the password callback handler for the current user.
   * @return the password callback handler.
   */
  PasswordCallback getPasswordCallback();

  /**
   * Signs the given digest with the private X002 key.
   * @param digest the given digest
   * @return the signature.
   * @throws GeneralSecurityException
   */
  byte[] authenticate(byte[] digest) throws GeneralSecurityException;

  /**
   * Signs the given digest with the private A005 key.
   * @param digest
   * @return the signature
   * @throws IOException
   * @throws GeneralSecurityException 
   */
  byte[] sign(byte[] digest) throws IOException, GeneralSecurityException;

  /**
   * Uses the E001 key to decrypt the given secret key.
   * @param encryptedKey the given secret key
   * @param transactionKey a given transaction key
   * @return the decrypted key;
   * @throws GeneralSecurityException
   * @throws IOException
   * @throws EbicsException 
   */
  byte[] decrypt(byte[] encryptedKey, byte[] transactionKey)
    throws GeneralSecurityException, IOException, EbicsException;
}
