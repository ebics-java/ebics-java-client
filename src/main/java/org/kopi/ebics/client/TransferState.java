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

import java.io.Serializable;

/**
 * Persistable state of a file transfer.
 * It may be used to continue a transfer via
 * <code>FileTransfer.nextChunk(TransferState)</code>
 * in this or a future session.
 *
 *
 */
public class TransferState implements Serializable {

  public TransferState(int numSegments, byte[] transactionId) {
    this.numSegments = numSegments;
    this.transactionId = transactionId;
  }

  /**
   * Returns the next segment number to be transferred.
   * @return the next segment number to be transferred.
   */
  public int next() {
    segmentNumber++;

    if (segmentNumber == numSegments) {
      lastSegment = true;
    }

    return segmentNumber;
  }

  public boolean hasNext() {
    return segmentNumber < numSegments;
  }

  /**
   * Sets the segment number
   * @param segmentNumber the segment number
   */
  public void setSegmentNumber(int segmentNumber) {
    this.segmentNumber = segmentNumber;
  }

  /**
   * Is the current segment is the last one?
   * @return True if it is the last segment
   */
  public boolean isLastSegment() {
    return lastSegment;
  }

  /**
   * @return the transactionID
   */
  public byte[] getTransactionId() {
    return transactionId;
  }

  /**
   * @param transactionId the transactionID to set
   */
  public void setTransactionId(byte[] transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * @return the numSegments
   */
  public int getNumSegments() {
    return numSegments;
  }

  /**
   * @param numSegments the numSegments to set
   */
  public void setNumSegments(int numSegments) {
    this.numSegments = numSegments;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[]			transactionId;
  private int 				segmentNumber;
  private int				numSegments;
  private transient boolean		lastSegment;

  private static final long 		serialVersionUID = -3189235892639115408L;
}
