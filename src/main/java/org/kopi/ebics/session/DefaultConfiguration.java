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

package org.kopi.ebics.session;

import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.LetterManager;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.interfaces.TraceManager;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.letter.DefaultLetterManager;


/**
 * A simple client application configuration.
 *
 *
 */
public class DefaultConfiguration implements Configuration {

  /**
   * Creates a new application configuration.
   * @param rootDir the root directory
   */
  public DefaultConfiguration(File rootDir, Properties properties) {
    this.rootDir = rootDir;
    bundle = ResourceBundle.getBundle(RESOURCE_DIR);
    this.properties = properties;
    serializationManager = new DefaultSerializationManager();
    traceManager = new DefaultTraceManager();
  }

  /**
   * Returns the corresponding property of the given key
   * @param key the property key
   * @return the property value.
   */
  private String getString(String key) {
    try {
      return bundle.getString(key);
    } catch(MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  @Override
  public File getRootDirectory() {
    return rootDir;
  }

  @Override
  public void init() {
    //Create the root directory
    IOUtils.createDirectories(getRootDirectory());
    //Create the serialization directory
    IOUtils.createDirectories(getSerializationDirectory());
    //create the SSL trusted stores directories
    IOUtils.createDirectories(getSSLTrustedStoreDirectory());
    //create the SSL key stores directories
    IOUtils.createDirectories(getSSLKeyStoreDirectory());
    //Create the SSL bank certificates directories
    IOUtils.createDirectories(getSSLBankCertificates());
    //Create users directory
    IOUtils.createDirectories(getUsersDirectory());

    serializationManager.setSerializationDirectory(getSerializationDirectory());
    traceManager.setTraceEnabled(isTraceEnabled());
    letterManager = new DefaultLetterManager(getLocale());
  }

  @Override
  public Locale getLocale() {
    return Locale.FRANCE;
  }

  @Override
  public File getConfigurationFile() {
    return rootDir(getString("conf.file.name"));
  }

  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  @Override
  public File getKeystoreDirectory(EbicsUser user) {
      return new File(getUserDirectory(user), getString("keystore.dir.name"));
  }

  @Override
  public File getTransferTraceDirectory(EbicsUser user) {
    return new File(getUserDirectory(user), getString("traces.dir.name"));
  }

  @Override
  public File getSerializationDirectory() {
    return rootDir(getString("serialization.dir.name"));
  }

  @Override
  public File getSSLTrustedStoreDirectory() {
    return rootDir(getString("ssltruststore.dir.name"));
  }

  @Override
  public File getSSLKeyStoreDirectory() {
    return rootDir(getString("sslkeystore.dir.name"));
  }

  @Override
  public File getSSLBankCertificates() {
    return rootDir(getString("sslbankcert.dir.name"));
  }

  @Override
  public File getUsersDirectory() {
    return rootDir(getString("users.dir.name"));
  }

  private File rootDir(String name) {
    return new File(rootDir, name);
  }

  @Override
  public SerializationManager getSerializationManager() {
    return serializationManager;
  }

  @Override
  public TraceManager getTraceManager() {
    return traceManager;
  }

  @Override
  public LetterManager getLetterManager() {
    return letterManager;
  }

  @Override
  public File getLettersDirectory(EbicsUser user) {
    return new File(getUserDirectory(user), getString("letters.dir.name"));
  }

  @Override
  public File getUserDirectory(EbicsUser user) {
    return new File(getUsersDirectory(), user.getUserId());
  }

  @Override
  public String getSignatureVersion() {
    return getString("signature.version");
  }

  @Override
  public String getAuthenticationVersion() {
    return getString("authentication.version");
  }

  @Override
  public String getEncryptionVersion() {
    return getString("encryption.version");
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public boolean isCompressionEnabled() {
    return true;
  }

  @Override
  public int getRevision() {
    return 1;
  }

  @Override
  public String getVersion() {
    return getString("ebics.version");
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private final File rootDir;
  private final ResourceBundle bundle;
  private final Properties properties;
  private final SerializationManager serializationManager;
  private final TraceManager traceManager;
  private LetterManager letterManager;

  private static final String RESOURCE_DIR = "org.kopi.ebics.client.config";
}
