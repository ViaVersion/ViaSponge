/*
 * This file is part of ViaSponge - https://github.com/ViaVersion/ViaSponge
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaDecodeHandler;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public final class SpongeChannelInitializer extends ViaChannelInitializer {

    public static final String DECODER = "decoder";
    public static final String ENCODER = "encoder";
    public static final String OUTBOUND_CONFIG = "outbound_config";

    public static final String COMPRESS = "compress";
    public static final String DECOMPRESS = "decompress";

    public SpongeChannelInitializer(final ChannelInitializer<Channel> original) {
        super(original, false);
    }

    @Override
    protected void injectPipeline(final ChannelPipeline pipeline, final UserConnection connection) {
        final Channel channel = pipeline.channel();
        if (Via.getAPI().getServerVersion().isKnown() && channel instanceof SocketChannel) { // channel can be LocalChannel on internal server
            UserConnection info = new UserConnectionImpl(channel);
            new ProtocolPipelineImpl(info);

            MessageToMessageEncoder<ByteBuf> encoder = new SpongeEncodeHandler(info);
            MessageToMessageDecoder<ByteBuf> decoder = new ViaDecodeHandler(info);

            final ViaInjector injector = Via.getManager().getInjector();

            final String encoderName = pipeline.get(OUTBOUND_CONFIG) != null ? OUTBOUND_CONFIG : ENCODER;
            channel.pipeline().addBefore(encoderName, injector.getEncoderName(), encoder);
            channel.pipeline().addBefore(DECODER, injector.getDecoderName(), decoder);
        }
    }
}
