/*
 * Copyright (c) 1990-2012 kopiRight Managed Solutions GmbH
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

package org.kopi.ebics.certificate;

import java.security.cert.X509Certificate;

/**
 * A simple key store cache.
 * Allows to save a loaded key store.
 * 
 * @author hachani
 *
 */
public class KeyStoreCache implements Comparable<KeyStoreCache> {

  /**
   * Gets the certificate alias
   * @return the certificate alias
   */
  public String getAlias() {
    return alias;
  }
  
  /**
   * Sets the certificate alias
   * @param alias the certificate alias
   */
  public void setAlias(String alias) {
    if (alias != null) {
      this.alias = alias;
    } else {
      this.alias = "";
    }
  }
  
  /**
   * Gets the certificate.
   * @return the certificate.
   */
  public X509Certificate getCertificate() {
    return certificate;
  }
  
  /**
   * Sets the certificate
   * @param certificate the certificate
   */
  public void setCertificate(X509Certificate certificate) {
    this.certificate = certificate;
  }
  
  /**
   * Tells if its an entry key
   * @return True if it is an entry key
   */
  public boolean isKeyEntry() {
    return isKeyEntry;
  }
  
  /**
   * Sets the entry key flag;
   * @param isKeyEntry the entry key flag
   */
  public void setKeyEntry(boolean isKeyEntry) {
    this.isKeyEntry = isKeyEntry;
  }
  
  @Override
  public int compareTo(KeyStoreCache cache) {
    return alias.toUpperCase().compareTo(cache.getAlias().toUpperCase());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------
  
  private String		alias;
  private X509Certificate	certificate;
  private boolean		isKeyEntry;
}
