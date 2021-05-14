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

package org.ebics.client.xml.h004;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.interfaces.EbicsRootElement;
import org.ebics.client.user.EbicsSession;
import org.ebics.client.order.EbicsAdminOrderType;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.namespace.QName;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public abstract class DefaultEbicsRootElement implements EbicsRootElement {

  /**
   * Constructs a new default <code>EbicsRootElement</code>
   * @param session the current ebics session
   */
  public DefaultEbicsRootElement(EbicsSession session) {
    this.session = session;
    suggestedPrefixes = new HashMap<String, String>();
  }

  /**
   *  Constructs a new default <code>EbicsRootElement</code>
   */
  public DefaultEbicsRootElement() {
    this(null);
  }

  /**
   * Saves the Suggested Prefixes when the XML is printed
   * @param uri the namespace URI
   * @param prefix the namespace URI prefix
   */
  protected static void setSaveSuggestedPrefixes(String uri, String prefix) {
    suggestedPrefixes.put(uri, prefix);
  }

  /**
   * Prints a pretty XML document using jdom framework.
   * @return the pretty XML document.
   * @throws EbicsException pretty print fails
   */
  public byte[] prettyPrint() throws EbicsException {
    Document                  	document;
    XMLOutputter              	xmlOutputter;
    SAXBuilder                	sxb;
    ByteArrayOutputStream	output;

    sxb = new SAXBuilder();
    output = new ByteArrayOutputStream();
    xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

    try {
      document = sxb.build(new InputStreamReader(new ByteArrayInputStream(toByteArray()), "UTF-8"));
      xmlOutputter.output(document, output);
    } catch (JDOMException e) {
      throw new EbicsException(e.getMessage());
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }

    return output.toByteArray();
  }

  /**
   * Inserts a schema location to the current ebics root element.
   * @param namespaceURI the name space URI
   * @param localPart the local part
   * @param prefix the prefix
   * @param value the value
   */
  public void insertSchemaLocation(String namespaceURI,
                                   String localPart,
                                   String prefix,
                                   String value)
  {
    XmlCursor 			cursor;

    cursor = document.newCursor();
    while (cursor.hasNextToken()) {
      if (cursor.isStart()) {
	cursor.toNextToken();
	cursor.insertAttributeWithValue(new QName(namespaceURI, localPart, prefix), value);
	break;
      } else {
	cursor.toNextToken();
      }
    }
  }

  /**
   * Generates a random file name with a prefix.
   * @param type the order type.
   * @return the generated file name.
   */
  public static String generateName(EbicsAdminOrderType type) {
    return type.toString() + new BigInteger(130, new SecureRandom()).toString(32);
  }
  
  /**
   * Generates a random file name with a prefix.
   * @param prefix the prefix to use.
   * @return the generated file name.
   */
  public static String generateName(String prefix) {
    return prefix + new BigInteger(130, new SecureRandom()).toString(32);
  }

  @Override
  public String toString() {
    return new String(toByteArray());
  }

  @Override
  public byte[] toByteArray() {
    XmlOptions		options;

    options = new XmlOptions();
    options.setSavePrettyPrint();
    options.setSaveSuggestedPrefixes(suggestedPrefixes);
    return document.xmlText(options).getBytes();
  }

  @Override
  public void addNamespaceDecl(String prefix, String uri) {
    XmlCursor 			cursor;

    cursor = document.newCursor();
    while (cursor.hasNextToken()) {
      if (cursor.isStart()) {
	cursor.toNextToken();
	cursor.insertNamespace(prefix, uri);
	break;
      } else {
	cursor.toNextToken();
      }
    }
  }

  @Override
  public void validate() throws EbicsException {
    ArrayList<XmlError>		validationMessages;
    boolean     		isValid;

    validationMessages = new ArrayList<XmlError>();
    isValid = document.validate(new XmlOptions().setErrorListener(validationMessages));

    if (!isValid) {
      String			message;
      Iterator<XmlError>    	iter;

      iter = validationMessages.iterator();
      message = "";
      while (iter.hasNext()) {
	if (!message.equals("")) {
	  message += ";";
	}
	message += iter.next().getMessage();
      }

      throw new EbicsException(message);
    }
  }

  @Override
  public void save(OutputStream out) throws EbicsException {
    try {
      byte[]		element;

      element = prettyPrint();
      out.write(element);
      out.flush();
      out.close();
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public void print(PrintStream stream) {
    stream.println(document.toString());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  protected XmlObject			document;
  protected EbicsSession 		session;
  private static Map<String, String> 	suggestedPrefixes;
  private static final long 		serialVersionUID = -3928957097145095177L;
}
