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
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;

public class GraphTouchFeature implements Touch<Void> {

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;
    private final GraphFileIdProvider fileid;

    public GraphTouchFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session);
        this.fileid = fileid;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final DriveItem folder = session.getItem(file.getParent());
            final DriveItem.Metadata metadata = Files.createFile(folder, URIEncoder.encode(file.getName()),
                StringUtils.isNotBlank(status.getMime()) ? status.getMime() : MimeTypeService.DEFAULT_CONTENT_TYPE);
            final PathAttributes attr = attributes.toAttributes(metadata);
            fileid.cache(file, attr.getFileId());
            return file.withAttributes(attr);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Cannot create {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return session.isAccessible(workdir);
    }

    @Override
    public Touch<Void> withWriter(final Write writer) {
        return this;
    }
}
