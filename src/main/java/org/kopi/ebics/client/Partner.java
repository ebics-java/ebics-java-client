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
    return Integer.valueOf(orderId);
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

  /**
   * In EBICS XSD schema - ebics_types.xsd, The order ID pattern
   * is defined as following: <b>pattern value="[A-Z][A-Z0-9]{3}"</b>.
   * <p>This means that the order ID should start with a letter
   * followed by three alphanumeric characters.
   *
   *<p> The <code>nextOrderId()</code> aims to generate orders from
   *<b>A000</b> to <b>ZZZZ</b>. The sequence cycle is performed infinitely.
   *
   *<p> The order index {@link Partner#orderId} is saved whenever it
   * changes.
   */
  @Override
  public String nextOrderId() {
    char[]      chars = new char[4];

    orderId += 1;
    if (orderId > 36*36*36*36 - 1) {
      // ensure that orderId starts with a letter
      orderId = 10*36*36*36;
    }
    chars[3] = ALPHA_NUM_CHARS.charAt(orderId % 36);
    chars[2] = ALPHA_NUM_CHARS.charAt((orderId / 36) % 36);
    chars[1] = ALPHA_NUM_CHARS.charAt((orderId / 36 / 36) % 36);
    chars[0] = ALPHA_NUM_CHARS.charAt(orderId / 36 / 36 / 36);
    needSave = true;

    return new String(chars);
  }

  @Override
  public String getSaveName() {
    return "partner-" + partnerId + ".cer";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final EbicsBank			bank;
  private int				orderId = 10*36*36*36;
  private final String			partnerId;
  private transient boolean		needSave;

  private static final String		ALPHA_NUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}
