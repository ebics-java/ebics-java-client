package org.ebics.client.filetransfer;

import org.ebics.client.session.EbicsSession;

public class AbstractFileTransfer {
    /**
     * Constructs a new FileTransfer session
     *
     * @param session the user session
     */
    public AbstractFileTransfer(EbicsSession session) {
        this.session = session;
    }

    protected EbicsSession session;
}
