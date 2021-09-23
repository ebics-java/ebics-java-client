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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;

import java.io.*;


/**
 * Some IO utilities for EBICS files management.
 * EBICS server
 *
 * @author hachani
 *
 */
public class IOUtils {

  /**
   * Creates a directory from a root one
   * @param parent the parent directory
   * @param child the directory name
   * @return The created directory
   */
  public static File createDirectory(File parent, String child) {
    File			directory;

    directory = new File(parent, child);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates a directory from a root one
   * @param parent the parent directory
   * @param child the directory name
   * @return The created directory
   */
  public static File createDirectory(String parent, String child) {
    File			directory;

    directory = new File(parent, child);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates a directory from a directory name
   * @param name the absolute directory name
   * @return The created directory
   */
  public static File createDirectory(String name) {
    File			directory;

    directory = new File(name);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates many directories from a given full path.
   * Path should use default separator like '/' for UNIX
   * systems
   * @param fullName the full absolute path of the directories
   * @return The created directory
   */
  public static File createDirectories(String fullName) {
    File			directory;

    directory = new File(fullName);
    directory.mkdirs();

    return directory;
  }

  /**
   * Creates a new <code>java.io.File</code> from a given root.
   * @param parent the parent of the file.
   * @param name the file name.
   * @return the created file.
   */
  public static File createFile(File parent, String name) {
    File			file;

    file = new File(parent, name);

    return file;
  }


  /**
   * Returns the content of a file as byte array.
   * @param file the file
   * @return the byte array content of the file
   * @throws EbicsException
   */
  public static byte[] getFileContent(File file) throws EbicsException {
    try {
      try(FileInputStream input = new FileInputStream(file)) {
          return inputStreamToBytes(input);
      }
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  /**
   * Returns the content of a <code>ContentFactory</code> as a byte array
   * @param content
   * @return
   * @throws EbicsException
   */
    public static byte[] getFactoryContent(ContentFactory content) throws EbicsException {
        try (InputStream in = content.getContent()) {
            return inputStreamToBytes(in);
        } catch (IOException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    private static byte[] inputStreamToBytes(InputStream in) throws IOException {
        int len;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
        return out.toByteArray();
    }
}
