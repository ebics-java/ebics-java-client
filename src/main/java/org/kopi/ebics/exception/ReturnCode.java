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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.kopi.ebics.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of EBICS return codes.
 * The return codes are described in chapter 13
 * of EBICS specification.
 *
 *
 */
public class ReturnCode implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ReturnCode.class);
  /**
   * Constructs a new <code>ReturnCode</code> with a given
   * standard code, symbolic name and text
   * @param code the given standard code.
   * @param symbolicName the symbolic name.
   * @param text the code text
   */
  public ReturnCode(String code, String symbolicName, String text) {
    this.code = code;
    this.symbolicName = symbolicName;
    this.text = text;
  }

  /**
   * Throws an equivalent <code>EbicsException</code>
   * @throws EbicsException
   */
  public void throwException() throws EbicsException {
    throw new EbicsException(this, text);
  }

  /**
   * Tells if the return code is an OK one.
   * @return True if the return code is OK one.
   */
  public boolean isOk() {
    return equals(EBICS_OK);
  }

  /**
   * Returns a slightly more human readable version of this return code.
   * @return a slightly more human readable version of this return code.
   */
  public String getSymbolicName() {
    return symbolicName;
  }

  /**
   * Returns a display text for the default locale.
   * @return a text that can be displayed.
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the code.
   * @return the code.
   */
  public int getCode() {
    return Integer.parseInt(code);
  }

  /**
   * Returns the equivalent <code>ReturnCode</code> of a given code
   * @param code the given code
   * @param text the given code text
   * @return the equivalent <code>ReturnCode</code>
   */
  public static ReturnCode toReturnCode(String code, String text) {
      ReturnCode returnCode = returnCodes.get(code);
      if (returnCode != null) {
          return returnCode;
      }
      log.warn("Unknown return code: {}, text: {}", code, text);
      return new ReturnCode(code, text, text);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReturnCode) {
      return this.code.equals(((ReturnCode)obj).code);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }

  @Override
  public String toString() {
    return code + " " + symbolicName + " " + text;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final String			code;
  private final String			symbolicName;
  private final String			text;

  private static final Map<String, ReturnCode> returnCodes = new HashMap<>();
  private static final String		BUNDLE_NAME = "org.kopi.ebics.exception.messages";
  private static final Messages messages = new Messages(BUNDLE_NAME);

  public static final ReturnCode 	EBICS_OK = create("000000", "EBICS_OK");
  public static final ReturnCode 	EBICS_DOWNLOAD_POSTPROCESS_DONE = create("011000", "EBICS_DOWNLOAD_POSTPROCESS_DONE");
  public static final ReturnCode 	EBICS_DOWNLOAD_POSTPROCESS_SKIPPED = create("011001", "EBICS_DOWNLOAD_POSTPROCESS_SKIPPED");
  public static final ReturnCode 	EBICS_TX_SEGMENT_NUMBER_UNDERRUN = create("011101", "EBICS_TX_SEGMENT_NUMBER_UNDERRUN");
  public static final ReturnCode 	EBICS_AUTHENTICATION_FAILED = create("061001", "EBICS_AUTHENTICATION_FAILED");
  public static final ReturnCode 	EBICS_INVALID_REQUEST = create("061002", "EBICS_INVALID_REQUEST");
  public static final ReturnCode 	EBICS_INTERNAL_ERROR = create("061099", "EBICS_INTERNAL_ERROR");
  public static final ReturnCode 	EBICS_TX_RECOVERY_SYNC = create("061101", "EBICS_TX_RECOVERY_SYNC");
  public static final ReturnCode 	EBICS_INVALID_USER_OR_USER_STATE = create("091002", "EBICS_INVALID_USER_OR_USER_STATE");
  public static final ReturnCode 	EBICS_USER_UNKNOWN = create("091003", "EBICS_USER_UNKNOWN");
  public static final ReturnCode 	EBICS_INVALID_USER_STATE = create("091004", "EBICS_INVALID_USER_STATE");
  public static final ReturnCode 	EBICS_INVALID_ORDER_TYPE = create("091005", "EBICS_INVALID_ORDER_TYPE");
  public static final ReturnCode 	EBICS_UNSUPPORTED_ORDER_TYPE = create("091006", "EBICS_UNSUPPORTED_ORDER_TYPE");
  public static final ReturnCode 	EBICS_USER_AUTHENTICATION_REQUIRED = create("091007", "EBICS_USER_AUTHENTICATION_REQUIRED");
  public static final ReturnCode 	EBICS_BANK_PUBKEY_UPDATE_REQUIRED = create("091008", "EBICS_BANK_PUBKEY_UPDATE_REQUIRED");
  public static final ReturnCode 	EBICS_SEGMENT_SIZE_EXCEEDED = create("091009", "EBICS_SEGMENT_SIZE_EXCEEDED");
  public static final ReturnCode 	EBICS_TX_UNKNOWN_TXID = create("091101", "EBICS_TX_UNKNOWN_TXID");
  public static final ReturnCode 	EBICS_TX_ABORT = create("091102", "EBICS_TX_ABORT");
  public static final ReturnCode 	EBICS_TX_MESSAGE_REPLAY = create("091103", "EBICS_TX_MESSAGE_REPLAY");
  public static final ReturnCode	EBICS_TX_SEGMENT_NUMBER_EXCEEDED = create("091104", "EBICS_TX_SEGMENT_NUMBER_EXCEEDED");
  public static final ReturnCode	EBICS_X509_CERTIFICATE_NOT_VALID_YET = create("091209", "EBICS_X509_CERTIFICATE_NOT_VALID_YET");
  public static final ReturnCode	EBICS_MAX_TRANSACTIONS_EXCEEDED = create("091119", "EBICS_MAX_TRANSACTIONS_EXCEEDED");
  public static final ReturnCode	EBICS_SIGNATURE_VERIFICATION_FAILED = create("091301", "EBICS_SIGNATURE_VERIFICATION_FAILED");
  public static final ReturnCode	EBICS_INVALID_ORDER_DATA_FORMAT = create("090004", "EBICS_INVALID_ORDER_DATA_FORMAT");
  public static final ReturnCode	EBICS_NO_DOWNLOAD_DATA_AVAILABLE = create("090005", "EBICS_NO_DOWNLOAD_DATA_AVAILABLE");
  public static final ReturnCode    EBICS_ORDERID_ALREADY_EXISTS = create("091115", "EBICS_ORDERID_ALREADY_EXISTS");
  public static final ReturnCode EBICS_AUTHORISATION_ORDER_TYPE_FAILED = create("090003", "EBICS_AUTHORISATION_ORDER_TYPE_FAILED");
  private static final long 		serialVersionUID = -497883146384363199L;


    private static ReturnCode create(String code, String symbolicName) {
        String text;
        try {
            text = messages.getString(code);
        } catch (MissingResourceException e) {
            text = symbolicName;
        }
        ReturnCode returnCode = new ReturnCode(code, symbolicName, text);
        ReturnCode prev = returnCodes.put(code, returnCode);
        if (prev != null) {
            throw new IllegalStateException("Duplicated code: " + code);
        }
        return returnCode;
    }
}
