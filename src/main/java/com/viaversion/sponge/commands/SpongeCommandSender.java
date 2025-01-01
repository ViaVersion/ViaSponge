/*
 * This file is part of ViaSponge - https://github.com/ViaVersion/ViaSponge
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.sponge.commands;

import com.viaversion.sponge.ViaSpongeLoader;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import java.util.UUID;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.util.Identifiable;

public class SpongeCommandSender implements ViaCommandSender {
    private final CommandCause source;

    public SpongeCommandSender(CommandCause source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        source.sendMessage(Identity.nil(), ViaSpongeLoader.LEGACY_SERIALIZER.deserialize(msg));
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Identifiable identifiable) {
            return identifiable.uniqueId();
        } else {
            return new UUID(0, 0);
        }

    }

    @Override
    public String getName() {
        return source.friendlyIdentifier().orElse(source.identifier());
    }
}
