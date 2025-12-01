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

package org.kopi.ebics.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.kopi.ebics.utils.Utils;

/**
 * A simple SSL socket factory for EBICS client.
 *
 *
 */
public class EbicsSocketFactory extends SSLSocketFactory {

  /**
   * Constructs a new <code>EbicsSocketFactory</code> from an SSL context
   * @param context the <code>SSLContext</code>
   */
  public EbicsSocketFactory(SSLContext context) {
    this.context = context;
  }

  /**
   * Constructs a new <code>EbicsSocketFactory</code> from
   * key store and trust store information
   * @param keystore the key store
   * @param keystoreType the key store type
   * @param keystorePass the key store password
   * @param truststore the trust store
   * @param truststoreType the trust store type
   * @param truststorePass the trust store password
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public EbicsSocketFactory(byte[] keystore,
                            String keystoreType,
                            char[] keystorePass,
                            byte[] truststore,
                            String truststoreType,
                            char[] truststorePass)
    throws IOException, GeneralSecurityException
  {
    this.context = getSSLContext(keystore,
	                         keystoreType,
	                         keystorePass,
	                         truststore,
	                         truststoreType,
	                         truststorePass);
  }

  /**
   * Returns the <code>SSLContext</code> from key store information.
   * @param keystore the key store
   * @param keystoreType the key store type
   * @param keystorePass the key store password
   * @param truststore the trust store
   * @param truststoreType the trust store type
   * @param truststorePass the trust store password
   * @return the <code>SSLContext</code>
   * @throws IOException
   * @throws GeneralSecurityException
   */
  public SSLContext getSSLContext(byte[] keystore,
                                  String keystoreType,
                                  char[] keystorePass,
                                  byte[] truststore,
                                  String truststoreType,
                                  char[] truststorePass)
    throws IOException, GeneralSecurityException
  {
    KeyStore 			kstore;
    KeyStore 			tstore;
    KeyManagerFactory 		kmf;
    TrustManagerFactory 	tmf;
    SSLContext			context;

    kstore = initKeyStore(keystore, keystorePass, keystoreType);
    kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(kstore, keystorePass);

    tstore = initKeyStore(truststore, truststorePass, truststoreType);
    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(tstore);
    context = SSLContext.getInstance("TLS");
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), Utils.secureRandom);

    return context;
  }

  /**
   * Initializes a key store.
   * @param keystore the key store
   * @param password the key store password
   * @return key store
   * @throws IOException
   */
  protected KeyStore initKeyStore(byte[] keystore, char[] password, String type)
    throws IOException
  {
    try {
      KeyStore  kstore;

      kstore = KeyStore.getInstance(type);
      kstore.load(new ByteArrayInputStream(keystore), password);
      return kstore;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
        throw new IOException("Exception trying to load keystore " + type + ": " + e, e);
    }
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
    throws IOException
  {
    return context.getSocketFactory().createSocket(s, host, port, autoClose);
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return context.getSocketFactory().getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return context.getSocketFactory().getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return context.getSocketFactory().createSocket(host, port);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return context.getSocketFactory().createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
    throws IOException {
    return context.getSocketFactory().createSocket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
    throws IOException
  {
    return context.getSocketFactory().createSocket(address, port, localAddress, localPort);
  }

  private final SSLContext			context;
}
