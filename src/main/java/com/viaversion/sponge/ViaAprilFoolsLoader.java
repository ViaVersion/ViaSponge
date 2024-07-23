/*
 * This file is part of ViaSponge - https://github.com/ViaVersion/ViaSponge
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

package com.viaversion.sponge;

import net.raphimc.viaaprilfools.platform.ViaAprilFoolsPlatform;
import java.io.File;
import java.util.logging.Logger;

public record ViaAprilFoolsLoader(Logger logger, File dataFolder) implements ViaAprilFoolsPlatform {

    public ViaAprilFoolsLoader(final Logger logger, final File dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.init(new File(this.dataFolder, "config.yml"));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}