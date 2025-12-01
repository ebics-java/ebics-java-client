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

package org.kopi.ebics.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.kopi.ebics.interfaces.ContentFactory;


/**
 * File content factory that delivers the file content
 * as a <code>FileInputStream</code>. This object is
 * serializable in a way to recover interrupted file transfers.
 *
 *
 */
public class FileContentFactory implements ContentFactory {

  /**
   * Constructs a new <code>FileContentFactory</code> with
   * a given input file
   * @param input the input file
   */
  public FileContentFactory(File input) {
    this.input = input;
  }

  @Override
  public InputStream getContent() throws IOException {
    return new FileInputStream(input);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final File			input;
  private static final long 	serialVersionUID = -7041705645994170039L;
}
