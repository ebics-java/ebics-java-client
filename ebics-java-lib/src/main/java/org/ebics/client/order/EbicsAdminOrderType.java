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

package org.ebics.client.order;

/**
 * EBICS order type.
 *
 * @author Pierre Ducroquet
 *
 */
public enum EbicsAdminOrderType {
    HIA,
    HAA,
    HKD,
    HPB,
    HPD,
    HTD,
    INI,
    SPR,
    FUL, //EBICS 2.4/2.5 FR Upload (standard business order types)
    FDL, //EBICS 2.4/2.5 FR Download (standard business order types)
    UPL, //EBICS 2.4/2.5 DE Upload (standard business order types)
    DNL, //EBICS 2.4/2.5 DE Download (standard business order types)
    BTU, //EBICS 3.0 Upload
    BTD  //EBICS 3.0 Download
}
