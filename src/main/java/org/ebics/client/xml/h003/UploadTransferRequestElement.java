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

package org.ebics.client.xml.h003;

import org.ebics.client.exception.EbicsException;
import org.ebics.client.interfaces.ContentFactory;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.order.EbicsOrderType;
import org.ebics.client.io.IOUtils;
import org.ebics.schema.h003.DataTransferRequestType;
import org.ebics.schema.h003.DataTransferRequestType.OrderData;
import org.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h003.MutableHeaderType;
import org.ebics.schema.h003.MutableHeaderType.SegmentNumber;
import org.ebics.schema.h003.StaticHeaderType;

/**
 * The <code>UTransferRequestElement</code> is the root element
 * for all ebics upload transfers.
 *
 * @author Hachani
 *
 */
public class UploadTransferRequestElement extends TransferRequestElement {

  /**
   * Constructs a new <code>UTransferRequestElement</code> for ebics upload transfer.
   * @param session the current ebics session
   * @param orderType the upload order type
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction ID
   * @param content the content factory
   */
  public UploadTransferRequestElement(EbicsSession session,
                                      EbicsOrderType orderType,
                                      int segmentNumber,
                                      boolean lastSegment,
                                      byte[] transactionId,
                                      ContentFactory content)
  {
    super(session, generateName(orderType), orderType, segmentNumber, lastSegment, transactionId);
    this.content = content;
  }

  @Override
  public void buildTransfer() throws EbicsException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    SegmentNumber			segmentNumber;
    StaticHeaderType 			xstatic;
    OrderData 				orderData;
    DataTransferRequestType 		dataTransfer;

    segmentNumber = EbicsXmlFactory.createSegmentNumber(this.segmentNumber, lastSegment);
    mutable = EbicsXmlFactory.createMutableHeaderType("Transfer", segmentNumber);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    orderData = EbicsXmlFactory.createEbicsRequestOrderData(IOUtils.getFactoryContent(content));
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(orderData);
    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
    request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
	                                         session.getConfiguration().getVersion(),
	                                         header,
	                                         body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private ContentFactory		content;
  private static final long 		serialVersionUID = 8465397978597444978L;
}
