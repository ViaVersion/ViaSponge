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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

public class SpongeEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;

    public SpongeEncodeHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf byteBuf, final List<Object> out) {
        if (!info.checkClientboundPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!info.shouldTransformPacket()) {
            out.add(byteBuf.retain());
            return;
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(byteBuf);
        try {
            info.transformClientbound(transformedBuf, CancelEncoderException::generate);

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelCodecException) return;
        super.exceptionCaught(ctx, cause);
    }
}
