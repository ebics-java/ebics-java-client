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

package org.kopi.ebics.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.io.Joiner;
import org.kopi.ebics.messages.Messages;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.Constants;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.DefaultEbicsRootElement;
import org.kopi.ebics.xml.DownloadInitializationRequestElement;
import org.kopi.ebics.xml.DownloadInitializationResponseElement;
import org.kopi.ebics.xml.DownloadTransferRequestElement;
import org.kopi.ebics.xml.DownloadTransferResponseElement;
import org.kopi.ebics.xml.InitializationResponseElement;
import org.kopi.ebics.xml.ReceiptRequestElement;
import org.kopi.ebics.xml.ReceiptResponseElement;
import org.kopi.ebics.xml.TransferResponseElement;
import org.kopi.ebics.xml.UploadInitializationRequestElement;
import org.kopi.ebics.xml.UploadTransferRequestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handling of file transfers.
 * Files can be transferred to and fetched from the bank.
 * Every transfer may be performed in a recoverable way.
 * For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call.
 * To use the recoverable transfer mode, you may set a working
 * directory for temporarily created files.
 *
 * <p> EBICS specification 2.4.2 - 6.2 Encryption at application level
 *
 * <p>In the event of an upload transaction, a random symmetrical key is generated in the
 * customer system that is used exclusively within the framework of this transaction both for
 * encryption of the ES’s and for encryption of the order data. This key is encrypted
 * asymmetrically with the financial institution’s public encryption key and is transmitted by the
 * customer system to the bank system during the initialization phase of the transaction.
 *
 * <p>Analogously, in the case of a download transaction a random symmetrical key is generated
 * in the bank system that is used for encryption of the order data that is to be downloaded and
 * for encryption of the bank-technical signature that has been provided by the financial
 * institution. This key is asymmetrically encrypted and is transmitted by the bank system to the
 * customer system during the initialization phase of the transaction. The asymmetrical
 * encryption takes place with the technical subscriber’s public encryption key if the
 * transaction’s EBICS messages are sent by a technical subscriber. Otherwise the
 * asymmetrical encryption takes place with the public encryption key of the non-technical
 * subscriber, i.e. the submitter of the order.
 *
 *
 */
public class FileTransfer {
    private static final Logger log = LoggerFactory.getLogger(FileTransfer.class);
  /**
   * Constructs a new FileTransfer session
   * @param session the user session
   */
  public FileTransfer(EbicsSession session) {
    this.session = session;
  }

  /**
   * Initiates a file transfer to the bank.
   * @param content The bytes you want to send.
   * @param orderType As which order type
   * @throws IOException
   * @throws EbicsException
   */
  public void sendFile(byte[] content, EbicsOrderType orderType, EbicsUploadParams params)
    throws IOException, EbicsException
  {
    HttpRequestSender sender = new HttpRequestSender(session);
    var initializer = new UploadInitializationRequestElement(session, orderType, params, content);
    initializer.build();
    initializer.validate();
    session.getConfiguration().getTraceManager().trace(initializer.getUserSignature());
    session.getConfiguration().getTraceManager().trace(initializer);
    int httpCode = sender.send(new ByteArrayContentFactory(initializer.prettyPrint()));

    Utils.checkHttpCode(httpCode);
    InitializationResponseElement response = new InitializationResponseElement(sender.getResponseBody(),
	                                         orderType,
	                                         DefaultEbicsRootElement.generateName(orderType));
    response.build();
    session.getConfiguration().getTraceManager().trace(response);

    TransferState state = new TransferState(initializer.getSegmentNumber(), response.getTransactionId());

        while (state.hasNext()) {
            int segmentNumber = state.next();
            sendFile(initializer.getContent(segmentNumber), segmentNumber, state.isLastSegment(),
                state.getTransactionId(), orderType);
        }
    }

