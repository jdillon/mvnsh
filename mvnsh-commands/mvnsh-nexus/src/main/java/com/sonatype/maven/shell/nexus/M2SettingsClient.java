/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus;

/**
 * M2Settings client extension.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 *
 * @see <a href="https://docs.sonatype.com/display/Nexus/Maven+Settings+Templates+API">Maven Settings Templates API</a>
 */
public interface M2SettingsClient
    extends NexusClient.Extension
{
    String fetch(String templateId);

    // TODO: list,create,update,delete

    // TODO: fetch (object) probably need to rename above to fetchContent
}