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

package org.kopi.ebics.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.EbicsLogger;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.LetterManager;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.interfaces.TraceManager;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.letter.DefaultLetterManager;


/**
 * A simple client application configuration.
 *
 * Loads client configuration from a property file, but allows properties
 * to be overridden from the System properties.
 *
 * @author hachani
 *
 */
public class DefaultConfiguration implements Configuration {

  /**
   * Creates a new application configuration.
   * @param rootDir the root directory
   */
  public DefaultConfiguration(String rootDir, Properties properties) {
    this.rootDir = rootDir;
    bundle = ResourceBundle.getBundle(RESOURCE_DIR);
    this.properties = properties;
    logger = new DefaultEbicsLogger();
    serializationManager = new DefaultSerializationManager();
    traceManager = new DefaultTraceManager();
  }

  /**
   * Returns the corresponding property of the given key.
   *
   * First, attempts to load property from system properties. If none found,
   * loads the property from the bundle.
   *
   * @param key the property key
   * @return the property value.
   */
  private String getString(String key) {
    try {
      return System.getProperty(key, bundle.getString(key));
    } catch(MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  @Override
  public String getRootDirectory() {
    return rootDir;
  }

  @Override
  public void init() {
    //Create the root directory
    IOUtils.createDirectories(getRootDirectory());
    //Create the logs directory
    IOUtils.createDirectories(getLogDirectory());
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

    logger.setLogFile(resolveFile(getLogDirectory(), getLogFileName()));
    ((DefaultEbicsLogger)logger).setFileLoggingEnabled(true);
    ((DefaultEbicsLogger)logger).setLevel(DefaultEbicsLogger.ALL_LEVEL);
    serializationManager.setSerializationDirectory(getSerializationDirectory());
    traceManager.setTraceEnabled(isTraceEnabled());
    letterManager = new DefaultLetterManager(getLocale());
  }

  /**
   * Returns absolute file name or prepends directory name.
   *
   * <p> If fileName starts with path separator, just returns the fileName. 
   * Otherwise, prepends directory name and a path separator.
   *
   * @param directory directory name
   * @param file file name
   * @return 
   */
  public static String resolveFile(String directory, String file) {
    if (file != null && file.startsWith(File.separator)) { 
      return file;
    } else {
      return directory + File.separator + file;
    }
  }

  @Override
  public Locale getLocale() {
    return Locale.FRANCE;
  }

  @Override
  public String getLogDirectory() {
    return resolveFile(rootDir, getString("log.dir.name"));
  }

  @Override
  public String getLogFileName() {
    return getString("log.file.name");
  }

  @Override
  public String getConfigurationFile() {
    return resolveFile(rootDir, getString("conf.file.name"));
  }

  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  @Override
  public String getKeystoreDirectory(EbicsUser user) {
    return resolveFile(getUserDirectory(user), getString("keystore.dir.name"));
  }

  @Override
  public String getTransferTraceDirectory(EbicsUser user) {
    return resolveFile(getUserDirectory(user), getString("traces.dir.name"));
  }

  @Override
  public String getSerializationDirectory() {
    return resolveFile(rootDir, getString("serialization.dir.name"));
  }

  @Override
  public String getSSLTrustedStoreDirectory() {
    return resolveFile(rootDir, getString("ssltruststore.dir.name"));
  }

  @Override
  public String getSSLKeyStoreDirectory() {
    return rootDir + File.separator + getString("sslkeystore.dir.name");
  }

  @Override
  public String getSSLBankCertificates() {
    return resolveFile(rootDir, getString("sslbankcert.dir.name"));
  }

  @Override
  public String getUsersDirectory() {
    return resolveFile(rootDir, getString("users.dir.name"));
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
  public String getLettersDirectory(EbicsUser user) {
    return resolveFile(getUserDirectory(user), getString("letters.dir.name"));
  }

  @Override
  public String getUserDirectory(EbicsUser user) {
    return resolveFile(getUsersDirectory(), user.getUserId());
  }

  @Override
  public EbicsLogger getLogger() {
    return logger;
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

  private final String rootDir;
  private final ResourceBundle bundle;
  private final Properties properties;
  private final EbicsLogger logger;
  private final SerializationManager serializationManager;
  private final TraceManager traceManager;
  private LetterManager letterManager;

  private static final String RESOURCE_DIR = "org.kopi.ebics.client.config";
}
