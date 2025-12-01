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

package org.kopi.ebics.certificate;

/**
 * X509 certificate constants
 *
 *
 */
public interface X509Constants {

  /**
   * Certificates key usage
   */
  enum CertificateKeyUsage {
      SIGNATURE_KEY_USAGE, AUTHENTICATION_KEY_USAGE, ENCRYPTION_KEY_USAGE,
  }

  /**
   * Certificate signature algorithm
   */
  String		SIGNATURE_ALGORITHM			= "SHA256WithRSAEncryption";

  /**
   * Default days validity of a certificate
   */
  int			DEFAULT_DURATION			= 10 * 365;

  /**
   * EBICS key size
   */
  int			EBICS_KEY_SIZE				= 2048;
}
