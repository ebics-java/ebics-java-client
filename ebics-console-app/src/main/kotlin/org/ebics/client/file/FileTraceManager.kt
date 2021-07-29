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
package org.ebics.client.file

import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.EbicsRootElement
import org.ebics.client.api.TraceManager
import org.ebics.client.io.IOUtils
import org.ebics.client.api.EbicsSession
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * The `DefaultTraceManager` aims to trace an ebics
 * transferable element in an instance of `java.io.File`
 * then saved to a trace directory.
 * The manager can delete all traces file if the configuration does
 * not offer tracing support.
 * see [isTraceEnabled()][Configuration.isTraceEnabled]
 *
 * @author hachani
 */
class FileTraceManager(
    /**
     * Constructs a new `TraceManger` to manage transfer traces.
     * @param isTraceEnabled is trace enabled?
     */
    isTraceEnabled: Boolean = true
) : TraceManager {

    @Throws(EbicsException::class)
    override fun trace(element: EbicsRootElement, session: EbicsSession) {
        try {
            val out: FileOutputStream
            val file: File
            val traceDir = File((session.configuration as FileConfiguration).getTransferTraceDirectory(session.user))
            file = IOUtils.createFile(traceDir, element.name)
            out = FileOutputStream(file)
            element.save(out)
            cache.add(file)
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    override fun setTraceEnabled(enabled: Boolean) {
        cache.setTraceEnabled(enabled)
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    private val cache: FileCache = FileCache(isTraceEnabled)
}