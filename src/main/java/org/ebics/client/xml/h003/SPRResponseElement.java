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

package org.ebics.client.xml.h003;

import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.ReturnCode;
import org.ebics.client.interfaces.ContentFactory;
import org.ebics.schema.h003.EbicsResponseDocument;
import org.ebics.schema.h003.EbicsResponseDocument.EbicsResponse;

/**
 * The <code>SPRResponseElement</code> is the response element
 * for an ebics subscriber revoking.
 *
 * @author Hachani
 *
 */
public class SPRResponseElement extends DefaultResponseElement {

  /**
   * Constructs a new SPR response element.
   * @param factory the content factory
   */
  public SPRResponseElement(ContentFactory factory) {
    super(factory, "SPRResponse.xml");
  }

  @Override
  public void build() throws EbicsException {
    String			code;
    String			text;

    parse(factory);
    response = ((EbicsResponseDocument)document).getEbicsResponse();
    code = response.getHeader().getMutable().getReturnCode();
    text = response.getHeader().getMutable().getReportText();
    returnCode = ReturnCode.toReturnCode(code, text);
    report();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsResponse				response;
  private static final long 			serialVersionUID = 8632578696636481642L;
}
