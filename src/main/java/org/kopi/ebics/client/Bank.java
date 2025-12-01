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
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.Savable;


/**
 * Simple implementation of an EBICS bank.
 * This object is serializable to facilitate persisting of the values.
 * Save the object whenever it needs to be saved.
 *
 *
 */
public class Bank implements EbicsBank, Savable {

  /**
   * Constructs a new EBICS bank with the data you should have obtained from the bank.
   * @param url the bank URL
   * @param name the bank name
   * @param hostId the bank host ID
   * @param useCertificate does the bank use certificates for exchange ?
   */
  public Bank(URL url, String name, String hostId, boolean useCertificate) {
    this.url = url;
    this.name = name;
    this.hostId = hostId;
    this.useCertificate = useCertificate;
    needSave = true;
  }

  @Override
  public void save(ObjectOutputStream oos) throws IOException {
    oos.writeObject(this);
    oos.flush();
    oos.close();
    needSave = false;
  }

  /**
   * Did any persistent attribute change since last load/save operation.
   * @return True if the object needs to be saved.
   */
  public boolean needsSave() {
    return needSave;
  }

  /**
   * readObject is called to restore the state of the URL from the
   * stream.
   * @param ois the object input stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();  // read the fields
  }

  /**
   * WriteObject is called to save the state of the EbicsBank to an
   * ObjectOutputStream.
   *
   * @serialData the default write object.
   * throw an IOException if it does not.
   */
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject(); // write the fields
  }

  @Override
  public URL getURL() {
    return url;
  }

  @Override
  public byte[] getE002Digest() {
    return e002Digest;
  }

  @Override
  public byte[] getX002Digest() {
    return x002Digest;
  }

  @Override
  public RSAPublicKey getE002Key() {
    return e002Key;
  }

  @Override
  public RSAPublicKey getX002Key() {
    return x002Key;
  }

  @Override
  public String getHostId() {
    return hostId;
  }

  @Override
  public String getName() {
    return name;
  }

    @Override
    public boolean useCertificate() {
      return useCertificate;
    }

    @Override
    public void setUseCertificate(boolean useCertificate) {
        this.useCertificate = useCertificate;
        needSave = true;
    }

  @Override
  public void setBankKeys(RSAPublicKey e002Key, RSAPublicKey x002Key) {
    this.e002Key = e002Key;
    this.x002Key = x002Key;
    needSave = true;
  }

  @Override
  public void setDigests(byte[] e002Digest, byte[] x002Digest) {
    this.e002Digest = e002Digest;
    this.x002Digest = x002Digest;
    needSave = true;
  }

  @Override
  public String getSaveName() {
    return hostId + ".cer";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  /**
   * The bank URL
   * @serial
   */
  private final URL			url;

  /**
   * The bank host id
   * @serial
   */
  private final String		hostId;
  
  /**
   * Does the bank use certificates for signing/crypting ?
   * @serial
   */
  private boolean               useCertificate;

  /**
   * The bank name
   * @serial
   */
  private final String		name;

  /**
   * The bank encryption digest
   * @serial
   */
  private byte[]		e002Digest;

  /**
   * The bank authentication digest
   * @serial
   */
  private byte[]		x002Digest;

  /**
   * The ban encryption key
   * @serial
   */
  private RSAPublicKey		e002Key;

  /**
   * The ban encryption key
   * @serial
   */
  private RSAPublicKey		x002Key;

  private transient boolean	needSave;

  private static final long 	serialVersionUID = 2123071449956793284L;

}
