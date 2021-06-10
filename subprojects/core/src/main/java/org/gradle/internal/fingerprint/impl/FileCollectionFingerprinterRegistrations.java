/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.internal.fingerprint.impl;

import org.gradle.api.internal.cache.StringInterner;
import org.gradle.internal.execution.fingerprint.FileCollectionFingerprinter;
import org.gradle.internal.execution.fingerprint.FileCollectionSnapshotter;
import org.gradle.internal.fingerprint.DirectorySensitivity;
import org.gradle.internal.fingerprint.LineEndingNormalization;
import org.gradle.internal.fingerprint.hashing.NormalizedContentHasher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class FileCollectionFingerprinterRegistrations {
    private final List<FileCollectionFingerprinter> registrants;

    public FileCollectionFingerprinterRegistrations(StringInterner stringInterner, FileCollectionSnapshotter fileCollectionSnapshotter, NormalizedContentHasher lineEndingNormalizationHasher) {
        this.registrants = stream(DirectorySensitivity.values())
            .flatMap(directorySensitivity ->
                stream(LineEndingNormalization.values()).flatMap(lineEndingNormalization ->
                    Stream.of(
                        new AbsolutePathFileCollectionFingerprinter(directorySensitivity, lineEndingNormalization, fileCollectionSnapshotter, lineEndingNormalizationHasher),
                        new RelativePathFileCollectionFingerprinter(stringInterner, directorySensitivity, lineEndingNormalization, fileCollectionSnapshotter, lineEndingNormalizationHasher),
                        new NameOnlyFileCollectionFingerprinter(directorySensitivity, lineEndingNormalization, fileCollectionSnapshotter, lineEndingNormalizationHasher)
                    )
                )
            ).collect(Collectors.toList());
        this.registrants.addAll(
            stream(LineEndingNormalization.values())
                .map(lineEndingNormalization -> new IgnoredPathFileCollectionFingerprinter(fileCollectionSnapshotter, lineEndingNormalization, lineEndingNormalizationHasher))
                .collect(Collectors.toList())
        );
    }

    public List<? extends FileCollectionFingerprinter> getRegistrants() {
        return registrants;
    }
}
