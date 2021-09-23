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

package org.kopi.ebics.certificate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.PasswordCallback;

/**
 * Simple manager for EBICS certificates.
 *
 * @author hacheni
 *
 */
public class CertificateManager {

  public CertificateManager(EbicsUser user) {
    this.user = user;
    generator = new X509Generator();
  }

  /**
   * Creates the certificates for the user
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void create() throws GeneralSecurityException, IOException {
    Calendar		calendar;

    calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION);

    createA005Certificate(new Date(calendar.getTimeInMillis()));
    createX002Certificate(new Date(calendar.getTimeInMillis()));
    createE002Certificate(new Date(calendar.getTimeInMillis()));
    setUserCertificates();
  }

  /**
   * Sets the user certificates
   */
  private void setUserCertificates() {
    user.setA005Certificate(a005Certificate);
    user.setX002Certificate(x002Certificate);
    user.setE002Certificate(e002Certificate);

    user.setA005PrivateKey(a005PrivateKey);
    user.setX002PrivateKey(x002PrivateKey);
    user.setE002PrivateKey(e002PrivateKey);
  }

  /**
   * Creates the signature certificate.
   * @param end the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createA005Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair			keypair;

    keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);
    a005Certificate = generator.generateA005Certificate(keypair,
	                                                user.getDN(),
	                                                new Date(),
	                                                end);
    a005PrivateKey = keypair.getPrivate();
  }

  /**
   * Creates the authentication certificate.
   * @param the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createX002Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair			keypair;

    keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);
    x002Certificate = generator.generateX002Certificate(keypair,
	                                                user.getDN(),
	                                                new Date(),
	                                                end);
    x002PrivateKey = keypair.getPrivate();
  }

  /**
   * Creates the encryption certificate.
   * @param end the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createE002Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair			keypair;

    keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);
    e002Certificate = generator.generateE002Certificate(keypair,
	                                                user.getDN(),
	                                                new Date(),
	                                                end);
    e002PrivateKey = keypair.getPrivate();
  }

  /**
   * Saves the certificates in PKCS12 format
   * @param path the certificates path
   * @param pwdCallBack the password call back
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void save(String path, PasswordCallback pwdCallBack)
    throws GeneralSecurityException, IOException
  {
    writePKCS12Certificate(path + "/" + user.getUserId(), pwdCallBack.getPassword());
  }

  /**
   * Loads user certificates from a given key store
   * @param path the key store path
   * @param pwdCallBack the password call back
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void load(String path, PasswordCallback pwdCallBack)
    throws GeneralSecurityException, IOException
  {
    KeyStoreManager		loader;

    loader = new KeyStoreManager();

    loader.load(path, pwdCallBack.getPassword());
    a005Certificate = loader.getCertificate(user.getUserId() + "-A005");
    x002Certificate = loader.getCertificate(user.getUserId() + "-X002");
    e002Certificate = loader.getCertificate(user.getUserId() + "-E002");

    a005PrivateKey = loader.getPrivateKey(user.getUserId() + "-A005");
    x002PrivateKey = loader.getPrivateKey(user.getUserId() + "-X002");
    e002PrivateKey = loader.getPrivateKey(user.getUserId() + "-E002");
    setUserCertificates();
  }

  /**
   * Writes a the generated certificates into a PKCS12 key store.
   * @param filename the key store file name
   * @param password the key password
   * @throws IOException
   */
  public void writePKCS12Certificate(String filename, char[] password)
    throws GeneralSecurityException, IOException
  {
    if (filename == null || "".equals(filename)) {
      throw new IOException("The file name cannot be empty");
    }

    if (!filename.toLowerCase().endsWith(".p12")) {
      filename += ".p12";
    }

    FileOutputStream fos = new FileOutputStream(filename);
    writePKCS12Certificate(password, fos);
    fos.close();
  }

  /**
   * Writes a the generated certificates into a PKCS12 key store.
   * @param password the key store password
   * @param fos the output stream
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void writePKCS12Certificate(char[] password, OutputStream fos)
    throws GeneralSecurityException, IOException
  {
    KeyStore			keystore;

    keystore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
    keystore.load(null, null);
    keystore.setKeyEntry(user.getUserId() + "-A005",
	                 a005PrivateKey,
	                 password,
	                 new X509Certificate[] {a005Certificate});
    keystore.setKeyEntry(user.getUserId() + "-X002",
	                 x002PrivateKey,
	                 password,
	                 new X509Certificate[] {x002Certificate});
    keystore.setKeyEntry(user.getUserId() + "-E002",
	                 e002PrivateKey,
	                 password,
	                 new X509Certificate[] {e002Certificate});
    keystore.store(fos, password);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private X509Generator					generator;
  private EbicsUser					user;

  private X509Certificate				a005Certificate;
  private X509Certificate				e002Certificate;
  private X509Certificate				x002Certificate;

  private PrivateKey					a005PrivateKey;
  private PrivateKey					x002PrivateKey;
  private PrivateKey					e002PrivateKey;
}
