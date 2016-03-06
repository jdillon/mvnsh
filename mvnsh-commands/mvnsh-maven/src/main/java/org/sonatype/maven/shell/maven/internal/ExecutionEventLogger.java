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
import org.slf4j.Logger;

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

    private final int lineLength;

    public ExecutionEventLogger(final Terminal term, final Logger logger) {
        assert term != null;
        this.term = term;
        assert logger != null;
        this.logger = logger;

        // Cache this, its expensive on Unix
        this.lineLength = lineLength();
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
        int len = term.getWidth() - 8;
        if (len < 0) {
            len = 79;
        }
        return len;
    }
    
    private static String chars(final char c, final int count) {
        StringBuilder buff = new StringBuilder(count);

        for (int i = count; i > 0; i--) {
            buff.append(c);
        }

        return buff.toString();
    }

    private static String getFormattedTime(final long time) {
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
    public void projectDiscoveryStarted(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info("Scanning for projects..."); // TODO: i18n
        }
    }

    @Override
    public void sessionStarted(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled() && event.getSession().getProjects().size() > 1) {
            logger.info(chars('-', lineLength));
            logger.info("Reactor Build Order:"); // TODO: i18n
            logger.info("");

            for (MavenProject project : event.getSession().getProjects()) {
                logger.info(project.getName());
            }
        }
    }

    @Override
    public void sessionEnded(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            if (event.getSession().getProjects().size() > 1) {
                logReactorSummary(event.getSession());
            }

            logResult(event.getSession());
            logStats(event.getSession());

            logger.info(chars('-', lineLength));
        }
    }

    private void logReactorSummary(final MavenSession session) {
        logger.info(chars('-', lineLength));
        logger.info("Reactor Summary:"); // TODO: i18n
        logger.info("");

        MavenExecutionResult result = session.getResult();

        for (MavenProject project : session.getProjects()) {
            StringBuilder buff = new StringBuilder(128);

            buff.append(project.getName());

            buff.append(' ');
            while (buff.length() < lineLength - 21) {
                buff.append('.');
            }
            buff.append(' ');

            BuildSummary buildSummary = result.getBuildSummary(project);

            if (buildSummary == null) {
                buff.append("SKIPPED"); // TODO: i18n
            }
            else if (buildSummary instanceof BuildSuccess) {
                buff.append("SUCCESS ["); // TODO: i18n
                buff.append(getFormattedTime(buildSummary.getTime()));
                buff.append("]");
            }
            else if (buildSummary instanceof BuildFailure) {
                buff.append("FAILURE ["); // TODO: i18n
                buff.append(getFormattedTime(buildSummary.getTime()));
                buff.append("]");
            }

            logger.info(buff.toString());
        }

        logger.info("");
    }

    private void logResult(final MavenSession session) {
        logger.info(chars('-', lineLength));

        if (session.getResult().hasExceptions()) {
            logger.info("BUILD FAILURE"); // TODO: i18n
        }
        else {
            logger.info("BUILD SUCCESS"); // TODO: i18n
        }
    }

    private void logStats(final MavenSession session) {
        logger.info(chars('-', lineLength));

        Date finish = new Date();

        long time = finish.getTime() - session.getRequest().getStartTime().getTime();

        logger.info("Total time: " + getFormattedTime(time)); // TODO: i18n
        logger.info("Finished at: " + finish); // TODO: i18n

        System.gc();

        Runtime r = Runtime.getRuntime();
        long MB = 1024 * 1024;

        logger.info("Final Memory: " + (r.totalMemory() - r.freeMemory()) / MB + "M/" + r.totalMemory() / MB + "M"); // TODO: i18n
    }

    @Override
    public void projectSkipped(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info(chars(' ', lineLength));
            logger.info(chars('-', lineLength));

            logger.info("Skipping " + event.getProject().getName()); // TODO: i18n
            logger.info("This project has been banned from the build due to previous failures."); // TODO: i18n

            logger.info(chars('-', lineLength));
        }
    }

    @Override
    public void projectStarted(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            logger.info(chars(' ', lineLength));
            logger.info(chars('-', lineLength));

            logger.info("Building " + event.getProject().getName() + " " + event.getProject().getVersion()); // TODO: i18n

            logger.info(chars('-', lineLength));
        }
    }

    @Override
    public void mojoSkipped(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isWarnEnabled()) {
            logger.warn("Goal " + event.getMojoExecution().getGoal()
                + " requires online mode for execution but Maven is currently offline, skipping"); // TODO: i18n
        }
    }

    @Override
    public void mojoStarted(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isInfoEnabled()) {
            MojoExecution me = event.getMojoExecution();
            StringBuilder buff = new StringBuilder(128);

            buff.append("--- ");
            buff.append(me.getArtifactId()).append(':').append(me.getVersion());
            buff.append(':').append(me.getGoal());
            if (me.getExecutionId() != null) {
                buff.append(" (").append(me.getExecutionId()).append(')');
            }
            buff.append(" @ ").append(event.getProject().getArtifactId());
            buff.append(" ---");

            logger.info("");
            logger.info(buff.toString());
        }
    }

    @Override
    public void forkStarted(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isDebugEnabled()) {
            logger.debug("Forking execution for " + event.getMojoExecution().getMojoDescriptor().getId()); // TODO: i18n
        }
    }

    @Override
    public void forkSucceeded(final ExecutionEvent event) {
        ensureThreadNotInterrupted();

        if (logger.isDebugEnabled()) {
            logger.debug("Completed forked execution for " + event.getMojoExecution().getMojoDescriptor().getId()); // TODO: i18n
        }
    }

}