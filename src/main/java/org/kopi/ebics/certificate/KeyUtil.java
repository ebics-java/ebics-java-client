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

package org.kopi.ebics.certificate;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.codec.binary.Hex;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.utils.Utils;

/**
 * Some key utilities
 *
 *
 */
public final class KeyUtil {

    private KeyUtil() {
    }

  /**
   * Generates a <code>KeyPair</code> in RSA format.
   *
   * @param keyLen - key size
   * @return KeyPair the key pair
   * @throws NoSuchAlgorithmException
   */
  public static KeyPair makeKeyPair(int keyLen) throws NoSuchAlgorithmException {
    KeyPairGenerator 		keyGen;

    keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(keyLen, Utils.secureRandom);

    return keyGen.generateKeyPair();

  }

  /**
   * Returns the digest value of a given public key.
   *
   *
   * <p>In Version “H003” of the EBICS protocol the ES of the financial:
   *
   * <p>The SHA-256 hash values of the financial institution's public keys for X002 and E002 are
   * composed by concatenating the exponent with a blank character and the modulus in hexadecimal
   * representation (using lower case letters) without leading zero (as to the hexadecimal
   * representation). The resulting string has to be converted into a byte array based on US ASCII
   * code.
   *
   * @param publicKey the public key
   * @return the digest value
   * @throws EbicsException
   */
  public static byte[] getKeyDigest(RSAPublicKey publicKey) throws EbicsException {
    String			modulus;
    String			exponent;
    String			hash;
    byte[]			digest;

    exponent = Hex.encodeHexString(publicKey.getPublicExponent().toByteArray());
    modulus =  Hex.encodeHexString(removeFirstByte(publicKey.getModulus().toByteArray()));
    hash = exponent + " " + modulus;

    if (hash.charAt(0) == '0') {
      hash = hash.substring(1);
    }

    try {
      digest = MessageDigest.getInstance("SHA-256", "BC").digest(hash.getBytes(
          StandardCharsets.US_ASCII));
    } catch (GeneralSecurityException e) {
      throw new EbicsException(e.getMessage());
    }

      return new String(Hex.encodeHex(digest, false)).getBytes();
  }

  /**
   * Remove the first byte of an byte array
   *
   * @return the array
   * */
  private static byte[] removeFirstByte(byte[] byteArray) {
      byte[] b = new byte[byteArray.length - 1];
      System.arraycopy(byteArray, 1, b, 0, b.length);
      return b;
  }
}
