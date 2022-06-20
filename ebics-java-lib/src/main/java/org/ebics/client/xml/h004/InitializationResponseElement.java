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

import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.h004.EbicsReturnCode;
import org.ebics.client.interfaces.ContentFactory;
import org.ebics.schema.h004.EbicsResponseDocument;
import org.ebics.schema.h004.EbicsResponseDocument.EbicsResponse;

/**
 * The <code>InitializationResponseElement</code> is the common
 * element for transfer initialization responses.
 *
 * @author Hachani
 *
 */
public class InitializationResponseElement extends DefaultResponseElement {

  /**
   * Constructs a new <code>InitializationResponseElement</code> element.
   * @param factory the content factory
   */
  public InitializationResponseElement(ContentFactory factory)
  {
    super(factory);
  }

  @Override
  public void build() throws EbicsException {
    parse(factory);
    response = ((EbicsResponseDocument)document).getEbicsResponse();
    String code = response.getHeader().getMutable().getReturnCode();
    String text = response.getHeader().getMutable().getReportText();
    returnCode = EbicsReturnCode.toReturnCode(code, text);
    checkReturnCode(returnCode);
    processBodyReturnCode();
    transactionId = response.getHeader().getStatic().getTransactionID();
    orderNumber = response.getHeader().getMutable().getOrderID();
  }

  protected void processBodyReturnCode() throws EbicsException {
      String bodyRetCode = response.getBody().getReturnCode().getStringValue();
      EbicsReturnCode returnCode = EbicsReturnCode.toReturnCode(bodyRetCode);
      checkReturnCode(returnCode);
  }


/**
   * Returns the transaction ID.
   * @return the transaction ID.
   */
  public byte[] getTransactionId() {
    return transactionId;
  }

  public String getOrderNumber() { return orderNumber; }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  protected EbicsResponse			response;
  private byte[]				transactionId;
  private String                orderNumber;
  private static final long 			serialVersionUID = 7684048385353175772L;
}
