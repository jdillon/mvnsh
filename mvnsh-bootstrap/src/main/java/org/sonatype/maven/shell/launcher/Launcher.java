/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell.launcher;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sonatype.gshell.launcher.Configuration;
import org.sonatype.gshell.launcher.Log;

/**
 * Launches the Maven Shell. The primary difference of this custom launcher compared to the default GShell launcher is
 * the way the class loader is setup.
 */
public class Launcher {

    private final Configuration config = new Configuration();

    public static void main(final String[] args) {
        new Launcher().run(args);
    }

    public void run(final String[] args) {
        Log.debug("Running");

        try {
            config.configure();

            launch(args);

            Log.debug("Exiting");

            System.exit(config.getSuccessExitCode());
        } catch (Throwable t) {
            Log.debug("Failure: ", t);
            t.printStackTrace();
            System.err.flush();
            System.exit(config.getFailureExitCode());
        }
    }

    public void launch(final String[] args) throws Exception {
        assert args != null;

        Log.debug("Launching");

        ClassLoader cl = getClassLoader();
        Class<?> type = cl.loadClass(config.getMainClass());
        Method method = getMainMethod(type);

        Thread.currentThread().setContextClassLoader(cl);

        Log.debug("Invoking: ", method, ", with args: ", Arrays.asList(args));

        try {
            method.invoke(null, new Object[] { args });
        } catch (InvocationTargetException e) {
            Log.debug("Invoke failed", e);

            Throwable cause = e.getTargetException();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw e;
            }
        }
    }

    private ClassLoader getClassLoader() throws Exception {
        List<URL> shellClassPath = config.getClassPath();

        if (Log.DEBUG) {
            Log.debug("Shell Classpath:");
            for (URL url : shellClassPath) {
                Log.debug("    ", url);
            }
        }

        List<URL> mavenClassPath = getMavenClassPath();

        if (Log.DEBUG) {
            Log.debug("Maven Classpath:");
            for (URL url : mavenClassPath) {
                Log.debug("    ", url);
            }
        }

        ClassLoader mavenLoader = new URLClassLoader(mavenClassPath.toArray(new URL[mavenClassPath.size()]));

        ClassLoader shellLoader = new URLClassLoader(shellClassPath.toArray(new URL[mavenClassPath.size()]),
                mavenLoader);

        return shellLoader;
    }

    private List<URL> getMavenClassPath() throws Exception {
        List<URL> urls = new ArrayList<URL>();

        String shellHome = System.getProperty("shell.home", "");
        if (shellHome.length() <= 0) {
            throw new IllegalStateException("system property shell.home not set");
        }

        File homeDir = new File(shellHome).getAbsoluteFile();

        collectJars(urls, new File(homeDir, "boot"));
        collectJars(urls, new File(homeDir, "lib"));
        collectJars(urls, new File(homeDir, "lib/ext"));

        return urls;
    }

    private static void collectJars(Collection<URL> urls, File directory) throws Exception {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar")
                        && !pathname.getName().equals(Configuration.BOOTSTRAP_JAR);
            }
        });

        if (files != null) {
            for (File file : files) {
                urls.add(file.toURI().toURL());
            }
        }
    }

    private Method getMainMethod(final Class<?> type) throws Exception {
        assert type != null;

        Method method = type.getMethod("main", String[].class);
        int modifiers = method.getModifiers();

        if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && method.getReturnType() == Void.TYPE) {
            return method;
        }

        throw new NoSuchMethodException("public static void main(String[] args) in " + type);
    }

}
