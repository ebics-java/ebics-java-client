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
package org.ebics.client.filetransfer.h004

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.TransferState
import org.ebics.client.exception.EbicsException
import org.ebics.client.filetransfer.AbstractFileTransfer
import org.ebics.client.http.HttpRequestSender
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.io.Joiner
import org.ebics.client.messages.Messages.getString
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h004.EbicsDownloadOrder
import org.ebics.client.order.h004.EbicsUploadOrder
import org.ebics.client.order.h004.EbicsUploadOrderResponse
import org.ebics.client.order.h004.OrderType
import org.ebics.client.utils.Constants
import org.ebics.client.xml.h004.*
import org.ebics.client.xml.h004.HTDResponseOrderDataElement
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream

/**
 * Handling of file transfers.
 * Files can be transferred to and fetched from the bank.
 * Every transfer may be performed in a recoverable way.
 * For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call.
 * To use the recoverable transfer mode, you may set a working
 * directory for temporarily created files.
 *
 *
 *  EBICS specification 2.4.2 - 6.2 Encryption at application level
 *
 *
 * In the event of an upload transaction, a random symmetrical key is generated in the
 * customer system that is used exclusively within the framework of this transaction both for
 * encryption of the ES’s and for encryption of the order data. This key is encrypted
 * asymmetrically with the financial institution’s public encryption key and is transmitted by the
 * customer system to the bank system during the initialization phase of the transaction.
 *
 *
 * Analogously, in the case of a download transaction a random symmetrical key is generated
 * in the bank system that is used for encryption of the order data that is to be downloaded and
 * for encryption of the bank-technical signature that has been provided by the financial
 * institution. This key is asymmetrically encrypted and is transmitted by the bank system to the
 * customer system during the initialization phase of the transaction. The asymmetrical
 * encryption takes place with the technical subscriber’s public encryption key if the
 * transaction’s EBICS messages are sent by a technical subscriber. Otherwise the
 * asymmetrical encryption takes place with the public encryption key of the non-technical
 * subscriber, i.e. the submitter of the order.
 *
 * @author Hachani
 */
/**
 * Constructs a new FileTransfer session
 *
 * @param session the user session
 */
class FileTransfer(session: EbicsSession) : AbstractFileTransfer(session) {
    /**
     * Initiates a file transfer to the bank.
     * @param content The bytes you want to send.
     * @param uploadOrder As which order type details
     * @throws IOException
     * @throws EbicsException
     */
    @Throws(IOException::class, EbicsException::class)
    fun sendFile(content: ByteArray, uploadOrder: EbicsUploadOrder): EbicsUploadOrderResponse {
        val sender = HttpRequestSender(session)
        val initializer = UploadInitializationRequestElement(
            session,
            uploadOrder.adminOrderType, uploadOrder.orderType, uploadOrder.attributeType,
            content
        )
        initializer.build()
        initializer.validate()
        session.configuration.traceManager.trace(initializer.userSignature, session)
        session.configuration.traceManager.trace(initializer, session)
        val responseBody = sender.send(ByteArrayContentFactory(initializer.prettyPrint()))
        
        val response = InitializationResponseElement(
            responseBody,
            DefaultEbicsRootElement.generateName(uploadOrder.orderType)
        )
        response.build()
        session.configuration.traceManager.trace(response, session)
        val state = TransferState(initializer.segmentNumber, response.transactionId)
        while (state.hasNext()) {
            val segmentNumber = state.next()
            sendFileSegment(
                initializer.getContent(segmentNumber), segmentNumber, state.isLastSegment,
                state.transactionId, uploadOrder.orderType ?: "FUL"
            )
        }
        return EbicsUploadOrderResponse(response.orderNumber, response.transactionId)
    }

    /**
     * Sends a segment to the ebics bank server.
     * @param factory the content factory that contain the segment data.
     * @param segmentNumber the segment number
     * @param lastSegment is it the last segment?
     * @param transactionId the transaction Id
     * @param orderType the order type
     * @throws IOException
     * @throws EbicsException
     */
    @Throws(IOException::class, EbicsException::class)
    protected fun sendFileSegment(
        factory: ContentFactory,
        segmentNumber: Int,
        lastSegment: Boolean,
        transactionId: ByteArray,
        orderType: String
    ) {
        logger.info(
            getString(
                "upload.segment",
                Constants.APPLICATION_BUNDLE_NAME,
                segmentNumber
            )
        )
        val uploader = UploadTransferRequestElement(
            session,
            orderType,
            segmentNumber,
            lastSegment,
            transactionId,
            factory
        )
        val sender = HttpRequestSender(session)
        uploader.build()
        uploader.validate()
        session.configuration.traceManager.trace(uploader, session)
        val responseBody = sender.send(ByteArrayContentFactory(uploader.prettyPrint()))
        
        val response = TransferResponseElement(
            responseBody,
            DefaultEbicsRootElement.generateName(orderType)
        )
        response.build()
        session.configuration.traceManager.trace(response, session)
    }

