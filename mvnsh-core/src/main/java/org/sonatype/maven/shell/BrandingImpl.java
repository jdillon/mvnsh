/*
 * Copyright (c) 2007-2009 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell;

import org.sonatype.gshell.branding.BrandingSupport;

import java.io.File;

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
    public String getGoodbyeMessage() {
        return getMessages().format("goodbye");
    }

    @Override
    public String getPrompt() {
        return String.format("@|bold %s|@:${%s}> ", getProgramName(), SHELL_USER_DIR);
    }

    @Override
    public File getUserContextDir() {
        return new File(getUserHomeDir(), ".m2");
    }
}