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

package org.kopi.ebics.xml;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.schema.DocumentFactory;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.exception.ReturnCode;
import org.kopi.ebics.interfaces.ContentFactory;


/**
 * The <code>DefaultResponseElement</code> is the common element for
 * all ebics server responses.
 *
 *
 */
public abstract class DefaultResponseElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new ebics response element.
   * @param factory the content factory containing the response.
   * @param name the element name
   */
  protected DefaultResponseElement(ContentFactory factory, String name) {
    this.factory = factory;
    this.name = name;
  }

  /**
   * Parses the content of a <code>ContentFactory</code>
   *
   * @param factory the content factory
   * @return the parsed document
   * @throws EbicsException parse error
   */
  protected <T extends XmlObject> T parse(ContentFactory factory,
      DocumentFactory<T> documentFactory) throws EbicsException {
      try {
          var doc = documentFactory.parse(factory.getContent());
          this.document = doc;
          return doc;
      } catch (XmlException | IOException e) {
          throw new EbicsException(e.getMessage());
      }
  }
  /**
   * Reports the return code to the user.
   * @throws EbicsException request fails.
   */
  public void report() throws EbicsException {
    checkReturnCode(returnCode);
  }

  protected void checkReturnCode(ReturnCode returnCode) throws EbicsException {
    if (!returnCode.isOk()) {
      returnCode.throwException();
    }
}

  @Override
  public String getName() {
    return name + ".xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final String 			name;
  protected ContentFactory		factory;
  protected ReturnCode			returnCode;
  private static final long 		serialVersionUID = 4014595046719645090L;
}
