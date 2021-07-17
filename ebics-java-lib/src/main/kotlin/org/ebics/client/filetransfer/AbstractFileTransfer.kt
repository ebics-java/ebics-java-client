package org.ebics.client.filetransfer

import org.ebics.client.api.EbicsSession

/**
 * Constructs a new FileTransfer session
 *
 * @param session the user session
 */
open class AbstractFileTransfer(protected val session: EbicsSession)