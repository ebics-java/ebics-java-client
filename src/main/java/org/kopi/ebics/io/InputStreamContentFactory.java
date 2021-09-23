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

package org.kopi.ebics.io;

import org.kopi.ebics.interfaces.ContentFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * Input stream content factory that delivers the its content
 * as an <code>InputStream</code>
 * This object is serializable in a way to recover interrupted
 * file transfers.
 *
 * @author hachani
 *
 */
public class InputStreamContentFactory implements ContentFactory {

  /**
   * Creates a new <code>ContentFactory</code> from an input stream
   * @param input the given input stream.
   */
  public InputStreamContentFactory(InputStream input) {
    this.input = input;
  }

  @Override
  public InputStream getContent() throws IOException {
    return input;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private InputStream			input;
  private static final long 		serialVersionUID = 2357104115203917168L;
}
