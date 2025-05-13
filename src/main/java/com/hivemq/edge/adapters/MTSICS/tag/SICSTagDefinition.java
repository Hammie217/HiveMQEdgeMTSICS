/*
 * Copyright 2023-present HiveMQ GmbH
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
package com.hivemq.edge.adapters.MTSICS.tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.adapter.sdk.api.annotations.ModuleConfigField;
import com.hivemq.adapter.sdk.api.tag.TagDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SICSTagDefinition implements TagDefinition {

    @JsonProperty(value = "command", required = true)
    @ModuleConfigField(
        title = "SICS Command",
        description = "The SICS command to send to the scale (e.g., 'SI' for weight, 'T' for tare).",
        required = true
    )
    private final @NotNull String command;

    @JsonCreator
    public SICSTagDefinition(@JsonProperty("command") final @NotNull String command) {
        this.command = command;
    }

    public @NotNull String getCommand() {
        return command;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SICSTagDefinition that = (SICSTagDefinition) o;
        return Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command);
    }

    @Override
    public String toString() {
        return "SICSTagDefinition{" + "command='" + command + '\'' + '}';
    }
}
