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

package org.kopi.ebics.session;

import java.io.Serializable;

/**
 * Optional information about the client product.
 *
 *
 */
public class Product implements Serializable {

  /**
   * Creates a new product information element.
   * @param name this is the name of the product. It is a mandatory field.
   * @param language this is the language. If you use null, the language of the default locale is used.
   * @param instituteID the institute, this is an optional value, you can leave this parameter empty.
   */
  public Product(String name,
                 String language,
                 String instituteID)
  {
    this.name = name;
    this.language = language;
    this.instituteID = instituteID;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the instituteID
   */
  public String getInstituteID() {
    return instituteID;
  }

  /**
   * @param instituteID the instituteID to set
   */
  public void setInstituteID(String instituteID) {
    this.instituteID = instituteID;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private transient String	name;
  private String		language;
  private String		instituteID;

  private static final long 	serialVersionUID = 6400195827756653241L;

}
