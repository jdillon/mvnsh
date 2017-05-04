/*
 * Copyright (c) 2009-present the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package com.planet57.maven.shell;

import java.util.List;

import com.google.inject.Module;
import com.planet57.gossip.Level;
import com.planet57.gshell.MainSupport;
import com.planet57.gshell.branding.Branding;
import com.planet57.gshell.logging.LoggingSystem;
import com.planet57.gshell.logging.logback.LogbackLoggingSystem;
import com.planet57.gshell.logging.logback.TargetConsoleAppender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Command-line bootstrap for Apache Maven Shell (<tt>mvnsh</tt>).
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class Main
    extends MainSupport
{
  @Override
  protected Branding createBranding() {
    return new BrandingImpl();
  }

  /**
   * Force logback to use current {@link System#out} reference before they are adapted by ThreadIO.
   */
  @Override
  protected void setupLogging(@Nullable final Level level) {
    TargetConsoleAppender.setTarget(System.out);

    // adapt configuration properties for logging unless already set
    if (level != null) {
      maybeSetProperty("shell.logging.console.threshold", level.name());
      maybeSetProperty("shell.logging.file.threshold", level.name());
      maybeSetProperty("shell.logging.root-level", level.name());
    }

    super.setupLogging(level);
  }

  /**
   * Helper to only set a property if not already set.
   */
  private void maybeSetProperty(final String name, final String value) {
    if (System.getProperty(name) == null) {
      System.setProperty(name, value);
    }
  }

  @Override
  protected void configure(@Nonnull final List<Module> modules) {
    super.configure(modules);
    modules.add(binder -> {
      binder.bind(LoggingSystem.class).to(LogbackLoggingSystem.class);
    });
  }

  public static void main(final String[] args) throws Exception {
    new Main().boot(args);
  }
}
