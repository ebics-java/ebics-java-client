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

package org.kopi.ebics.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A class to manage application messages.
 *
 */
public class Messages {

  private static Locale defaultLocale = Locale.ENGLISH;
  private final Locale locale;
  private final ResourceBundle resourceBundle;

  public Messages(String bundleName, Locale locale) {
    this.locale = locale;
    this.resourceBundle = getBundle(bundleName, locale);
  }

  public Messages(String bundleName) {
    this(bundleName, defaultLocale);
  }

  /**
   * Return the corresponding value of a given key and string parameter.
   * @param key the given key
   * @param arguments object(s) to format
   * @return the corresponding key value
   */
  public String getString(String key, Object ... arguments) {
    try {
      MessageFormat messageFormat = new MessageFormat(resourceBundle.getString(key));
      messageFormat.setLocale(locale);
      return messageFormat.format(arguments);
    } catch (MissingResourceException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the corresponding value of a given key and parameters.
   * @param key the given key
   * @return the corresponding key value
   */
  public String getString(String key) {
      return resourceBundle.getString(key);
  }

  private static ResourceBundle getBundle(String bundleName, Locale locale) {
    try {
      return ResourceBundle.getBundle(bundleName, locale);
    } catch (MissingResourceException e) {
      try {
        return ResourceBundle.getBundle(bundleName, Locale.ENGLISH);
      } catch (MissingResourceException e2) {
        throw new RuntimeException(e2);
      }
    }
  }

  public static void setLocale(Locale locale) {
    Messages.defaultLocale = locale;
  }
}
