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

import org.ebics.client.api.trace.TraceManager
import java.util.*

/**
 * EBICS client application configuration.
 *
 * @author hachani
 */
interface Configuration {

    /**
     * Returns the SSL trusted store, used for establishing connections if needed (usually for no public EBICS servers only).
     * @return the SSL trusted store.
     */
    val sslTrustedStoreFile: String?

    val httpProxyHost: String?
    val httpProxyPort: Int?
    val httpProxyUser: String?
    val httpProxyPassword: String?

    /**
     * Returns the Ebics client trace manager.
     * @return the Ebics client trace manager.
     */
    val traceManager: TraceManager

    /**
     * Returns the letter manager.
     * @return the letter manager.
     */
    val letterManager: LetterManager

    /**
     * Returns the application locale.
     * @return the application locale.
     */
    val locale: Locale

    /**
     * Returns the client application signature version
     * @return the signature version
     */
    val signatureVersion: String

    /**
     * Returns the client application authentication version
     * @return the authentication version
     */
    val authenticationVersion: String

    /**
     * Returns the client application encryption version
     * @return the encryption version
     */
    val encryptionVersion: String

    /**
     * Tells if the client application should keep XML transfer
     * files in the transfer log directory
     * @return True if the client application should not delete
     * the XML transfer files
     */
    val isTraceEnabled: Boolean

    /**
     * Returns if the files to be transferred should be
     * compressed or sent without compression. This can
     * affect the time of data upload especially for big
     * files
     *
     * @return true if the file compression is enabled
     */
    val isCompressionEnabled: Boolean
}