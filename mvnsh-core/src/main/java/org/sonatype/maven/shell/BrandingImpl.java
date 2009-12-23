/*
 * Copyright (C) 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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