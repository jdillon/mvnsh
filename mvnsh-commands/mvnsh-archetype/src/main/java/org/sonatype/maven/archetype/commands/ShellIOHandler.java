/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at 
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses. 
 *******************************************************************************/

package org.sonatype.maven.archetype.commands;

import jline.console.ConsoleReader;
import org.apache.maven.archetype.ui.prompt.IOHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.shell.ShellHolder;

import java.io.IOException;

/**
 * Shell {@link IOHandler}
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.10
 */
@Component(role = IOHandler.class)
public class ShellIOHandler
    implements IOHandler, Initializable
{
    private IO io;

    private ConsoleReader reader;

    public void initialize() throws InitializationException {
        this.io = ShellHolder.get().getIo();

        try {
            this.reader = new ConsoleReader(
                io.streams.in,
                io.out,
                io.getTerminal());
        }
        catch (IOException e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }

    public String readln() throws IOException {
        assert reader != null;
        return reader.readLine();
    }

    public void write(final String line) throws IOException {
        assert io != null;
        io.out.print(line);
        io.out.flush();
    }

    public void writeln(final String line) throws IOException {
        assert io != null;
        io.out.println(line);
    }
}