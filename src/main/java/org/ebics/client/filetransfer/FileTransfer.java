package org.ebics.client.filetransfer;

import org.ebics.client.exception.EbicsException;
import org.ebics.client.order.EbicsDownloadOrder;
import org.ebics.client.order.EbicsUploadOrder;
import org.ebics.client.session.EbicsSession;

import java.io.File;
import java.io.IOException;

public abstract class FileTransfer {
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
     * @param uploadOrder As which order details
     * @throws IOException
     * @throws EbicsException
     */
    public abstract void sendFile(byte[] content, EbicsUploadOrder uploadOrder)
            throws IOException, EbicsException;

    /**
     * Fetches a file of the given order type from the bank.
     * You may give an optional start and end date.
     * This type of transfer will run until everything is processed.
     * No transaction recovery is possible.
     * @param downloadOrder type details of file to fetch
     * @param outputFile dest where to put the data
     * @throws IOException communication error
     * @throws EbicsException server generated error
     */
    public abstract void fetchFile(EbicsDownloadOrder downloadOrder, File outputFile)
            throws IOException, EbicsException;

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    protected EbicsSession			session;
}
