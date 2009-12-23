/*
 * Sonatype Maven Shell (TM) Professional Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package org.sonatype.maven.shell.commands.maven;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.Attribute.*;

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
        buff.append((char)b);
        if (b == '\n') {
            writeBuffer();
        }
    }

    @Override
    public void write(final byte buf[], final int off, final int len) {
        for (int i=off; i < off + len; i++) {
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

        if (line.startsWith("ERROR") || line.contains("FAILURE")) {
            line = ansi().a(INTENSITY_BOLD).fg(RED).a(line).reset().toString();
        }
        else if (line.contains("SUCCESS") || line.contains(".OK (")) {
            line = ansi().a(INTENSITY_BOLD).fg(GREEN).a(line).reset().toString();
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

        byte[] bytes = line.getBytes();
        super.write(bytes, 0, bytes.length);
    }
}