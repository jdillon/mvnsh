/*
 * Copyright (c) 2009-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 */
package org.sonatype.maven.shell;

import org.sonatype.gshell.branding.BrandingSupport;
import org.sonatype.gshell.branding.License;
import org.sonatype.gshell.branding.LicenseSupport;
import org.sonatype.gshell.util.PrintBuffer;

import java.io.File;

import static org.sonatype.gshell.variables.VariableNames.SHELL_GROUP;
import static org.sonatype.gshell.variables.VariableNames.SHELL_USER_DIR;

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
        return getMessages().format("displayName");
    }
 
    @Override
    public String getWelcomeMessage() {
        PrintBuffer buff = new PrintBuffer();

        buff.format("%s (%s)", getDisplayName(), getVersion()).println();
        buff.println();
        buff.println("Type '@|bold help|@' for more information.");
        buff.print(line());
        buff.flush();

        return buff.toString();
    }

    @Override
    public String getGoodbyeMessage() {
        return getMessages().format("goodbye");
    }

    @Override
    public String getPrompt() {
        return String.format("@|bold %s|@(${%s}):${%s}> ", getProgramName(), SHELL_GROUP, SHELL_USER_DIR + "~.");
    }

    @Override
    public File getUserContextDir() {
        return new File(getUserHomeDir(), ".m2");
    }

    @Override
    public License getLicense() {
        return new LicenseSupport("Eclipse Public License, 1.0", getClass().getResource("license.txt"));
    }
}