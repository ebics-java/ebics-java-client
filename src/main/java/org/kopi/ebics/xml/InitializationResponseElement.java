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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.exception.ReturnCode;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.schema.h005.EbicsResponseDocument;
import org.kopi.ebics.schema.h005.EbicsResponseDocument.EbicsResponse;

/**
 * The <code>InitializationResponseElement</code> is the common
 * element for transfer initialization responses.
 *
 *
 */
public class InitializationResponseElement extends DefaultResponseElement {

  /**
   * Constructs a new <code>InitializationResponseElement</code> element.
   * @param factory the content factory
   * @param orderType the order type
   * @param name the element name
   */
  public InitializationResponseElement(ContentFactory factory,
                                       EbicsOrderType orderType,
                                       String name)
  {
    super(factory, name);
    this.orderType = orderType;
  }

    @Override
    public void build() throws EbicsException {
        var doc = parse(factory, EbicsResponseDocument.Factory);
        response = doc.getEbicsResponse();
        String code = response.getHeader().getMutable().getReturnCode();
        String text = response.getHeader().getMutable().getReportText();
        returnCode = ReturnCode.toReturnCode(code, text);
        checkReturnCode(returnCode);
        processBodyReturnCode();
        transactionId = response.getHeader().getStatic().getTransactionID();
    }

  protected void processBodyReturnCode() throws EbicsException {
      String bodyRetCode = response.getBody().getReturnCode().getStringValue();
      ReturnCode returnCode = ReturnCode.toReturnCode(bodyRetCode, "");
      checkReturnCode(returnCode);
  }


/**
   * Returns the transaction ID.
   * @return the transaction ID.
   */
  public byte[] getTransactionId() {
    return transactionId;
  }

  /**
   * Returns the order type.
   * @return the order type.
   */
  public String getOrderType() {
    return orderType.getCode();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  protected EbicsResponse			response;
  private final EbicsOrderType orderType;
  private byte[]				transactionId;
  private static final long 			serialVersionUID = 7684048385353175772L;
}
