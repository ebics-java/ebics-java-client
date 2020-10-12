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

package org.kopi.ebics.session;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.EbicsUser;


/**
 * Communication hub for EBICS.
 *
 * @author Hachani
 *
 */
public class EbicsSession {

  /**
   * Constructs a new ebics session
   * @param user the ebics user
   * @param configuration the ebics client configuration
   */
  public EbicsSession(EbicsUser user, Configuration configuration) {
    this.user = user;
    this.configuration = configuration;
    parameters = new HashMap<String, String>();
  }

  /**
   * Returns the banks encryption key.
   * The key will be fetched automatically form the bank if needed.
   * @return the banks encryption key.
   * @throws IOException Communication error during key retrieval.
   * @throws EbicsException Server error message generated during key retrieval.
   */
  public RSAPublicKey getBankE002Key() throws IOException, EbicsException {
    return user.getPartner().getBank().getE002Key();
  }

  /**
   * Returns the banks authentication key.
   * The key will be fetched automatically form the bank if needed.
   * @return the banks authentication key.
   * @throws IOException Communication error during key retrieval.
   * @throws EbicsException Server error message generated during key retrieval.
   */
  public RSAPublicKey getBankX002Key() throws IOException, EbicsException {
    return user.getPartner().getBank().getX002Key();
  }

  /**
   * Returns the bank id.
   * @return the bank id.
   * @throws EbicsException
   */
  public String getBankID() throws EbicsException {
    return user.getPartner().getBank().getHostId();
  }

  /**
   * Return the session user.
   * @return the session user.
   */
  public EbicsUser getUser() {
    return user;
  }

  /**
   * Returns the client application configuration.
   * @return the client application configuration.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Sets the optional product identification that will be sent to the bank during each request.
   * @param product Product description
   */
  public void setProduct(Product product) {
    this.product = product;
  }

  /**
   * @return the product
   */
  public Product getProduct() {
    return product;
  }

  /**
   * Adds a session parameter to use it in the transfer process.
   * @param key the parameter key
   * @param value the parameter value
   */
  public void addSessionParam(String key, String value) {
    parameters.put(key, value);
  }

  /**
   * Retrieves a session parameter using its key.
   * @param key the parameter key
   * @return the session parameter
   */
  public String getSessionParam(String key) {
    if (key == null) {
      return null;
    }

    return parameters.get(key);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsUser				user;
  private Configuration 			configuration;
  private Product				product;
  private Map<String, String>			parameters;
}
