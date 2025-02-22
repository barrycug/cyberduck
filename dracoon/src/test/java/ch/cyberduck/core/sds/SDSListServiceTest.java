package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SDSListServiceTest extends AbstractSDSTest {

    @Test
    public void testList() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        assertTrue(new SDSListService(session, nodeid).list(room, new DisabledListProgressListener()).isEmpty());
        new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(1, (new SDSListService(session, nodeid).list(room, new DisabledListProgressListener(), 1).size()));
        new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(2, (new SDSListService(session, nodeid).list(room, new DisabledListProgressListener(), 1).size()));
        assertEquals(2, (new SDSListService(session, nodeid).list(room, new DisabledListProgressListener()).size()));
        new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(3, (new SDSListService(session, nodeid).list(room, new DisabledListProgressListener(), 1).size()));
        assertEquals(3, (new SDSListService(session, nodeid).list(room, new DisabledListProgressListener()).size()));
        new SDSDeleteFeature(session, nodeid).delete(Collections.<Path>singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListAlphanumeric() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        assertTrue(new SDSListService(session, nodeid).list(room, new DisabledListProgressListener()).isEmpty());
        new SDSTouchFeature(session, nodeid).touch(new Path(room, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        new SDSTouchFeature(session, nodeid).touch(new Path(room, "0a", EnumSet.of(Path.Type.file)), new TransferStatus());
        new SDSTouchFeature(session, nodeid).touch(new Path(room, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new SDSListService(session, nodeid).list(room, new DisabledListProgressListener());
        assertEquals(3, list.size());
        assertEquals("0a", list.get(0).getName());
        assertEquals("a", list.get(1).getName());
        assertEquals("aa", list.get(2).getName());
        new SDSDeleteFeature(session, nodeid).delete(Collections.<Path>singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
