/*
 * Copyright (c) 1990-2012 kopiRight Managed Solutions GmbH
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

package org.kopi.ebics.letter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.codec.binary.Hex;
import org.kopi.ebics.interfaces.InitLetter;
import org.kopi.ebics.messages.Messages;


public abstract class AbstractInitLetter implements InitLetter {
  
  /**
   * Constructs a new initialization letter.
   * @param locale the application locale
   */
  public AbstractInitLetter(Locale locale) {
    this.locale = locale;
  }

  @Override
  public void save(OutputStream output) throws IOException {
    output.write(letter.getLetter());
    output.flush();
    output.close();
  }
  
  /**
   * Builds an initialization letter.
   * @param hostId the host ID
   * @param bankName the bank name
   * @param userId the user ID
   * @param username the user name
   * @param partnerId the partner ID
   * @param version the signature version
   * @param certTitle the certificate title
   * @param certificate the certificate content
   * @param hashTitle the hash title
   * @param hash the hash value
   * @throws IOException
   */
  protected void build(String hostId, 
                       String bankName, 
                       String userId,
                       String username,
                       String partnerId, 
                       String version,
                       String certTitle, 
                       byte[] certificate, 
                       String hashTitle, 
                       byte[] hash) 
    throws IOException 
  {
    letter = new Letter(getTitle(), 
	                hostId, 
	                bankName, 
	                userId, 
	                username, 
	                partnerId, 
	                version);
    letter.build(certTitle, certificate, hashTitle, hash);
  }
  
  /**
   * Returns the value of the property key. 
   * @param key the property key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @return the property value
   */
  protected String getString(String key, String bundleName, Locale locale) {
    return Messages.getString(key, bundleName, locale);
  }
  
  /**
   * Returns the certificate hash
   * @param certificate the certificate
   * @return the certificate hash
   * @throws GeneralSecurityException 
   */
  protected byte[] getHash(byte[] certificate) throws GeneralSecurityException {
    String			hash256;
    
    hash256 = new String(Hex.encodeHex(MessageDigest.getInstance("SHA-256").digest(certificate), false));
    return format(hash256).getBytes();
  }
  
  /**
   * Formats a hash 256 input.
   * @param hash256 the hash input
   * @return the formatted hash
   */
  private String format(String hash256) {
    StringBuffer	buffer;
    String		formatted;
    
    buffer = new StringBuffer();
    for (int i = 0; i < hash256.length(); i += 2) {
      buffer.append(hash256.charAt(i));
      buffer.append(hash256.charAt(i + 1));
      buffer.append(' ');
    }
    
    formatted = buffer.substring(0, 48) + LINE_SEPARATOR + buffer.substring(48) + LINE_SEPARATOR;
    return formatted;
  }
  
  // --------------------------------------------------------------------
  // INNER CLASS
  // --------------------------------------------------------------------
  
  /**
   * The <code>Letter</code> object is the common template
   * for all initialization letter.
   * 
   * @author Hachani
   *
   */
  private class Letter {
    
    /**
     * Constructs new <code>Letter</code> template
     * @param title the letter title
     * @param hostId the host ID
     * @param bankName the bank name
     * @param userId the user ID
     * @param partnerId the partner ID
     * @param version the signature version
     */
    public Letter(String title,
                  String hostId, 
                  String bankName, 
                  String userId,
                  String username,
                  String partnerId, 
                  String version) 
    {
      this.title = title;
      this.hostId = hostId;
      this.bankName = bankName;
      this.userId = userId;
      this.username = username;
      this.partnerId = partnerId;
      this.version = version;
    }
    
    /**
     * Builds the letter content.
     * @param certTitle the certificate title
     * @param certificate the certificate content
     * @param hashTitle the hash title
     * @param hash the hash content
     * @throws IOException
     */
    public void build(String certTitle, 
                      byte[] certificate, 
                      String hashTitle, 
                      byte[] hash) 
      throws IOException 
    {
      out = new ByteArrayOutputStream();
      writer = new PrintWriter(out, true);
      buildTitle();
      buildHeader();
      buildCertificate(certTitle, certificate);
      buildHash(hashTitle, hash);
      buildFooter();
      writer.close();
      out.flush();
      out.close();
    }
    
    /**
     * Builds the letter title.
     * @throws IOException 
     */
    public void buildTitle() throws IOException {
      emit(title);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
    }
    
    /**
     * Builds the letter header
     * @throws IOException
     */
    public void buildHeader() throws IOException {
      emit(Messages.getString("Letter.date", BUNDLE_NAME, locale));
      appendSpacer();
      emit(formatDate(new Date()));
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.time", BUNDLE_NAME, locale));
      appendSpacer();
      emit(formatTime(new Date()));
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.hostId", BUNDLE_NAME, locale));
      appendSpacer();
      emit(hostId);
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.bank", BUNDLE_NAME, locale));
      appendSpacer();
      emit(bankName);
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.userId", BUNDLE_NAME, locale));
      appendSpacer();
      emit(userId);
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.username", BUNDLE_NAME, locale));
      appendSpacer();
      emit(username);
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.partnerId", BUNDLE_NAME, locale));
      appendSpacer();
      emit(partnerId);
      emit(LINE_SEPARATOR);
      emit(Messages.getString("Letter.version", BUNDLE_NAME, locale));
      appendSpacer();
      emit(version);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
    }
    
    /**
     * Writes the certificate core.
     * @param title the title
     * @param cert the certificate core
     * @throws IOException
     */
    public void buildCertificate(String title, byte[] cert) 
      throws IOException 
    {
      emit(title);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit("-----BEGIN CERTIFICATE-----" + LINE_SEPARATOR);
      emit(new String(cert));
      emit("-----END CERTIFICATE-----" + LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
    }
    
    /**
     * Builds the hash section.
     * @param title the title
     * @param hash the hash value
     * @throws IOException
     */
    public void buildHash(String title, byte[] hash) 
      throws IOException 
    {
      emit(title);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(new String(hash));
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
      emit(LINE_SEPARATOR);
    }
    
    /**
     * Builds the footer section
     * @throws IOException
     */
    public void buildFooter() throws IOException {
      emit(Messages.getString("Letter.date", BUNDLE_NAME, locale));
      emit("                                  ");
      emit(Messages.getString("Letter.signature", BUNDLE_NAME, locale));
    }
    
    /**
     * Appends a spacer
     * @throws IOException 
     */
    public void appendSpacer() throws IOException {
      emit("        ");
    }
    
    /**
     * Emits a text to the writer
     * @param text the text to print
     * @throws IOException
     */
    public void emit(String text) throws IOException {
      writer.write(text);
    }
    
    /**
     * Formats the input date
     * @param date the input date
     * @return the formatted date
     */
    public String formatDate(Date date) {
      SimpleDateFormat		formatter;
      
      formatter = new SimpleDateFormat(Messages.getString("Letter.dateFormat", BUNDLE_NAME, locale), locale);
      return formatter.format(date);
    }
    
    /**
     * Formats the input time
     * @param time the input time
     * @return the formatted time
     */
    public String formatTime(Date time) {
      SimpleDateFormat		formatter;
      
      formatter = new SimpleDateFormat(Messages.getString("Letter.timeFormat", BUNDLE_NAME, locale), locale);
      return formatter.format(time);
    }
    
    /**
     * Returns the letter content
     * @return
     */
    public byte[] getLetter() {
      return out.toByteArray();
    }
    
    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    
    private ByteArrayOutputStream	out;
    private Writer			writer;
    private final String		title;
    private final String		hostId;
    private final String		bankName;
    private final String		userId;
    private final String		username;
    private final String		partnerId;
    private final String		version;
  }
  
  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------
  
  private Letter				letter;
  protected Locale				locale;
  
  protected static final String			BUNDLE_NAME = "org.kopi.ebics.letter.messages";
  private static final String			LINE_SEPARATOR = System.getProperty("line.separator");
}
