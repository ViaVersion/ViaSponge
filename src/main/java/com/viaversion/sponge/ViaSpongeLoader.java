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

import com.viaversion.sponge.commands.SpongeCommandHandler;
import com.viaversion.sponge.commands.SpongePlayer;
import com.viaversion.sponge.util.LoggerWrapper;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.sponge.platform.SpongeViaAPI;
import com.viaversion.sponge.platform.SpongeViaConfig;
import com.viaversion.sponge.platform.SpongeViaInjector;
import com.viaversion.sponge.platform.SpongeViaLoader;
import com.viaversion.sponge.platform.SpongeViaTask;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.VersionInfo;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginContributor;

public final class ViaSpongeLoader implements ViaPlatform<Player> {

    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().extractUrls().build();
    private final SpongeViaAPI api = new SpongeViaAPI();
    private final PluginContainer container;
    private final Game game;
    private final Path configDir;
    @SuppressWarnings("SpongeLogging")
    private final Logger logger;
    private final SpongeViaConfig conf;

    public ViaSpongeLoader(final PluginContainer container, final Game game, final org.apache.logging.log4j.Logger logger, final Path configDir) {
        this.container = container;
        this.game = game;
        this.logger = new LoggerWrapper(logger);
        this.configDir = configDir;

        // Setup Plugin
        conf = new SpongeViaConfig(configDir.toFile(), this.logger);

        // Init platform
        Via.init(ViaManagerImpl.builder()
            .platform(this)
            .commandHandler(new SpongeCommandHandler())
            .injector(new SpongeViaInjector())
            .loader(new SpongeViaLoader(this))
            .build());
        conf.reload();

        if (hasClass("com.viaversion.viabackwards.api.ViaBackwardsPlatform")) {
            getLogger().info("Found ViaBackwards, loading it");
            Via.getManager().addEnableListener(() -> new ViaBackwardsLoader(getLogger(), getDataFolder()));
        }
        if (hasClass("com.viaversion.viarewind.api.ViaRewindPlatform")) {
            getLogger().info("Found ViaRewind, loading it");
            Via.getManager().addEnableListener(() -> new ViaRewindLoader(getLogger(), getDataFolder()));
        }
    }

    private boolean hasClass(final String name) {
        try {
            Class.forName(name);
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    public void onServerStart() {
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        manager.init();
    }

    public void onCommandRegister(final RegisterCommandEvent<Command.Raw> event) {
        event.register(container, (Command.Raw) Via.getManager().getCommandHandler(), "viaversion", "viaver", "vvsponge");
    }

    public void onServerStarted() {
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        manager.onServerLoaded();
    }

    public void onServerStop() {
        ((ViaManagerImpl) Via.getManager()).destroy();
    }

    @Override
    public String getPlatformName() {
        return game.platform().container(Platform.Component.IMPLEMENTATION).metadata().name().orElse("unknown");
    }

    @Override
    public String getPlatformVersion() {
        return game.platform().container(Platform.Component.IMPLEMENTATION).metadata().version().toString();
    }

    @Override
    public String getPluginVersion() {
        // Use the implementation version
        return VersionInfo.getVersion();
    }

    @Override
    public PlatformTask runAsync(final Runnable runnable) {
        final Task task = Task.builder().plugin(container).execute(runnable).build();
        return new SpongeViaTask(game.asyncScheduler().submit(task));
    }

    @Override
    public PlatformTask runRepeatingAsync(final Runnable runnable, final long ticks) {
        final Task task = Task.builder().plugin(container).execute(runnable).interval(Ticks.of(ticks)).build();
        return new SpongeViaTask(game.asyncScheduler().submit(task));
    }

    @Override
    public PlatformTask runSync(final Runnable runnable) {
        final Task task = Task.builder().plugin(container).execute(runnable).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public PlatformTask runSync(final Runnable runnable, final long delay) {
        final Task task = Task.builder().plugin(container).execute(runnable).delay(Ticks.of(delay)).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public PlatformTask runRepeatingSync(final Runnable runnable, final long period) {
        final Task task = Task.builder().plugin(container).execute(runnable).interval(Ticks.of(period)).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        final Collection<ServerPlayer> players = game.server().onlinePlayers();
        final ViaCommandSender[] array = new ViaCommandSender[players.size()];
        int i = 0;
        for (final ServerPlayer player : players) {
            array[i++] = new SpongePlayer(player);
        }
        return array;
    }

    @Override
    public void sendMessage(final UUID uuid, final String message) {
        game.server().player(uuid).ifPresent(player -> player.sendMessage(LEGACY_SERIALIZER.deserialize(message)));
    }

    @Override
    public boolean kickPlayer(final UUID uuid, final String message) {
        return game.server().player(uuid).map(player -> {
            player.kick(LegacyComponentSerializer.legacySection().deserialize(message));
            return true;
        }).orElse(false);
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public File getDataFolder() {
        return configDir.toFile();
    }

    @Override
    public void onReload() {
        logger.severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
    }

    @Override
    public JsonObject getDump() {
        final JsonObject platformSpecific = new JsonObject();

        final List<PluginInfo> plugins = new ArrayList<>();
        for (final PluginContainer plugin : game.pluginManager().plugins()) {
            final PluginMetadata metadata = plugin.metadata();
            plugins.add(new PluginInfo(
                true,
                metadata.name().orElse("Unknown"),
                metadata.version().toString(),
                plugin.instance() != null ? plugin.instance().getClass().getCanonicalName() : "Unknown",
                metadata.contributors().stream().map(PluginContributor::name).collect(Collectors.toList())
            ));
        }
        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public boolean hasPlugin(final String name) {
        return game.pluginManager().plugin(name).isPresent();
    }

    @Override
    public SpongeViaAPI getApi() {
        return api;
    }

    @Override
    public SpongeViaConfig getConf() {
        return conf;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public PluginContainer container() {
        return container;
    }
}
