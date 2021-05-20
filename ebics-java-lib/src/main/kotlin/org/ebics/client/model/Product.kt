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
package org.ebics.client.model

import java.io.Serializable

/**
 * Optional information about the client product.
 *
 * @author Hachani
 */
class Product
/**
 * Creates a new product information element.
 * @param name this is the name of the product. It is a mandatory field.
 * @param language this is the language. If you use null, the language of the default locale is used.
 * @param instituteID the institute, this is an optional value, you can leave this parameter empty.
 */(
    /**
     * @param name the name to set
     */
    val name: String,
    /**
     * @param language the language to set
     */
    val language: String,
    /**
     * @param instituteID the instituteID to set
     */
    val instituteID: String?
) : Serializable {
    /**
     * @return the name
     */
    /**
     * @return the language
     */
    /**
     * @return the instituteID
     */

    companion object {
        private const val serialVersionUID = 6400195827756653241L
    }
}