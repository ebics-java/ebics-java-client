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

package org.ebics.client.xml.h004;

import org.ebics.client.api.EbicsSession;
import org.ebics.client.api.trace.h004.TraceSession;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.utils.Utils;

/**
 * The INI request XML element. This root element is to be sent
 * to the ebics server to initiate the signature certificate.
 *
 * @author hachani
 *
 */
public class INIRequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new INI request element.
   * @param session the ebics session.
   */
  public INIRequestElement(EbicsSession session, TraceSession traceSession) {
    super(session);
    this.traceSession = traceSession;
  }

  @Override
  public void build() throws EbicsException {
    SignaturePubKeyOrderDataElement signaturePubKey;

    signaturePubKey = new SignaturePubKeyOrderDataElement(session);
    signaturePubKey.build();
    traceSession.trace(signaturePubKey);
    unsecuredRequest = new UnsecuredRequestElement(session,
	                                           EbicsAdminOrderType.INI,
	                                           Utils.zip(signaturePubKey.prettyPrint()));
    unsecuredRequest.build();
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/h004", "");

    return unsecuredRequest.toByteArray();
  }

  @Override
  public void validate() throws EbicsException {
    unsecuredRequest.validate();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private UnsecuredRequestElement unsecuredRequest;
  private TraceSession traceSession;
  private static final long 		serialVersionUID = -1966559247739923555L;
}
