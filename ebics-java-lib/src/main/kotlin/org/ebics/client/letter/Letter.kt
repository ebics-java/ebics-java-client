package org.ebics.client.letter

import org.ebics.client.messages.Messages
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * The `Letter` object is the common template
 * for all initialization letter.
 *
 * @author Hachani
 */
class Letter
/**
 * Constructs new `Letter` template
 * @param title the letter title
 * @param hostId the host ID
 * @param bankName the bank name
 * @param userId the user ID
 * @param partnerId the partner ID
 * @param version the signature version
 * @param certTitle the certificate title
 * @param certificate the certificate content
 * @param hashTitle the hash title
 * @param hash the hash content
 */
    (
    private val title: String,
    private val hostId: String,
    private val bankName: String,
    private val userId: String,
    private val username: String,
    private val partnerId: String,
    private val version: String,
    certTitle: String,
    private val certificate: ByteArray?,
    private val hashTitle: String,
    private val hash: ByteArray,
    private val locale: Locale
) {
    private val out: ByteArrayOutputStream = ByteArrayOutputStream()
    private val writer: Writer = PrintWriter(out, true)

    init {
        buildTitle()
        buildHeader()
        buildCertificate(certTitle, certificate)
        buildHash(hashTitle, hash)
        buildFooter()
        writer.close()
        out.flush()
        out.close()
    }

    /**
     * Builds the letter title.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildTitle() {
        emit(title)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
    }

    /**
     * Builds the letter header
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildHeader() {
        emit(Messages.getString("Letter.date", BUNDLE_NAME, locale))
        appendSpacer()
        emit(formatDate(Date()))
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.time", BUNDLE_NAME, locale))
        appendSpacer()
        emit(formatTime(Date()))
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.hostId", BUNDLE_NAME, locale))
        appendSpacer()
        emit(hostId)
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.bank", BUNDLE_NAME, locale))
        appendSpacer()
        emit(bankName)
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.userId", BUNDLE_NAME, locale))
        appendSpacer()
        emit(userId)
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.username", BUNDLE_NAME, locale))
        appendSpacer()
        emit(username)
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.partnerId", BUNDLE_NAME, locale))
        appendSpacer()
        emit(partnerId)
        emit(LINE_SEPARATOR)
        emit(Messages.getString("Letter.version", BUNDLE_NAME, locale))
        appendSpacer()
        emit(version)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
    }

    /**
     * Writes the certificate core.
     * @param title the title
     * @param cert the certificate core
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildCertificate(title: String, cert: ByteArray?) {
        emit(title)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit("-----BEGIN CERTIFICATE-----${LINE_SEPARATOR}")
        if (cert != null)
            emit(String(cert))
        emit("-----END CERTIFICATE-----${LINE_SEPARATOR}")
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
    }

    /**
     * Builds the hash section.
     * @param title the title
     * @param hash the hash value
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildHash(title: String, hash: ByteArray) {
        emit(title)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(String(hash))
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
        emit(LINE_SEPARATOR)
    }

    /**
     * Builds the footer section
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildFooter() {
        emit(Messages.getString("Letter.date", BUNDLE_NAME, locale))
        emit("                                  ")
        emit(Messages.getString("Letter.signature", BUNDLE_NAME, locale))
    }

    /**
     * Appends a spacer
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun appendSpacer() {
        emit("        ")
    }

    /**
     * Emits a text to the writer
     * @param text the text to print
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun emit(text: String) {
        writer.write(text)
    }

    /**
     * Formats the input date
     * @param date the input date
     * @return the formatted date
     */
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat(
            Messages.getString(
                "Letter.dateFormat",
                BUNDLE_NAME,
                locale
            ), locale
        )
        return formatter.format(date)
    }

    /**
     * Formats the input time
     * @param time the input time
     * @return the formatted time
     */
    private fun formatTime(time: Date): String {
        val formatter = SimpleDateFormat(
            Messages.getString("Letter.timeFormat", BUNDLE_NAME, locale), locale
        )
        return formatter.format(time)
    }

    /**
     * Returns the letter content
     * @return
     */
    fun getLetterBytes(): ByteArray {
        return out.toByteArray()
    }

    companion object {
        @JvmStatic
        private val BUNDLE_NAME = "org.ebics.client.letter.messages"
        private val LINE_SEPARATOR = System.getProperty("line.separator")
    }
}