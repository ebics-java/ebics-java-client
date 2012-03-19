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

package org.kopi.ebics.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.PasswordCallback;
import org.kopi.ebics.interfaces.Savable;

import org.kopi.ebics.certificate.CertificateManager;
import gnu.crypto.sig.rsa.RSAPKCS1V1_5Signature;

/**
 * Simple implementation of an EBICS user.
 * This object is not serializable, but it should be persisted every time it needs to be saved.
 * Persistence is achieved via <code>save(ObjectOutputStream)</code> and the matching constructor.
 *
 * @author Hachani
 *
 */
public class User implements EbicsUser, Savable {

  /**
   * First time constructor. Use this constructor,
   * if you want to prepare the first communication with the bank. For further communications you should recover persisted objects.
   * All required signature keys will be generated now.
   *
   * @param partner customer in whose name we operate.
   * @param userId UserId as obtained from the bank.
   * @param name the user name,
   * @param email the user email
   * @param country the user country
   * @param organisation the user organization or company
   * @param passwordCallback a callback-handler that supplies us with the password.
   *                         This parameter can be null, in this case no password is used.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  public User(EbicsPartner partner,
              String userId,
              String name,
              String email,
              String country,
              String organization,
              PasswordCallback passwordCallback)
    throws GeneralSecurityException, IOException
  {
    this.partner = partner;
    this.userId = userId;
    this.name = name;
    this.dn = makeDN(name, email, country, organization);
    this.passwordCallback = passwordCallback;
    createUserCertificates();
    needSave = true;
  }

  /**
   * Reconstructs a persisted EBICS user.
   *
   * @param partner the customer in whose name we operate.
   * @param ois the object stream
   * @param passwordCallback a callback-handler that supplies us with the password.
   * @throws IOException
   * @throws GeneralSecurityException if the supplies password is wrong.
   * @throws ClassNotFoundException
   */
  public User(EbicsPartner partner,
              ObjectInputStream ois,
              PasswordCallback passwordCallback)
    throws IOException, GeneralSecurityException, ClassNotFoundException
  {
    this.partner = partner;
    this.passwordCallback = passwordCallback;
    this.userId = ois.readUTF();
    this.name = ois.readUTF();
    this.dn = ois.readUTF();
    this.isInitialized = ois.readBoolean();
    this.isInitializedHIA = ois.readBoolean();
    this.a005Certificate = (X509Certificate)ois.readObject();
    this.e002Certificate = (X509Certificate)ois.readObject();
    this.x002Certificate = (X509Certificate)ois.readObject();
    this.a005PrivateKey = (PrivateKey)ois.readObject();
    this.e002PrivateKey = (PrivateKey)ois.readObject();
    this.x002PrivateKey = (PrivateKey)ois.readObject();
    ois.close();
  }

  /**
   * Reconstructs a an EBICS user by loading its certificate
   * from a given key store.
   * @param partner the customer in whose name we operate.
   * @param userId UserID as obtained from the bank.
   * @param keystorePath the key store path
   * @param passwordCallback a callback-handler that supplies us with the password.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public User(EbicsPartner partner,
              String userId,
              String keystorePath,
              PasswordCallback passwordCallback)
    throws GeneralSecurityException, IOException
  {
    this.partner = partner;
    this.userId = userId;
    this.passwordCallback = passwordCallback;
    loadCertificates(keystorePath);
    needSave = true;
  }

  /**
   * Creates new certificates for a user.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  private void createUserCertificates() throws GeneralSecurityException, IOException {
    manager = new CertificateManager(this);
    manager.create();
  }

  /**
   * Saves the user certificates in a given path
   * @param path the certificates path
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void saveUserCertificates(String path) throws GeneralSecurityException, IOException {
    if (manager == null) {
      throw new GeneralSecurityException("Cannot save user certificates");
    }

    manager.save(path, passwordCallback);
  }

  /**
   * Loads the user certificates from a key store
   * @param keyStorePath the key store path
   * @throws GeneralSecurityException
   * @throws IOException
   */
  private void loadCertificates(String keyStorePath)
    throws GeneralSecurityException, IOException
  {
    CertificateManager			manager;

    manager = new CertificateManager(this);
    manager.load(keyStorePath, passwordCallback);
  }

