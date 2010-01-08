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

package org.sonatype.maven.shell.maven.internal;

import jline.Terminal;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.BuildSuccess;
import org.apache.maven.execution.BuildSummary;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Logs execution events to a user-supplied logger.  Checks for thread interruption.
 *
 * @author Benjamin Bentmann
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class ExecutionEventLogger
    extends AbstractExecutionListener
{
    private final Terminal term;
    
    private final Logger logger;

    public ExecutionEventLogger(final Terminal term, final Logger logger) {
        assert term != null;
        this.term = term;
        assert logger != null;
        this.logger = logger;
    }

    private static class InterruptedRuntimeException
        extends RuntimeException
    {
        // empty
    }

    private void ensureThreadNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedRuntimeException();
        }
    }

    private int lineLength() {
        return term.getWidth() - 10;
    }
    
    private static String chars(char c, int count) {
        StringBuilder buffer = new StringBuilder(count);

        for (int i = count; i > 0; i--) {
            buffer.append(c);
        }

        return buffer.toString();
    }

    private static String getFormattedTime(long time) {
        String pattern = "s.SSS's'";

        if (time / 60000L > 0) {
            pattern = "m:s" + pattern;

            if (time / 3600000L > 0) {
                pattern = "H:m" + pattern;
            }
        }

        DateFormat fmt = new SimpleDateFormat(pattern);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        return fmt.format(new Date(time));
    }

    @Override
    public void projectDiscoveryStarted(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info("Scanning for projects...");
        }
    }

    @Override
    public void sessionStarted(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled() && event.getSession().getProjects().size() > 1) {
            logger.info(chars('-', lineLength()));

            logger.info("Reactor Build Order:");

            logger.info("");

            for (MavenProject project : event.getSession().getProjects()) {
                logger.info(project.getName());
            }
        }
    }

    @Override
    public void sessionEnded(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            if (event.getSession().getProjects().size() > 1) {
                logReactorSummary(event.getSession());
            }

            logResult(event.getSession());

            logStats(event.getSession());

            logger.info(chars('-', lineLength()));
        }
    }

    private void logReactorSummary(MavenSession session) {
        logger.info(chars('-', lineLength()));

        logger.info("Reactor Summary:");

        logger.info("");

        MavenExecutionResult result = session.getResult();

        for (MavenProject project : session.getProjects()) {
            StringBuilder buffer = new StringBuilder(128);

            buffer.append(project.getName());

            buffer.append(' ');
            while (buffer.length() < lineLength() - 21) {
                buffer.append('.');
            }
            buffer.append(' ');

            BuildSummary buildSummary = result.getBuildSummary(project);

            if (buildSummary == null) {
                buffer.append("SKIPPED");
            }
            else if (buildSummary instanceof BuildSuccess) {
                buffer.append("SUCCESS [");
                buffer.append(getFormattedTime(buildSummary.getTime()));
                buffer.append("]");
            }
            else if (buildSummary instanceof BuildFailure) {
                buffer.append("FAILURE [");
                buffer.append(getFormattedTime(buildSummary.getTime()));
                buffer.append("]");
            }

            logger.info(buffer.toString());
        }
    }

    private void logResult(MavenSession session) {
        logger.info(chars('-', lineLength()));

        if (session.getResult().hasExceptions()) {
            logger.info("BUILD FAILURE");
        }
        else {
            logger.info("BUILD SUCCESS");
        }
    }

    private void logStats(MavenSession session) {
        logger.info(chars('-', lineLength()));

        Date finish = new Date();

        long time = finish.getTime() - session.getRequest().getStartTime().getTime();

        logger.info("Total time: " + getFormattedTime(time));

        logger.info("Finished at: " + finish);

        System.gc();

        Runtime r = Runtime.getRuntime();

        long MB = 1024 * 1024;

        logger.info("Final Memory: " + (r.totalMemory() - r.freeMemory()) / MB + "M/" + r.totalMemory() / MB + "M");
    }

    @Override
    public void projectSkipped(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info(chars(' ', lineLength()));
            logger.info(chars('-', lineLength()));

            logger.info("Skipping " + event.getProject().getName());
            logger.info("This project has been banned from the build due to previous failures.");

            logger.info(chars('-', lineLength()));
        }
    }

    @Override
    public void projectStarted(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info(chars(' ', lineLength()));
            logger.info(chars('-', lineLength()));

            logger.info("Building " + event.getProject().getName() + " " + event.getProject().getVersion());

            logger.info(chars('-', lineLength()));
        }
    }

    @Override
    public void mojoSkipped(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isWarnEnabled()) {
            logger.warn("Goal " + event.getMojoExecution().getGoal()
                + " requires online mode for execution but Maven is currently offline, skipping");
        }
    }

    @Override
    public void mojoStarted(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            MojoExecution me = event.getMojoExecution();
            StringBuilder buffer = new StringBuilder(128);

            buffer.append("--- ");
            buffer.append(me.getArtifactId()).append(':').append(me.getVersion());
            buffer.append(':').append(me.getGoal());
            if (me.getExecutionId() != null) {
                buffer.append(" (").append(me.getExecutionId()).append(')');
            }
            buffer.append(" @ ").append(event.getProject().getArtifactId());
            buffer.append(" ---");

            logger.info("");
            logger.info(buffer.toString());
        }
    }

    @Override
    public void forkStarted(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isDebugEnabled()) {
            logger.debug("Forking execution for " + event.getMojoExecution().getMojoDescriptor().getId());
        }
    }

    @Override
    public void forkSucceeded(ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isDebugEnabled()) {
            logger.debug("Completed forked execution for " + event.getMojoExecution().getMojoDescriptor().getId());
        }
    }

}