  /**
   * Sends a segment to the ebics bank server.
   * @param factory the content factory that contain the segment data.
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction id
   * @param orderType the order type
   * @throws IOException
   * @throws EbicsException
   */
  public void sendFile(ContentFactory factory,
                       int segmentNumber,
                       boolean lastSegment,
                       byte[] transactionId,
                       EbicsOrderType orderType)
    throws IOException, EbicsException
  {
    UploadTransferRequestElement		uploader;
    HttpRequestSender			sender;
    TransferResponseElement		response;
    int					httpCode;

    Messages messages = new Messages(Constants.APPLICATION_BUNDLE_NAME);
    log.info(messages.getString("upload.segment", segmentNumber));
    uploader = new UploadTransferRequestElement(session,
	                                   orderType,
	                                   segmentNumber,
	                                   lastSegment,
	                                   transactionId,
	                                   factory);
    sender = new HttpRequestSender(session);
    uploader.build();
    uploader.validate();
    session.getConfiguration().getTraceManager().trace(uploader);
    httpCode = sender.send(new ByteArrayContentFactory(uploader.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new TransferResponseElement(sender.getResponseBody(),
	                                   DefaultEbicsRootElement.generateName(orderType));
    response.build();
    session.getConfiguration().getTraceManager().trace(response);
  }

  /**
   * Fetches a file of the given order type from the bank.
   * You may give an optional start and end date.
   * This type of transfer will run until everything is processed.
   * No transaction recovery is possible.
   * @param orderType type of file to fetch
   * @param outputFile where to put the data
   * @throws IOException communication error
   * @throws EbicsException server generated error
   */
  public void fetchFile(EbicsOrderType orderType,
                        File outputFile)
    throws IOException, EbicsException
  {
    var sender = new HttpRequestSender(session);
      var initializer = new DownloadInitializationRequestElement(session, orderType);
    initializer.build();
    initializer.validate();

    session.getConfiguration().getTraceManager().trace(initializer);
    var request = initializer.prettyPrint();
    var httpCode = sender.send(new ByteArrayContentFactory(request));
    Utils.checkHttpCode(httpCode);
    var response = new DownloadInitializationResponseElement(sender.getResponseBody(),
	                                          orderType,
	                                          DefaultEbicsRootElement.generateName(orderType));

    response.build();
    session.getConfiguration().getTraceManager().trace(response);
    response.report();
    var state = new TransferState(response.getSegmentsNumber(), response.getTransactionId());
    state.setSegmentNumber(response.getSegmentNumber());
    var joiner = new Joiner(session.getUser());
    joiner.append(response.getOrderData());
    while(state.hasNext()) {
      int segmentNumber = state.next();
      fetchFile(orderType,
	        segmentNumber,
	        state.isLastSegment(),
	        state.getTransactionId(),
	        joiner);
    }

    try (FileOutputStream dest = new FileOutputStream(outputFile)) {
        joiner.writeTo(dest, response.getTransactionKey());
    }
    var receipt = new ReceiptRequestElement(session,
	                                state.getTransactionId(),
	                                DefaultEbicsRootElement.generateName(orderType));
    receipt.build();
    receipt.validate();
    session.getConfiguration().getTraceManager().trace(receipt);
    httpCode = sender.send(new ByteArrayContentFactory(receipt.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    var receiptResponse = new ReceiptResponseElement(sender.getResponseBody(),
	                                         DefaultEbicsRootElement.generateName(orderType));
    receiptResponse.build();
    session.getConfiguration().getTraceManager().trace(receiptResponse);
    receiptResponse.report();
  }

  /**
   * Fetches a given portion of a file.
   * @param orderType the order type
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction ID
   * @param joiner the portions joiner
   * @throws IOException communication error
   * @throws EbicsException server generated error
   */
  public void fetchFile(EbicsOrderType orderType,
                        int segmentNumber,
                        boolean lastSegment,
                        byte[] transactionId,
                        Joiner joiner)
    throws IOException, EbicsException
  {
    DownloadTransferRequestElement		downloader;
    HttpRequestSender			sender;
    DownloadTransferResponseElement		response;
    int					httpCode;

    sender = new HttpRequestSender(session);
    downloader = new DownloadTransferRequestElement(session,
	                                     orderType,
	                                     segmentNumber,
	                                     lastSegment,
	                                     transactionId);
    downloader.build();
    downloader.validate();
    session.getConfiguration().getTraceManager().trace(downloader);
    httpCode = sender.send(new ByteArrayContentFactory(downloader.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new DownloadTransferResponseElement(sender.getResponseBody(),
	                                    orderType,
	                                    DefaultEbicsRootElement.generateName(orderType));
    response.build();
    session.getConfiguration().getTraceManager().trace(response);
    response.report();
    joiner.append(response.getOrderData());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------
  private final EbicsSession session;
}
