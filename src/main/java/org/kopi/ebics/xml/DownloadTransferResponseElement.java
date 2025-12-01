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
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsOrderType;

/**
 * The <code>DTransferResponseElement</code> is the response element
 * for all ebics downloads transfers.
 *
 *
 */
public class DownloadTransferResponseElement extends TransferResponseElement {

  /**
   * Constructs a new <code>DTransferResponseElement</code> object.
   * @param factory the content factory
   * @param orderType the order type
   * @param name the element name.
   */
  public DownloadTransferResponseElement(ContentFactory factory,
                                  EbicsOrderType orderType,
                                  String name)
  {
    super(factory, name);
  }

  @Override
  public void build() throws EbicsException {
    super.build();

    orderData = response.getBody().getDataTransfer().getOrderData().getByteArrayValue();
  }

  /**
   * Returns the order data.
   * @return the order data.
   */
  public byte[] getOrderData() {
    return orderData;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[]			orderData;
  private static final long 		serialVersionUID = -3317833033395561745L;
}
