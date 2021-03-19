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

import org.ebics.client.exception.EbicsException;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.order.EbicsOrderType;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h004.MutableHeaderType;
import org.ebics.schema.h004.MutableHeaderType.SegmentNumber;
import org.ebics.schema.h004.StaticHeaderType;

/**
 * The <code>DTransferRequestElement</code> is the common elements
 * for all ebics downloads.
 *
 * @author Hachani
 *
 */
public class DownloadTransferRequestElement extends TransferRequestElement {

  /**
   * Constructs a new <code>DTransferRequestElement</code> element.
   * @param session the current ebics session
   * @param type the order type
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction ID
   */
  public DownloadTransferRequestElement(EbicsSession session,
                                        EbicsOrderType type,
                                        int segmentNumber,
                                        boolean lastSegment,
                                        byte[] transactionId)
  {
    super(session, generateName(type), type, segmentNumber, lastSegment, transactionId);
  }

  @Override
  public void buildTransfer() throws EbicsException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    SegmentNumber			segmentNumber;
    StaticHeaderType 			xstatic;

    segmentNumber = EbicsXmlFactory.createSegmentNumber(this.segmentNumber, lastSegment);
    mutable = EbicsXmlFactory.createMutableHeaderType("Transfer", segmentNumber);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createEbicsRequestBody();
    request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
        				         session.getConfiguration().getVersion(),
	                                         header,
	                                         body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long serialVersionUID = -7765739964317408967L;
}