    fun getHTD(): HTDResponseOrderDataElement {
        val bos = ByteOutputStream()
        fetchFile(EbicsDownloadOrder(EbicsAdminOrderType.HTD, null, null, null), bos)
        return HTDResponseOrderDataElement(ByteArrayContentFactory(bos.bytes)).apply {
            build()
            validate()
        }
    }

    /**
     * Shortcut to ordertypes from HTD xml, for further processing
     */
    fun getOrderTypes(): List<OrderType> = getHTD().getOrderTypes()

    /**
     * Fetches a file of the given order type from the bank.
     * You may give an optional start and end date.
     * This type of transfer will run until everything is processed.
     * No transaction recovery is possible.
     * @param downloadOrder type of file to fetch
     * @param outputFile dest where to put the data
     * @throws IOException communication error
     * @throws EbicsException server generated error
     */
    @Throws(IOException::class, EbicsException::class)
    fun fetchFile(
        downloadOrder: EbicsDownloadOrder,
        outputStream: OutputStream
    ) {
        val sender = HttpRequestSender(session)
        val initializer = DownloadInitializationRequestElement(
            session,
            downloadOrder.adminOrderType,
            downloadOrder.orderType,
            downloadOrder.startDate,
            downloadOrder.endDate
        )
        initializer.build()
        initializer.validate()
        session.configuration.traceManager.trace(initializer, session)
        val responseBody = sender.send(ByteArrayContentFactory(initializer.prettyPrint()))
        
        val response = DownloadInitializationResponseElement(
            responseBody,
            DefaultEbicsRootElement.generateName(downloadOrder.adminOrderType)
        )
        response.build()
        session.configuration.traceManager.trace(response, session)
        response.report()
        val state = TransferState(response.segmentsNumber, response.transactionId)
        state.setSegmentNumber(response.segmentNumber)
        val joiner = Joiner(session.userCert)
        joiner.append(response.orderData)
        while (state.hasNext()) {
            val segmentNumber: Int = state.next()
            fetchFileSegment(
                downloadOrder.adminOrderType,
                segmentNumber,
                state.isLastSegment,
                state.transactionId,
                joiner
            )
        }
        outputStream.use { dest -> joiner.writeTo(dest, response.transactionKey) }
        val receipt = ReceiptRequestElement(
            session,
            state.transactionId,
            DefaultEbicsRootElement.generateName(downloadOrder.adminOrderType)
        )
        receipt.build()
        receipt.validate()
        session.configuration.traceManager.trace(receipt, session)
        val receiptResponseBody = sender.send(ByteArrayContentFactory(receipt.prettyPrint()))
        
        val receiptResponse = ReceiptResponseElement(
            receiptResponseBody,
            DefaultEbicsRootElement.generateName(downloadOrder.adminOrderType)
        )
        receiptResponse.build()
        session.configuration.traceManager.trace(receiptResponse, session)
        receiptResponse.report()
    }

    /**
     * Fetches a given portion of a file.
     * @param adminOrderType the order type
     * @param segmentNumber the segment number
     * @param lastSegment is it the last segment?
     * @param transactionId the transaction ID
     * @param joiner the portions joiner
     * @throws IOException communication error
     * @throws EbicsException server generated error
     */
    @Throws(IOException::class, EbicsException::class)
    protected fun fetchFileSegment(
        adminOrderType: EbicsAdminOrderType,
        segmentNumber: Int,
        lastSegment: Boolean,
        transactionId: ByteArray,
        joiner: Joiner
    ) {
        val sender = HttpRequestSender(session)
        val downloader = DownloadTransferRequestElement(
            session,
            adminOrderType,
            segmentNumber,
            lastSegment,
            transactionId
        )
        downloader.build()
        downloader.validate()
        session.configuration.traceManager.trace(downloader, session)
        val responseBody = sender.send(ByteArrayContentFactory(downloader.prettyPrint()))
        
        val response = DownloadTransferResponseElement(
            responseBody,
            adminOrderType,
            DefaultEbicsRootElement.generateName(adminOrderType)
        )
        response.build()
        session.configuration.traceManager.trace(response, session)
        response.report()
        joiner.append(response.orderData)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileTransfer::class.java)
    }
}