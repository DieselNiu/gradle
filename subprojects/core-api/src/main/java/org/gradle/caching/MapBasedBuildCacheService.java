/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.caching;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple build cache implementation that delegates to a {@link ConcurrentMap}.
 *
 * @since 3.5
 */
public class MapBasedBuildCacheService implements BuildCacheService {
    private final ConcurrentMap<String, byte[]> delegate;

    public MapBasedBuildCacheService(ConcurrentMap<String, byte[]> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean load(BuildCacheKey key, BuildCacheEntryReader reader) throws BuildCacheException {
        final byte[] bytes = delegate.get(key.getHashCode());
        if (bytes == null) {
            return false;
        }
        try (BuildCacheEntryFileReference fileReference = reader.openFileReference()) {
            Files.write(fileReference.getFile(), bytes);
        } catch (IOException e) {
            throw new BuildCacheException("loading " + key, e);
        }
        return true;
    }

    @Override
    public StoreOutcome maybeStore(BuildCacheKey key, BuildCacheEntryWriter writer) throws BuildCacheException {
        try (BuildCacheEntryFileReference fileReference = writer.openFileReference()) {
            delegate.put(key.getHashCode(), Files.readAllBytes(fileReference.getFile()));
        } catch (IOException e) {
            throw new BuildCacheException("storing " + key, e);
        }

        return StoreOutcome.STORED;
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }
}
