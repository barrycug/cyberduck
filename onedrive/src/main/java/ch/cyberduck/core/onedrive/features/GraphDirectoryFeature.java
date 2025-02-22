package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;

public class GraphDirectoryFeature implements Directory<Void> {

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;
    private final GraphFileIdProvider fileid;

    public GraphDirectoryFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session);
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        final DriveItem folder = session.getItem(directory.getParent());
        try {
            final DriveItem.Metadata metadata = Files.createFolder(folder, directory.getName());
            final PathAttributes attr = attributes.toAttributes(metadata);
            fileid.cache(directory, attr.getFileId());
            return directory.withAttributes(attr);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Cannot create folder {0}", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, directory);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return session.isAccessible(workdir);
    }

    @Override
    public Directory<Void> withWriter(final Write writer) {
        return this;
    }
}
