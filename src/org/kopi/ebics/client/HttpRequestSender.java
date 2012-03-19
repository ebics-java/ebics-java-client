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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.InputStreamContentFactory;
import org.kopi.ebics.session.EbicsSession;


/**
 * A simple HTTP request sender and receiver.
 * The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 *
 */
public class HttpRequestSender {

  /**
   * Constructs a new <code>HttpRequestSender</code> with a
   * given ebics session.
   * @param session the ebics session
   */
  public HttpRequestSender(EbicsSession session) {
    this.session = session;
  }

  /**
   * Sends the request contained in the <code>ContentFactory</code>.
   * The <code>ContentFactory</code> will deliver the request as
   * an <code>InputStream</code>.
   *
   * @param request the ebics request
   * @return the HTTP return code
   */
  public final int send(ContentFactory request) throws IOException {
    HttpClient			httpClient;
    String                      proxyConfiguration;

    httpClient = new HttpClient();
    proxyConfiguration = session.getConfiguration().getProperty("http.proxyHost");

    if (proxyConfiguration != null && !proxyConfiguration.equals("")) {
      HostConfiguration		hostConfig;
      String			proxyHost;
      int			proxyPort;

      hostConfig = httpClient.getHostConfiguration();
      proxyHost = session.getConfiguration().getProperty("http.proxyHost").trim();
      proxyPort = Integer.parseInt(session.getConfiguration().getProperty("http.proxyPort").trim());
      hostConfig.setProxy(proxyHost, proxyPort);
      if (!session.getConfiguration().getProperty("http.proxyUser").equals("")) {
	String				user;
	String				pwd;
	UsernamePasswordCredentials	credentials;
	AuthScope			authscope;

	user = session.getConfiguration().getProperty("http.proxyUser").trim();
	pwd = session.getConfiguration().getProperty("http.proxyPassword").trim();
	credentials = new UsernamePasswordCredentials(user, pwd);
	authscope = new AuthScope(proxyHost, proxyPort);
	httpClient.getState().setProxyCredentials(authscope, credentials);
      }
    }

      PostMethod			method;
      RequestEntity			requestEntity;
      InputStream			input;
      int				retCode;

      input = request.getContent();
      method = new PostMethod(session.getUser().getPartner().getBank().getURL().toString());
      method.getParams().setSoTimeout(30000);
      requestEntity = new InputStreamRequestEntity(input);
      method.setRequestEntity(requestEntity);
      method.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
      retCode = -1;
      retCode = httpClient.executeMethod(method);
      response = new InputStreamContentFactory(method.getResponseBodyAsStream());

      return retCode;
  }

  /**
   * Returns the content factory of the response body
   * @return the content factory of the response.
   */
  public ContentFactory getResponseBody() {
    return response;
  }

  //////////////////////////////////////////////////////////////////
  // DATA MEMBERS
  //////////////////////////////////////////////////////////////////

  private EbicsSession				session;
  private ContentFactory			response;
}
