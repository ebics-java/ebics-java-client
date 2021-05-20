package org.ebics.client.api

import java.security.interfaces.RSAPublicKey

interface EbicsBankCertificateManager {
    /**
     * Returns the encryption key digest you have obtained from the bank.
     * Ensure that nobody was able to modify the digest on its way from the bank to you.
     * @return the encryption key digest you have obtained from the bank.
     */
    var e002Digest: ByteArray?

    /**
     * Returns the authentication key digest you have obtained from the bank.
     * Ensure that nobody was able to modify the digest on its way from the bank to you.
     * @return the authentication key digest you have obtained from the bank.
     */
    var x002Digest: ByteArray?

    /**
     * Returns the banks encryption key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks encryption key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    /**
     * Returns the banks encryption key.
     * @return the banks encryption key.
     */
    var e002Key: RSAPublicKey?

    /**
     * Returns the banks authentication key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks authentication key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    /**
     * Returns the banks authentication key.
     * @return the banks authentication key.
     */
    var x002Key: RSAPublicKey?

    /**
     * Keys have been fetched from the bank.
     * The getters for the appropriate attributes should return the given values from now on.
     * For the sake of performance the values should be persisted for later usage.
     *
     * @param e002Key the banks encryption key.
     * @param x002Key the banks authentication key.
     */
    fun setBankKeys(e002Key: RSAPublicKey, x002Key: RSAPublicKey) {
        this.e002Key = e002Key
        this.x002Key = x002Key
    }

    /**
     * Sets the bank digests.
     * @param e002Digest encryption digest
     * @param x002Digest authentication digest
     */
    fun setDigests(e002Digest: ByteArray, x002Digest: ByteArray) {
        this.e002Digest = e002Digest
        this.x002Digest = x002Digest
    }
}