/*
 * Copyright (C) 2010 the original author or authors.
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

package org.sonatype.maven.shell.commands.maven;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Adds ANSI color to Maven output.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class ColorizingStream
    extends PrintStream
{
    private final StringBuilder buff = new StringBuilder();

    public ColorizingStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void write(final int b) {
        buff.append((char) b);
        if (b == '\n') {
            writeBuffer();
        }
    }

    @Override
    public void write(final byte buf[], final int off, final int len) {
        for (int i = off; i < off + len; i++) {
            write(buf[i]);
        }
    }

    private static enum State
    {
        DEFAULT,
        START_HEADER,
        IN_HEADER
    }

    private State state = State.DEFAULT;

    //
    // FIXME: The details of this should be externalized
    //
    
    private void writeBuffer() {
        String line = buff.toString();
        buff.setLength(0);

        if (line.startsWith("[INFO] ------------------------------------------------------------------------")) {
            line = ansi().a(INTENSITY_BOLD).a(line).reset().toString();

            switch (state) {
                case START_HEADER:
                    state = State.IN_HEADER;
                    break;

                case IN_HEADER:
                    state = State.DEFAULT;
                    break;

                default:
                    state = State.IN_HEADER;
                    break;
            }
        }
        else if (state == State.IN_HEADER && line.startsWith("[INFO] Building")) {
            line = ansi().a(INTENSITY_BOLD).a(line).reset().toString();
        }
        else if (state == State.IN_HEADER) {
            state = State.DEFAULT;
        }

        if (line.startsWith("[INFO] Reactor Build Order:")) {
            line = ansi().a(INTENSITY_BOLD).a(line).reset().toString();
        }
        else if (line.startsWith("[WARNING]")) {
            line = ansi().fg(RED).a(line).reset().toString();
        }
        else if (line.startsWith("ERROR") || line.contains("FAILURE") || line.contains("FAILED")) {
            line = ansi().a(INTENSITY_BOLD).fg(RED).a(line).reset().toString();
        }
        else if (line.startsWith("[ERROR]")) {
            line = ansi().a(INTENSITY_BOLD).fg(RED).a(line).reset().toString();
        }
        else if (line.contains("BUILD SUCCESS") || line.contains(".OK (")) {
            line = ansi().a(INTENSITY_BOLD).fg(GREEN).a(line).reset().toString();
        }
        else if (line.contains("SUCCESS") || line.contains(".OK (")) {
            line = ansi().fg(GREEN).a(line).reset().toString();
        }
        else if (line.startsWith("[INFO] ---") && line.trim().endsWith(" ---")) {
            String[] items = line.split("\\s");
            StringBuilder buff = new StringBuilder();

            buff.append(ansi().a(INTENSITY_BOLD).a("[INFO] ---").reset());
            buff.append(" ");
            buff.append(ansi().fg(GREEN).a(items[2]).reset());
            buff.append(" ");
            buff.append(ansi().a(INTENSITY_BOLD).a(items[3]).reset());
            buff.append(" @ ");
            buff.append(ansi().fg(CYAN).a(items[5]).reset());
            buff.append(ansi().a(INTENSITY_BOLD).a(" ---").reset());
            buff.append(System.getProperty("line.separator"));

            line = buff.toString();
        }
        else if (line.startsWith("Downloading:")) {
            line = ansi().fg(CYAN).a(line).reset().toString();
        }
        else if (line.startsWith("Tests run:")) {
            if (line.contains("Errors: 0") && line.contains("Failures: 0")) {
                line = ansi().fg(GREEN).a(line).reset().toString();
            }
        }
        else if (line.contains("Errors: ") || line.contains("Failures: ")) {
            if (line.contains("Errors: 0") && line.contains("Failures: 0")) {
                line = ansi().fg(GREEN).a(line).reset().toString();
            }
            else {
                line = ansi().fg(RED).a(line).reset().toString();
            }
        }

        byte[] bytes = line.getBytes();
        super.write(bytes, 0, bytes.length);
    }
}