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

package org.sonatype.maven.shell.maven.internal;

import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Console download progress meter.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @since 0.9
 */
public class ConsoleMavenTransferListener
    extends AbstractMavenTransferListener
{

    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

    private int lastLength;

    public ConsoleMavenTransferListener( PrintStream out )
    {
        super( out );
    }

    @Override
    public void transferProgressed( TransferEvent event )
        throws TransferCancelledException
    {
        TransferResource resource = event.getResource();
        downloads.put( resource, Long.valueOf( event.getTransferredBytes() ) );

        StringBuilder buffer = new StringBuilder( 64 );

        for ( Map.Entry<TransferResource, Long> entry : downloads.entrySet() )
        {
            long total = entry.getKey().getContentLength();
            long complete = entry.getValue().longValue();

            buffer.append( getStatus( complete, total ) ).append( "  " );
        }

        int pad = lastLength - buffer.length();
        lastLength = buffer.length();
        pad( buffer, pad );
        buffer.append( '\r' );

        out.print( buffer );
    }

    private String getStatus( long complete, long total )
    {
        if ( total >= 1024 )
        {
            return toKB( complete ) + "/" + toKB( total ) + " KB ";
        }
        else if ( total >= 0 )
        {
            return complete + "/" + total + " B ";
        }
        else if ( complete >= 1024 )
        {
            return toKB( complete ) + " KB ";
        }
        else
        {
            return complete + " B ";
        }
    }

    private void pad( StringBuilder buffer, int spaces )
    {
        String block = "                                        ";
        while ( spaces > 0 )
        {
            int n = Math.min( spaces, block.length() );
            buffer.append( block, 0, n );
            spaces -= n;
        }
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        transferCompleted( event );

        super.transferSucceeded( event );
    }

    @Override
    public void transferFailed( TransferEvent event )
    {
        transferCompleted( event );

        super.transferFailed( event );
    }

    private void transferCompleted( TransferEvent event )
    {
        downloads.remove( event.getResource() );

        StringBuilder buffer = new StringBuilder( 64 );
        pad( buffer, lastLength );
        buffer.append( '\r' );
        out.print( buffer );
    }

}
