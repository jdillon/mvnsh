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

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.planet57.gshell.branding.Branding;
import com.planet57.gshell.console.ConsoleErrorHandler;
import com.planet57.gshell.console.ConsolePrompt;
import com.planet57.gshell.guice.GuiceMainSupport;
import com.planet57.gshell.logging.LoggingSystem;
import com.planet57.gshell.logging.logback.LogbackLoggingSystem;
import com.planet57.gshell.shell.ShellErrorHandler;
import com.planet57.gshell.shell.ShellPrompt;

/**
 * Command-line bootstrap for Apache Maven Shell (<tt>mvnsh</tt>).
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class Main
    extends GuiceMainSupport
{
  @Override
  protected void configure(final List<Module> modules) {
    super.configure(modules);

    Module custom = new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(LoggingSystem.class).to(LogbackLoggingSystem.class);
        bind(ConsolePrompt.class).to(ShellPrompt.class);
        bind(ConsoleErrorHandler.class).to(ShellErrorHandler.class);
        bind(Branding.class).to(BrandingImpl.class);
      }
    };

    modules.add(custom);
  }

  public static void main(final String[] args) throws Exception {
    new Main().boot(args);
  }
}
