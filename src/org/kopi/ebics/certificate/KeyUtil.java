/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL
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

package org.kopi.ebics.certificate;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import org.kopi.ebics.exception.EbicsException;

/**
 * Some key utilities
 * 
 * @author hachani
 *
 */
public class KeyUtil {

  /**
   * Generates a <code>KeyPair</code> in RSA format.
   * 
   * @param keyLen - key size
   * @return KeyPair the key pair
   * @throws NoSuchAlgorithmException 
   */
  public static KeyPair makeKeyPair(int keyLen) throws NoSuchAlgorithmException{
    KeyPairGenerator 		keyGen;
    
    keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(keyLen, new SecureRandom());

    KeyPair keypair = keyGen.generateKeyPair();

    return keypair;

  }

  /**
   * Generates a random password
   * 
   * @return the password
   */
  public static String generatePassword() {
    SecureRandom 		random;
    
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
      String pwd = Base64.encodeBase64String(random.generateSeed(5));

      return pwd.substring(0, pwd.length() - 2);
    } catch (NoSuchAlgorithmException e) {
      return "changeit";
    }
  }
  
  /**
   * Returns the digest value of a given public key
   * @param publicKey the public key
   * @return the digest value
   * @throws Exception
   */
  public static byte[] getKeyDigest(RSAPublicKey publicKey) throws EbicsException {
    String			modulus;
    String			exponent;
    String			hash;
    byte[]			digest;
    
    exponent = Hex.encodeHexString(publicKey.getPublicExponent().toByteArray());
    modulus =  Hex.encodeHexString(publicKey.getModulus().toByteArray());
    hash = clean(exponent) + " " + clean(modulus);
    
    try {
      digest = MessageDigest.getInstance("SHA-256", "BC").digest(hash.getBytes("US-ASCII"));
    } catch (GeneralSecurityException e) {
      throw new EbicsException(e.getMessage());
    } catch (UnsupportedEncodingException e) {
      throw new EbicsException(e.getMessage());
    }
    
    return new String(Hex.encodeHex(digest, false)).getBytes();
  }

  /**
   * Removes left zeros from an hexadecimal string
   * @param hex the hexadecimal string
   * @return the shortened form of an hexadecimal string
   */
  private static String clean(String hex) {
    int			i;
    
    i = 0;
    while (hex.charAt(i++) == '0');
    return hex.substring(i - 1); 
  }
}
