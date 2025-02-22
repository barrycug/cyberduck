package ch.cyberduck.core.googlestorage;

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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageWriteFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testWrite() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus();
            final byte[] content = RandomUtils.nextBytes(2048);
            status.setLength(content.length);
            status.setMime("application/octet-stream");
            status.setStorageClass("multi_regional");
            status.setMetadata(Collections.singletonMap("c", "d"));
            final HttpResponseOutputStream<VersionId> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertNotNull(out.getStatus().id);
            assertTrue(new GoogleStorageFindFeature(session).find(test));
            final Write.Append append = new GoogleStorageWriteFeature(session).append(test, status.withRemote(new GoogleStorageAttributesFinderFeature(session).find(test)));
            assertFalse(append.append);
            assertEquals(content.length, append.size, 0L);
            final byte[] buffer = new byte[content.length];
            final InputStream in = new GoogleStorageReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
        }
        // Overwrite
        {
            final TransferStatus status = new TransferStatus();
            status.setExists(true);
            final byte[] content = RandomUtils.nextBytes(1024);
            status.setLength(content.length);
            final HttpResponseOutputStream<VersionId> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertNotNull(out.getStatus().id);
            final PathAttributes attributes = new GoogleStorageAttributesFinderFeature(session).find(test);
            assertEquals(content.length, attributes.getSize());
        }
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
