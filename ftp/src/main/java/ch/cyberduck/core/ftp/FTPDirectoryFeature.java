package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

public class FTPDirectoryFeature implements Directory<Integer> {

    private final FTPSession session;

    public FTPDirectoryFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().makeDirectory(folder.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            return folder.withAttributes(new FTPAttributesFinderFeature(session).find(folder));
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return true;
    }

    @Override
    public FTPDirectoryFeature withWriter(final Write writer) {
        return this;
    }
}
