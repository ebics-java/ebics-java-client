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
package org.ebics.client.api

import java.io.Serializable

/**
 * State of a file transfer.
 * It may be used to continue a transfer via
 * `FileTransfer.nextChunk(TransferState)`
 * in this or a future session.
 *
 * @author Hachani
 */
class TransferState(
    /**
     * @param numSegments the numSegments to set
     */
    private var numSegments: Int,
    /**
     * @param transactionId the transactionID to set
     */
    var transactionId: ByteArray
) {
    /**
     * Returns the next segment number to be transferred.
     * @return the next segment number to be transferred.
     */
    operator fun next(): Int {
        segmentNumber++
        if (segmentNumber == numSegments) {
            isLastSegment = true
        }
        return segmentNumber
    }

    operator fun hasNext(): Boolean {
        return segmentNumber < numSegments
    }

    /**
     * Sets the segment number
     * @param segmentNumber the segment number
     */
    fun setSegmentNumber(segmentNumber: Int) {
        this.segmentNumber = segmentNumber
    }

    /**
     * @return the transactionID
     */
    private var segmentNumber = 0

    /**
     * Is the current segment is the last one?
     * @return True if it is the last segment
     */
    @Transient
    var isLastSegment = false
        private set
}