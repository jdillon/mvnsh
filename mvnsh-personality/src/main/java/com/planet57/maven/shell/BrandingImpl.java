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

import java.io.File;

import com.planet57.gshell.branding.BrandingSupport;
import com.planet57.gshell.branding.License;
import com.planet57.gshell.branding.LicenseSupport;
import com.planet57.gshell.util.io.PrintBuffer;

import javax.annotation.Nullable;

import static com.planet57.gshell.variables.VariableNames.SHELL_GROUP;
import static com.planet57.gshell.variables.VariableNames.SHELL_USER_DIR;

/**
 * Branding for <tt>mvnsh</tt>.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class BrandingImpl
    extends BrandingSupport
{
  @Override
  public String getDisplayName() {
    return "@|bold,red Maven|@ @|bold Shell|@";
  }

  @Override
  public String getWelcomeMessage() {
    PrintBuffer buff = new PrintBuffer();
    buff.format("%s (%s)%n%n", getDisplayName(), getVersion());
    buff.println("Type '@|bold help|@' for more information.");
    buff.format("@|intensity_faint %s|@", LINE_TOKEN);
    return buff.toString();
  }

  @Override
  public String getGoodbyeMessage() {
    return "@|green Goodbye!|@";
  }

  @Override
  public String getPrompt() {
    // FIXME: may need to adjust ansi-renderer syntax or pre-render before expanding to avoid needing escapes
    return String.format("\\@\\|bold %s\\|\\@\\(${%s}\\):${%s}> ", getProgramName(), SHELL_GROUP, SHELL_USER_DIR);
  }

  @Nullable
  @Override
  public String getRightPrompt() {
    // FIXME: may need to adjust ansi-renderer syntax or pre-render before expanding to avoid needing escapes
    return "\\@\\|intensity_faint $(date)\\|\\@";
  }

  @Override
  public File getUserContextDir() {
    return new File(getUserHomeDir(), ".m2");
  }

  @Override
  public License getLicense() {
    return new LicenseSupport("Eclipse Public License, 1.0", getClass().getResource("license.txt").toExternalForm());
  }
}
