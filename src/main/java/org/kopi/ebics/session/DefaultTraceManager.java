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
import java.io.FileOutputStream;
import java.io.IOException;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.EbicsRootElement;
import org.kopi.ebics.interfaces.TraceManager;
import org.kopi.ebics.io.FileCache;


/**
 * The <code>DefaultTraceManager</code> aims to trace an ebics
 * transferable element in an instance of <code>java.io.File</code>
 * then saved to a trace directory.
 * The manager can delete all traces file if the configuration does
 * not offer tracing support.
 * see {@link Configuration#isTraceEnabled() isTraceEnabled()}
 *
 *
 */
public class DefaultTraceManager implements TraceManager {

  /**
   * Constructs a new <code>TraceManger</code> to manage transfer traces.
   * @param traceDir the trace directory
   * @param isTraceEnabled is trace enabled?
   */
  public DefaultTraceManager(File traceDir, boolean isTraceEnabled) {
    this.traceDir = traceDir;
    cache = new FileCache(isTraceEnabled);
  }

  /**
   * Constructs a new <code>TraceManger</code> to manage transfer traces.
   * @param isTraceEnabled is trace enabled?
   */
  public DefaultTraceManager(boolean isTraceEnabled) {
    this(null, isTraceEnabled);
  }

  /**
   * Constructs a new <code>TraceManger</code> with trace option enabled.
   */
  public DefaultTraceManager() {
    this(null, true);
  }

  @Override
  public void trace(EbicsRootElement element) throws EbicsException {
    try {
      var file = new File(traceDir, element.getName());
      var out = new FileOutputStream(file);
      element.save(out);
      cache.add(file);
    } catch (IOException e) {
      throw new EbicsException(e.getMessage());
    }
  }

  @Override
  public void remove(EbicsRootElement element) {
    cache.remove(element.getName());
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public void setTraceDirectory(File traceDir) {
    this.traceDir = traceDir;
  }

  @Override
  public void setTraceEnabled(boolean enabled) {
    cache.setTraceEnabled(enabled);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private File				traceDir;
  private final FileCache			cache;
}
