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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.Savable;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.io.IOUtils;


/**
 * A simple implementation of the <code>SerializationManager</code>.
 * The serialization process aims to save object on the user disk
 * using a separated file for each object to serialize.
 *
 * @author hachani
 *
 */
public class DefaultSerializationManager implements SerializationManager {

  /**
   * Constructs a new <code>SerializationManager</code>
   * @param serializationDir the serialization directory
   */
  public DefaultSerializationManager(File serializationDir) {
    this.serializationDir = serializationDir;
  }

  /**
   * Constructs a new <code>SerializationManager</code>
   */
  public DefaultSerializationManager() {
    this(null);
  }

  @Override
  public void serialize(Savable object) throws EbicsException {
    try {
      ObjectOutputStream	out;

      out = new ObjectOutputStream(new FileOutputStream(IOUtils.createFile(serializationDir, object.getSaveName())));
      object.save(out);
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public ObjectInputStream deserialize(String name) throws EbicsException {
    try {
      ObjectInputStream		input;

      input = new ObjectInputStream(new FileInputStream(IOUtils.createFile(serializationDir, name + ".cer")));
      return input;
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public void setSerializationDirectory(String serializationDir) {
    this.serializationDir = new File(serializationDir);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private File 					serializationDir;
}
