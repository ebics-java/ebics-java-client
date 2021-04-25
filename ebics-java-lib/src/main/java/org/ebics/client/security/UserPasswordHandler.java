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

package org.ebics.client.security;

import org.ebics.client.interfaces.PasswordCallback;

/**
 * A simple user password handler that creates a password
 * from the user id and a simple suffix.
 *
 * @author hachani
 *
 */
public class UserPasswordHandler implements PasswordCallback {

  /**
   * Creates a new user password handler from a given user id
   * and a given suffix.
   * @param userId the user id.
   * @param suffix the user suffix.
   */
  public UserPasswordHandler(String userId, String suffix) {
    this.userId = userId;
    this.suffix = suffix;
  }

  @Override
  public char[] getPassword() {
    String		pwd;

    pwd = userId + suffix;
    return pwd.toCharArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String		userId;
  private String		suffix;
}
