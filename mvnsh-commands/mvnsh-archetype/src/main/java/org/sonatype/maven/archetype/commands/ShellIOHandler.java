/*
 * Copyright (C) 2009 the original author(s).
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