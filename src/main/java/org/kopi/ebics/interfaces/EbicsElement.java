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

package org.kopi.ebics.interfaces;

import java.io.PrintStream;
import java.io.Serializable;

import org.kopi.ebics.exception.EbicsException;


public interface EbicsElement extends Serializable {

  /**
   * Returns the name of this <code>EbicsElement</code>
   * @return the name of the element
   */
  String getName();

  /**
   * Builds the <code>EbicsElement</code> XML fragment
   * @throws EbicsException
   */
  void build() throws EbicsException;

  /**
   * Prints the <code>EbicsElement</code> into
   * the given stream.
   * @param stream the print stream
   */
  void print(PrintStream stream);

}
