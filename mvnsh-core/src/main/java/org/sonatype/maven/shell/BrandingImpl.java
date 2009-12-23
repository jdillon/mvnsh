/*
 * Sonatype Maven Shell (TM) Professional Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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