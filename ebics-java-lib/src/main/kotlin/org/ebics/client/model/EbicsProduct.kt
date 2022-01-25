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

/**
 * Optional information about the client product.
 *
 * @author Hachani
 */
interface EbicsProduct
{
    /**
     * @param name his is the name of the product. It is a mandatory field.
     */
    val name: String

    /**
     * @param language this is the language. If you use null, the language of the default locale is used.
     */
    val language: String

    /**
     * @param instituteID the institute, this is an optional value, you can leave this parameter empty.
     */
    val instituteID: String?
}