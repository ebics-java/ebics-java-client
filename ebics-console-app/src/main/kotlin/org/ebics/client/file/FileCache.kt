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

import java.io.File
import java.util.*

/**
 * A simple mean to cache created files.
 *
 * @author hachani
 */
class FileCache(private var isTraceEnabled: Boolean) {
    /**
     * Cache a new `java.io.File` in the cache buffer
     * @param file the file to cache
     * @return True if the file is cached
     */
    fun add(file: File): Boolean {
        if (cache.containsKey(file.name)) {
            return false
        }
        cache[file.name] = file
        return true
    }

    /**
     * Removes the given `java.io.file` from the cache.
     * @param filename the file to remove
     * @return True if the file is removed
     */
    fun remove(filename: String): Boolean {
        if (!cache.containsKey(filename)) {
            return false
        }
        cache.remove(filename)
        return true
    }

    /**
     * Clears the cache buffer
     */
    fun clear() {
        if (isTraceEnabled) {
            for (file in cache.values) {
                file.delete()
            }
        }
        cache.clear()
    }

    /**
     * Sets the trace ability.
     * @param enabled is trace enabled?
     */
    fun setTraceEnabled(enabled: Boolean) {
        isTraceEnabled = enabled
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    private val cache: MutableMap<String, File>

    /**
     * Constructs a new `FileCache` object
     * @param isTraceEnabled is trace enabled?
     */
    init {
        cache = Hashtable()
    }
}