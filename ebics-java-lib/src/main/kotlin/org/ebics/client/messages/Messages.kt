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
package org.ebics.client.messages

import java.text.MessageFormat
import java.lang.NullPointerException
import java.util.*

/**
 * A mean to manage application messages.
 *
 * @author Hachani
 * @author Jan Toegel
 */
object Messages {

    /**
     * Default locale are used for setting language
     * can be overridden by Locale.setDefault(...)
     */
    val locale: Locale = Locale.getDefault()

    /**
     * Return the corresponding value of a given key and string parameter.
     * @param key the given key
     * @param bundleName the bundle name
     * @param param the string parameter
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String, param: String?): String = getString(key, bundleName, locale, param)

    /**
     * Return the corresponding value of a given key and integer parameter.
     * @param key the given key
     * @param bundleName the bundle name
     * @param param the int parameter
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String, param: Int): String = getString(key, bundleName, locale, param)

    /**
     * Return the corresponding value of a given key and parameters.
     * @param key the given key
     * @param bundleName the bundle name
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String): String = getString(key, bundleName, locale)

    /**
     * Return the corresponding value of a given key and string parameter.
     * @param key the given key
     * @param bundleName the bundle name
     * @param locale the bundle locale
     * @param param the parameter
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String, locale: Locale, param: String?): String {
        return try {
            val resourceBundle: ResourceBundle = ResourceBundle.getBundle(bundleName, locale)
            MessageFormat.format(resourceBundle.getString(key), param)
        } catch (e: MissingResourceException) {
            "!!$key!!"
        } catch (npe: NullPointerException) {
            if (param != null) "!!$key with param $param!!" else "!!$key!!"
        }
    }

    /**
     * Return the corresponding value of a given key and integer parameter.
     * @param key the given key
     * @param bundleName the bundle name
     * @param locale the bundle locale
     * @param param the parameter
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String, locale: Locale, param: Int): String {
        return try {
            val resourceBundle: ResourceBundle = ResourceBundle.getBundle(bundleName, locale)
            MessageFormat.format(resourceBundle.getString(key), param)
        } catch (e: MissingResourceException) {
            "!!$key!!"
        } catch (e: NullPointerException) {
            "!!$key!!"
        }
    }

    /**
     * Return the corresponding value of a given key and parameters.
     * @param key the given key
     * @param bundleName the bundle name
     * @param locale the bundle locale
     * @return the corresponding key value
     */
    @JvmStatic
    fun getString(key: String, bundleName: String, locale: Locale): String {
        return try {
            val resourceBundle: ResourceBundle = ResourceBundle.getBundle(bundleName, locale)
            resourceBundle.getString(key)
        } catch (e: MissingResourceException) {
            "!!$key!!"
        } catch (e: NullPointerException) {
            "!!$key!!"
        }
    }
}