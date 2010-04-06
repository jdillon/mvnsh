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

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

/**
 * {@link BasicClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class BasicClientImpl
    extends NexusClientExtensionSupport
    implements BasicClient
{
    @Inject
    public BasicClientImpl(final NexusClient client) {
        super(client);
    }

    public StatusResource status() {
        ensureOpened();

        URI uri = baseUri().path("status").build();
        String content = client.get(uri);
        return unmarshal(StatusResourceResponse.class, content).getData();
    }

    public SearchResponse search(final MultivaluedMap<String,String> params) {
        assert params != null;

        ensureOpened();

        URI uri = baseUri().path("data_index").build();
        String content = client.get(uri, params);
        return unmarshal(SearchResponse.class, content);
    }
}