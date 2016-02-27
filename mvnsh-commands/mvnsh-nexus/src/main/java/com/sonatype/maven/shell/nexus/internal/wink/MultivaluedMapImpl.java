/*******************************************************************************
 * Copyright (c) 2009-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at 
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses. 
 *******************************************************************************/

package com.sonatype.maven.shell.nexus.internal.wink;

import java.util.Map;

/**
 * {@link javax.ws.rs.core.MultivaluedMap} implementation.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MultivaluedMapImpl<K,V>
    extends org.apache.wink.common.internal.MultivaluedMapImpl<K,V>
{
    public MultivaluedMapImpl() {
        super();
    }

    public MultivaluedMapImpl(final Map<K,V> map) {
        super(map);
    }
}