  @Override
  public void save(ObjectOutputStream oos) throws IOException {
    oos.writeUTF(userId);
    oos.writeUTF(name);
    oos.writeUTF(dn);
    oos.writeBoolean(isInitialized);
    oos.writeBoolean(isInitializedHIA);
    oos.writeObject(a005Certificate);
    oos.writeObject(e002Certificate);
    oos.writeObject(x002Certificate);
    oos.writeObject(a005PrivateKey);
    oos.writeObject(e002PrivateKey);
    oos.writeObject(x002PrivateKey);
    oos.flush();
    oos.close();
    needSave = false;
  }

  /**
   * Has the users signature key been sent to the bank?
   * @return True if the users signature key been sent to the bank
   */
  public boolean isInitialized() {
    return isInitialized;
  }

  /**
   * The users signature key has been sent to the bank.
   * @param isInitialized transfer successful?
   */
  public void setInitialized(boolean isInitialized) {
    this.isInitialized = isInitialized;
    needSave = true;
  }

  /**
   * Have the users authentication and encryption keys been sent to the bank?
   * @return True if the users authentication and encryption keys been sent to the bank.
   */
  public boolean isInitializedHIA() {
    return isInitializedHIA;
  }

  /**
   * The users authentication and encryption keys have been sent to the bank.
   * @param isInitializedHIA transfer successful?
   */
  public void setInitializedHIA(boolean isInitializedHIA) {
    this.isInitializedHIA = isInitializedHIA;
    needSave = true;
  }

  /**
   * Generates new keys for this user and sends them to the bank.
   * @param keymgmt the key management instance with the ebics session.
   * @param passwordCallback the password-callback for the new keys.
   * @throws EbicsException Exception during server request
   * @throws IOException Exception during server request
   */
  public void updateKeys(KeyManagement keymgmt, PasswordCallback passwordCallback)
    throws EbicsException, IOException
  {
    needSave = true;
  }

  /**
   * Writes a byte buffer from offset to length
   * @param buf the given byte buffer
   * @param offset the offset
   * @param length the length
   * @return The byte buffer portion corresponding to the given length and offset
   */
  public static byte[] write(byte[] buf, int offset, int length) {
    ByteArrayOutputStream		output;

    output = new ByteArrayOutputStream();
    for (int i = 0; i < length; i++) {
      if ((buf[i] == 13) || (buf[i] == 10) || (buf[i] == 26)) {
        continue;
      }
      output.write(buf[i]);
    }

    return output.toByteArray();
  }

  /**
   * Makes the Distinguished Names for the user certificates.
   * @param name the user name
   * @param email the user email
   * @param country the user country
   * @param organization the user organization
   * @return
   */
  private String makeDN(String name,
                        String email,
                        String country,
                        String organization)
  {
    StringBuffer		buffer;

    buffer = new StringBuffer();

    buffer.append("CN=" + name);
    if (country != null) {
      buffer.append(", " + "C=" + country.toUpperCase());
    }
    if (organization != null) {
      buffer.append(", " + "O=" + organization);
    }
    if (email != null) {
      buffer.append(", " + "E=" + email);
    }

    return buffer.toString();
  }

  /**
   * Did any persistable attribute change since last load/save operation.
   * @return True if the object needs to be saved.
   */
  public boolean needsSave() {
    return needSave;
  }

