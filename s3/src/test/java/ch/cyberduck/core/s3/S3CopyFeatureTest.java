package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
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
public class S3CopyFeatureTest extends AbstractS3Test {

    @Test
    public void testCopyFileZeroLength() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);
        new S3TouchFeature(session).touch(test, new TransferStatus().withMime("application/cyberduck"));
        final Path copy = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3CopyFeature(session, new S3AccessControlListFeature(session)).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session).find(copy));
        assertEquals("application/cyberduck",
            new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(copy).get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFile() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setMetadata(Collections.singletonMap("cyberduck", "m"));
        final Path test = new S3TouchFeature(session).touch(new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final Path copy = new S3CopyFeature(session, new S3AccessControlListFeature(session)).copy(test,
            new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals("m", new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(copy).get("cyberduck"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session).find(copy));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
