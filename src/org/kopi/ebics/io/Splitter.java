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

package org.kopi.ebics.io;

import javax.crypto.spec.SecretKeySpec;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.utils.Utils;


/**
 * A mean to split a given input file to
 * 1MB portions. this i useful to handle
 * big file uploading.
 * 
 * @author Hachani
 *
 */
public class Splitter {

  /**
   * Constructs a new <code>FileSplitter</code> with a given file.
   * @param input the input byte array
   */
  public Splitter(byte[] input) {
    this.input = input;
  }

  /**
   * Reads the input stream and splits it to segments of 1MB size.
   * @param isCompressionEnabled enable compression?
   * @param keySpec the secret key spec
   * @throws EbicsException
   */
  public final void readInput(boolean isCompressionEnabled, SecretKeySpec keySpec) 
    throws EbicsException
  {
    try {
      if (isCompressionEnabled) {
	content = Utils.zip(input);
      }
      content = Utils.encrypt(content, keySpec);
      segmentation();
    } catch (Exception e) {
      throw new EbicsException(e.getMessage());
    }
  }
  
  /**
   * Slits the input into 1MB portions
   */
  private void segmentation() {
    int			size;
    
    size = ((content.length + 2) / 3 << 2);
    numSegments = size / 1048576;
    
    if (size % 1048576 != 0) {
      numSegments ++;
    }
    
    segmentSize = size / numSegments;
    
    while (segmentSize % 4 != 0) {
      segmentSize += 1;
    }
    
    segmentSize = ((segmentSize + 3) / 4 * 3);
  }
  
  /**
   * Returns the content of a sata segment
   * @param segmentNumber the segment number
   * @return
   */
  public ContentFactory getContent(int segmentNumber) {
    byte[]		segment;
    int			offset;

    offset = segmentSize * (segmentNumber - 1);
    if (content.length < segmentSize + offset) {
      segment = new byte[content.length - offset];
    } else {
      segment = new byte[segmentSize];
    }
    
    System.arraycopy(content, offset, segment, 0, segment.length);
    return new ByteArrayContentFactory(segment);
  }
  
  /**
   * Returns the hole content.
   * @return the input content.
   */
  public byte[] getContent() {
    return content;
  }
  
  /**
   * Returns the total segment number.
   * @return the total segment number.
   */
  public int getSegmentNumber() {
    return numSegments;
  }
  
  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------
  
  private byte[]				input;
  private byte[]				content;
  private int					segmentSize;
  private int					numSegments;
}
