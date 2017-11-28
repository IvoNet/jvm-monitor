/*
 * Copyright 2017 Ivo Woltring <WebMaster@ivonet.nl>
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

package nl.ivonet.model;

import lombok.Value;

import javax.management.openmbean.CompositeData;

/**
 * @author Ivo Woltring
 */
@Value
public class Memory {

    private final long committed;
    private final long init;
    private final long max;
    private final long used;

    public Memory(final CompositeData data) {
        this.committed = (long) data.get("committed");
        this.init = (long) data.get("init");
        this.max = (long) data.get("max");
        this.used = (long) data.get("used");
    }
}
