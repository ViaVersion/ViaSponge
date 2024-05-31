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

import com.google.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import net.lenni0451.reflect.Methods;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("viaversion")
public class ViaSpongePlugin {

    private final PluginContainer container;
    private final Game game;
    private final Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    private Object platform; // Classloading says HMMMM

    @Inject
    ViaSpongePlugin(final PluginContainer container, final Game game, final Logger logger) {
        this.container = container;
        this.game = game;
        this.logger = logger;
    }

    @Listener
    public void constructPlugin(final ConstructPluginEvent event) {
        try {
            loadImplementation();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        platform = new ViaSpongeLoader(container, game, logger, configDir);
    }

    @Listener
    public void onServerStart(final StartingEngineEvent<Server> event) {
        ((ViaSpongeLoader) platform).onServerStart();
    }

    @Listener
    public void onCommandRegister(final RegisterCommandEvent<Command.Raw> event) {
        ((ViaSpongeLoader) platform).onCommandRegister(event);
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        ((ViaSpongeLoader) platform).onServerStarted();
    }

    @Listener
    public void onServerStop(final StoppingEngineEvent<Server> event) {
        ((ViaSpongeLoader) platform).onServerStop();
    }

    private void loadImplementation() throws Exception {
        final File[] files = configDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("You need to place the main ViaVersion jar in config/viaversion/");
        }

        // Cursedness to get to the actual classloader
        final ClassLoader classLoader = ViaSpongePlugin.class.getClassLoader();
        final Field delegatedClassLoaderField = classLoader.getClass().getDeclaredField("delegatedClassLoader");
        delegatedClassLoaderField.setAccessible(true);
        final URLClassLoader urlClassLoader = (URLClassLoader) delegatedClassLoaderField.get(classLoader);
        final Method addURL = Methods.getDeclaredMethod(URLClassLoader.class, "addURL", URL.class);

        boolean found = false;
        for (final File file : files) {
            if (file.getName().endsWith(".jar")) {
                Methods.invoke(urlClassLoader, addURL, file.toURI().toURL());
                found = true;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("You need to place the main ViaVersion jar in config/viaversion/");
        }
    }
}
