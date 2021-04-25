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

package org.ebics.client.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A mean to manage application messages.
 *
 * @author Hachani
 *
 */
public class Messages {

  /**
   * Return the corresponding value of a given key and string parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param param the string parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, String param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    } catch (NullPointerException npe) {
      if (param != null)
        return "!!" + key + " with param " + param + "!!";
      else
        return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and integer parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param param the int parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, int param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException | NullPointerException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and parameters.
   * @param key the given key
   * @param bundleName the bundle name
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return resourceBundle.getString(key);
    } catch (MissingResourceException | NullPointerException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and string parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @param param the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale, String param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    } catch (NullPointerException npe) {
      if (param != null)
        return "!!" + key + " with param " + param + "!!";
      else
        return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and integer parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @param param the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale, int param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException | NullPointerException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and parameters.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return resourceBundle.getString(key);
    } catch (MissingResourceException | NullPointerException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Sets the default locale.
   * @param locale the locale
   */
  public static void setLocale(Locale locale) {
    Messages.locale = locale;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static Locale					locale;
}
