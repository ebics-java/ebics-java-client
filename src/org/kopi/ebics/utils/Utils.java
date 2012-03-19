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

package org.kopi.ebics.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.utils.IgnoreAllErrorHandler;
import org.apache.xpath.XPathAPI;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.messages.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;


/**
 * Some utilities for EBICS request creation and reception
 *
 * @author hachani
 *
 */
public class Utils {

  /**
   * Compresses an input of byte array
   * @param toZip the input to be compressed
   * @return the compressed input data
   * @throws IOException compression failed
   */
  public static byte[] zip(byte[] toZip) throws EbicsException {

    if (toZip == null) {
      throw new EbicsException("The input to be zipped cannot be null");
    }

    Deflater				compressor;
    ByteArrayOutputStream		output;
    byte[]				buffer;

    output = new ByteArrayOutputStream(toZip.length);
    buffer = new byte[1024];
    compressor = new Deflater(Deflater.BEST_COMPRESSION);
    compressor.setInput(toZip);
    compressor.finish();

    while (!compressor.finished()) {
      int count = compressor.deflate(buffer);
      output.write(buffer, 0, count);
    }

    try {
      output.close();
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
    compressor.end();

    return output.toByteArray();
  }

  /**
   * Generates a random nonce.
   * @return a random nonce.
   * @throws EbicsException nonce generation fails.
   */
  public static byte[] generateNonce() throws EbicsException {
    SecureRandom 		secureRandom;

    try {
      secureRandom = SecureRandom.getInstance("SHA1PRNG");
      return secureRandom.generateSeed(16);
    } catch (NoSuchAlgorithmException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Uncompresses a given byte array input.
   * @param zip the zipped input.
   * @return the uncompressed data.
   */
  public static byte[] unzip(byte[] zip) throws EbicsException {
    Inflater 			decompressor;
    ByteArrayOutputStream 	output;
    byte[] 			buf;

    decompressor = new Inflater();
    output = new ByteArrayOutputStream(zip.length);
    decompressor.setInput(zip);
    buf = new byte[1024];

    while (!decompressor.finished()) {
      int 		count;

      try {
	count = decompressor.inflate(buf);
      } catch (DataFormatException e) {
	throw new EbicsException(e.getMessage());
      }
      output.write(buf, 0, count);
    }

    try {
      output.close();
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }

    decompressor.end();

    return output.toByteArray();
  }

  /**
   * Canonizes an input with inclusive c14n without comments algorithm.
   * @param input the byte array XML input.
   * @return the canonized form of the given XML
   * @throws EbicsException
   */
  public static byte[] canonize(byte[] input) throws EbicsException {
    DocumentBuilderFactory 		factory;
    DocumentBuilder			builder;
    Document				document;
    NodeIterator			iter;
    ByteArrayOutputStream		output;
    Node 				node;

    try {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new IgnoreAllErrorHandler());
      document = builder.parse(new ByteArrayInputStream(input));
      iter = XPathAPI.selectNodeIterator(document, "//*[@authenticate='true']");
      output = new ByteArrayOutputStream();
      while ((node = iter.nextNode()) != null) {
        Canonicalizer 		canonicalizer;

        canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        output.write(canonicalizer.canonicalizeSubtree(node));
      }

      return output.toByteArray();
    } catch (Exception e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Encrypts an input with a given key spec
   * @param input the input to encrypt
   * @param keySpec the key spec
   * @return the encrypted input
   * @throws EbicsException
   */
  public static byte[] encrypt(byte[] input, SecretKeySpec keySpec)
    throws EbicsException
  {
    try {
      IvParameterSpec		iv;
      Cipher 			cipher;

      iv = new IvParameterSpec(new byte[16]);
      cipher = Cipher.getInstance("AES/CBC/ISO10126Padding", "BC");
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
      return cipher.doFinal(input);
    } catch (Exception e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Parses a string date
   * @param date the given string date
   * @return the date value
   */
  public static Date parse(String date) throws EbicsException {
    try {
      return Constants.DEFAULT_DATE_FORMAT.parse(date);
    } catch (ParseException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Checks for the returned http code
   * @param httpCode the http code
   * @throws EbicsException
   */
  public static void checkHttpCode(int httpCode) throws EbicsException {
    if (httpCode != 200) {
      throw new EbicsException(Messages.getString("http.code.error",
	                                          Constants.APPLICATION_BUNDLE_NAME,
	                                          httpCode));
    }
  }
}
