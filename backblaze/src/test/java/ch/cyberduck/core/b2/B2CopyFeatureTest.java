package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class B2CopyFeatureTest extends AbstractB2Test {

    @Test
    public void testCopy() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(container, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        final Path copy = new B2CopyFeature(session, fileid).copy(test, new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback());
        assertNotEquals(test.attributes().getVersionId(), copy.attributes().getVersionId());
        assertTrue(new B2FindFeature(session, fileid).find(new Path(container, name, EnumSet.of(Path.Type.file))));
        assertTrue(new B2FindFeature(session, fileid).find(copy));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testCopyDifferentBucket() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path target = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)),
            null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(container, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        final Path copy = new B2CopyFeature(session, fileid).copy(test, new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new B2FindFeature(session, fileid).find(new Path(container, name, EnumSet.of(Path.Type.file))));
        assertTrue(new B2FindFeature(session, fileid).find(copy));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(test, copy, target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(folder, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new B2TouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(new Path(folder, name, EnumSet.of(Path.Type.file))));
        assertTrue(new B2FindFeature(session, fileid).find(copy));
        new B2CopyFeature(session, fileid).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
