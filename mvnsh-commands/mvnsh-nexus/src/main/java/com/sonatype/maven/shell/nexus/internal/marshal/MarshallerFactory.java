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
package com.sonatype.maven.shell.nexus.internal.marshal;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sonatype.gshell.util.marshal.Marshaller;

import java.util.ArrayList;
import java.util.List;

/**
 * Support for {@link MarshallerFactory} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public interface MarshallerFactory
{
    <T> Marshaller<T> create(final Class<T> type);

    class AliasingListConverter
        implements Converter
    {
        private Class<?> type;

        private String alias;

        public AliasingListConverter(Class<?> type, String alias) {
            this.type = type;
            this.alias = alias;
        }

        @SuppressWarnings("unchecked")
        public boolean canConvert(Class type) {
            return List.class.isAssignableFrom(type);
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            List<?> list = (List<?>) source;
            for (Object elem : list) {
                if (!elem.getClass().isAssignableFrom(type)) {
                    throw new ConversionException("Found " + elem.getClass() + ", expected to find: " + this.type + " in List.");
                }

                ExtendedHierarchicalStreamWriterHelper.startNode(writer, alias, elem.getClass());
                context.convertAnother(elem);
                writer.endNode();
            }
        }

        @SuppressWarnings("unchecked")
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            List list = new ArrayList();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                list.add(context.convertAnother(list, type));
                reader.moveUp();
            }
            return list;
        }
    }
}