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

package org.kopi.ebics.exception;

/**
 * Download operation failed due to lack of data.
 *
 *
 */
public class NoDownloadDataAvailableException extends EbicsException {
    public NoDownloadDataAvailableException() {
        super(ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE);
    }
    public NoDownloadDataAvailableException(String message) {
        super(ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE, message);
    }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long 			serialVersionUID = -5156261061322817326L;
}
