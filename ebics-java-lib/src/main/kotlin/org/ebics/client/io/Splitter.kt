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
package org.ebics.client.io

import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.utils.CryptoUtils
import org.ebics.client.utils.Utils
import javax.crypto.spec.SecretKeySpec

/**
 * A mean to split a given input file to
 * 1MB portions. this i useful to handle
 * big file uploading.
 *
 * @author Hachani
 * @author Jan Toegel
 *
 * Constructs a new `FileSplitter` with a given file.
 * @param input the input byte array
 */
class Splitter(
    private val input: ByteArray,
    isCompressionEnabled: Boolean,
    keySpec: SecretKeySpec
) {
    /**
     * Reads the input stream and splits it to segments of 1MB size.
     *
     *
     * EBICS Specification 2.4.2 - 7 Segmentation of the order data:
     *
     *
     * The following procedure is to be followed with segmentation:
     *
     *  1.  The order data is ZIP compressed
     *  1.  The compressed order data is encrypted in accordance with Chapter 6.2
     *  1.  The compressed, encrypted order data is base64-coded.
     *  1.  The result is to be verified with regard to the data volume:
     *
     *  1.  If the resulting data volume is below the threshold of 1 MB = 1,048,576 bytes,
     * the order data can be sent complete as a data segment within one transmission step
     *  1.  If the resulting data volume exceeds 1,048,576 bytes the data is to be
     * separated sequentially and in a base64-conformant manner into segments
     * that each have a maximum of 1,048,576 bytes.
     *
     *
     * @param isCompressionEnabled enable compression?
     * @param keySpec the secret key spec
     * @throws EbicsException
     */
    /**
     * Returns the hole content.
     * @return the input content.
     */
    val content: ByteArray
    private val segmentSize: Int

    /**
     * Returns the total segment number.
     * @return the total segment number.
     */
    val segmentNumber: Int

    /**
     * Maximum size of the segment (1MB)
     */
    val maxSegmentSize: Int = 1024 * 1024

    /**
     * Slits the input into 1MB portions.
     *
     *
     *  EBICS Specification 2.4.2 - 7 Segmentation of the order data:
     *
     *
     * In Version H003 of the EBICS standard, order data that requires more than 1 MB of storage
     * space in compressed, encrypted and base64-coded form MUST be segmented before
     * transmission, irrespective of the transfer direction (upload/download).
     *
     */
    init {
        try {
            val compressedInput = if (isCompressionEnabled) Utils.zip(input) else input
            content = CryptoUtils.encrypt(compressedInput, keySpec)
            val lastSegmentNotFull = content.size % maxSegmentSize != 0
            segmentNumber = content.size / maxSegmentSize + if (lastSegmentNotFull) 1 else 0
            segmentSize = content.size / segmentNumber
        } catch (e: Exception) {
            throw EbicsException(e.message)
        }
    }

    /**
     * Returns the content of a data segment according to
     * a given segment number.
     *
     * @param segmentNumber the segment number
     * @return
     */
    fun getContent(segmentNumber: Int): ContentFactory {
        val segment: ByteArray
        val offset: Int = segmentSize * (segmentNumber - 1)
        segment = if (content.size < segmentSize + offset) {
            ByteArray(content.size - offset)
        } else {
            ByteArray(segmentSize)
        }
        System.arraycopy(content, offset, segment, 0, segment.size)
        return ByteArrayContentFactory(segment)
    }
}