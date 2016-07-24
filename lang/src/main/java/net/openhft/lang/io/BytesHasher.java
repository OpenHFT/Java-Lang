/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

public interface BytesHasher {
    /**
     * Provide a 64-bit hash for the bytes in Bytes between the bytes.position() and bytes.limit();
     *
     * @param bytes to hash
     * @return 64-bit hash
     */
    long hash(Bytes bytes);

    /**
     * Provide a 64-bit hash for the bytes between offset and limit
     *
     * @param bytes  to hash
     * @param offset the start inclusive
     * @param limit  the end exclusive
     * @return 64-bit hash.
     */
    long hash(Bytes bytes, long offset, long limit);
}