  @Override
  public byte[] getA005Certificate() throws EbicsException {
    try {
      return a005Certificate.getEncoded();
    } catch (CertificateEncodingException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public byte[] getE002Certificate() throws EbicsException {
    try {
      return e002Certificate.getEncoded();
    } catch (CertificateEncodingException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public byte[] getX002Certificate() throws EbicsException {
    try {
      return x002Certificate.getEncoded();
    } catch (CertificateEncodingException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public void setA005Certificate(X509Certificate a005Certificate) {
    this.a005Certificate = a005Certificate;
    needSave = true;
  }

  @Override
  public void setE002Certificate(X509Certificate e002Certificate) {
    this.e002Certificate = e002Certificate;
    needSave = true;
  }

  @Override
  public void setX002Certificate(X509Certificate x002Certificate) {
    this.x002Certificate = x002Certificate;
    needSave = true;
  }

  @Override
  public RSAPublicKey getA005PublicKey() {
    return (RSAPublicKey) a005Certificate.getPublicKey();
  }

  @Override
  public RSAPublicKey getE002PublicKey() {
    return (RSAPublicKey) e002Certificate.getPublicKey();
  }

  @Override
  public RSAPublicKey getX002PublicKey() {
    return (RSAPublicKey) x002Certificate.getPublicKey();
  }

  @Override
  public void setA005PrivateKey(PrivateKey a005PrivateKey) {
    this.a005PrivateKey = a005PrivateKey;
    needSave = true;
  }

  @Override
  public void setX002PrivateKey(PrivateKey x002PrivateKey) {
    this.x002PrivateKey = x002PrivateKey;
    needSave = true;
  }

  @Override
  public void setE002PrivateKey(PrivateKey e002PrivateKey) {
    this.e002PrivateKey = e002PrivateKey;
    needSave = true;
  }

  @Override
  public String getSecurityMedium() {
    return "0000";
  }

  @Override
  public EbicsPartner getPartner() {
    return partner;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDN() {
    return dn;
  }

  @Override
  public PasswordCallback getPasswordCallback() {
    return passwordCallback;
  }

  @Override
  public String getSaveName() {
    return userId + ".cer";
  }

  @Override
  public byte[] authenticate(byte[] digest) throws GeneralSecurityException {
    Signature			signature;

    signature = Signature.getInstance("SHA256WithRSA", "BC");
    signature.initSign(x002PrivateKey);
    signature.update(digest);
    return signature.sign();
  }

  @Override
  public byte[] sign(byte[] digest) throws IOException {
    RSAPKCS1V1_5Signature 	signature;
    Map<String, PrivateKey>	attributes;
    BufferedInputStream		input;
    byte[] 			bytes;

    signature = new RSAPKCS1V1_5Signature("sha-256");
    attributes = new HashMap<String, PrivateKey>();
    attributes.put("gnu.crypto.sig.private.key", a005PrivateKey);
    signature.setupSign(attributes);
    input = new BufferedInputStream(new ByteArrayInputStream(digest));
    bytes = new byte[4096];

    int 		count = 0;
    for (int i = input.read(bytes); count != -1; count = input.read(bytes)) {
      byte[]		buf;

      buf = write(bytes, 0, i);
      signature.update(buf, 0, buf.length);
    }

    input.close();

    return (byte[]) signature.sign();
  }

  @Override
  public byte[] decrypt(byte[] encryptedKey, byte[] transactionKey)
    throws GeneralSecurityException, IOException
  {
    Cipher			cipher;
    int				blockSize;
    ByteArrayOutputStream	outputStream;
    byte[]			bytes;
    SecretKeySpec		keySpec;
    IvParameterSpec		iv;

    cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
    cipher.init(Cipher.DECRYPT_MODE, e002PrivateKey);
    blockSize = cipher.getBlockSize();
    outputStream = new ByteArrayOutputStream(64);
    for (int j = 0; transactionKey.length - j * blockSize > 0; j++) {
      outputStream.write(cipher.doFinal(transactionKey, j * blockSize, blockSize));
    }
    keySpec = new SecretKeySpec(outputStream.toByteArray(), "EAS");
    bytes = new byte[16];
    iv = new IvParameterSpec(bytes);

    cipher = Cipher.getInstance("AES/CBC/ISO10126Padding", "BC");
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
    return cipher.doFinal(encryptedKey);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsPartner				partner;
  private String				userId;
  private String				name;
  private String				dn;
  private boolean				isInitializedHIA;
  private boolean				isInitialized;
  private PasswordCallback 			passwordCallback;
  private transient boolean			needSave;
  private CertificateManager			manager;

  private PrivateKey				a005PrivateKey;
  private PrivateKey				e002PrivateKey;
  private PrivateKey				x002PrivateKey;

  private X509Certificate			a005Certificate;
  private X509Certificate			e002Certificate;
  private X509Certificate			x002Certificate;
}
