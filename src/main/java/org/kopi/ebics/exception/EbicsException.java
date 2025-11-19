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
 * Common exception for all EBICS errors.
 *
 *
 */
public class EbicsException extends Exception {

  /**
   * A means to construct a server error.
   */
  public EbicsException(Throwable cause) {
    super(cause);
  }

  /**
   * A means to construct a server error with an additional message.
   * @param message the exception message
   */
  public EbicsException(String message) {
    super(message);
  }

    public EbicsException(String message, Throwable cause) {
        super(message, cause);
    }
  /**
   * A means to construct a server error with no additional message.
   * @param returnCode the ebics return code.
   */
  public EbicsException(ReturnCode returnCode) {
    super(returnCode.getText());
    this.returnCode = returnCode;
  }

  /**
   * A means to construct a server error with an additional message.
   * @param returnCode the ebics return code.
   * @param message the additional message.
   */
  public EbicsException(ReturnCode returnCode, String message) {
    super(message);
    this.returnCode = returnCode;
  }

  /**
   * Returns the standardized error code.
   * @return the standardized error code.
   */
  public ReturnCode getReturnCode() {
    return returnCode;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private ReturnCode 		returnCode;
  private static final long 	serialVersionUID = 2728820344946361669L;
}
