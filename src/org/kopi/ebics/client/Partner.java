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

package org.kopi.ebics.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.Savable;


/**
 * Simple implementation of an EBICS customer.
 * This object is not serializable, but it should be persisted every time it needs to be saved.
 * Persistence is achieved via <code>save(ObjectOutputStream)</code> and the matching constructor.
 *
 * @author Hachani
 *
 */
public class Partner implements EbicsPartner, Savable {

  /**
   * Reconstructs a persisted EBICS customer.
   * @param bank the bank
   * @param ois the stream object
   * @throws IOException
   */
  public Partner(EbicsBank bank, ObjectInputStream ois) throws IOException {
    this.bank = bank;
    this.partnerId = ois.readUTF();
    this.orderId = ois.readInt();
    ois.close();
  }

  /**
   * First time constructor.
   * @param bank the bank
   * @param partnerId the partner ID
   */
  public Partner(EbicsBank bank, String partnerId) {
    this.bank = bank;
    this.partnerId = partnerId;
    needSave = true;
  }

  /**
   * Returns the next order available ID
   * @return the next order ID
   */
  public Integer getNextOrderId() {
    return new Integer(orderId);
  }

  /**
   * Sets the order ID
   * @param orderId the order ID
   */
  public void setOrderId(Integer orderId) {
    this.orderId = orderId.intValue();
    needSave = true;
  }

  @Override
  public void save(ObjectOutputStream oos) throws IOException {
    oos.writeUTF(partnerId);
    oos.writeInt(orderId);
    oos.flush();
    oos.close();
    needSave = false;
  }

  /**
   * Did any persistable attribute change since last load/save operation.
   * @return True if the object needs to be saved.
   */
  public boolean needsSave() {
    return needSave;
  }

  @Override
  public EbicsBank getBank() {
    return bank;
  }

  @Override
  public String getPartnerId() {
    return partnerId;
  }

  @Override
  public String nextOrderId() {
    int 		index;
    String		order;

    needSave = true;
    orderId ++;
    if (orderId >= 46656) {
      if (orderId >= 1679616) {
	orderId = 0;
      } else if (orderId < 513216) {
	orderId = 513216;
      }
    }

    index = orderId - 1;
    if (index < 0) {
      index = 1679615;
    }
    order = Integer.toString(index, 36).toUpperCase();

    if (order.length() < 4) {
      while (order.length() < 3) {
	order = "0" + order;
      }

      return "A" + order;
    }

    return order;
  }

  @Override
  public String getSaveName() {
    return partnerId + ".cer";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsBank			bank;
  private int				orderId;
  private String			partnerId;
  private transient boolean		needSave;
}
