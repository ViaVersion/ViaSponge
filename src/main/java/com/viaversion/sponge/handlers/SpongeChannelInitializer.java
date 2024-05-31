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
package com.viaversion.sponge.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.lang.reflect.Method;

public class SpongeChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {

    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public SpongeChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Ensure ViaVersion is loaded
        if (Via.getAPI().getServerVersion().isKnown()
            && channel instanceof SocketChannel) { // channel can be LocalChannel on internal server
            UserConnection info = new UserConnectionImpl(channel);
            // init protocol
            new ProtocolPipelineImpl(info);
            // Add originals
            INIT_CHANNEL_METHOD.invoke(this.original, channel);
            // Get the pipeline
            final ChannelPipeline pipeline = channel.pipeline();
            // Get the encoder name
            final String encoderName = pipeline.get("outbound_config") != null ? "outbound_config" : "encoder";
            // Add our transformers
            MessageToMessageEncoder<ByteBuf> encoder = new SpongeEncodeHandler(info);
            MessageToMessageDecoder<ByteBuf> decoder = new SpongeDecodeHandler(info);

            channel.pipeline().addBefore(encoderName, "via-encoder", encoder);
            channel.pipeline().addBefore("decoder", "via-decoder", decoder);
        } else {
            INIT_CHANNEL_METHOD.invoke(this.original, channel);
        }
    }

    @Override
    public ChannelInitializer<Channel> original() {
        return original;
    }
}
