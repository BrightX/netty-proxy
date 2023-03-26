package com.bright.proxy;

import com.bright.proxy.http.HttpServerHeadDecoder;
import com.bright.proxy.socks.SocksServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixinSelectHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(MixinSelectHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        final int readerIndex = msg.readerIndex();

        ChannelPipeline p = ctx.pipeline();
        final byte versionVal = msg.getByte(readerIndex);

        SocksVersion version = SocksVersion.valueOf(versionVal);
        if (version.equals(SocksVersion.SOCKS4a) || version.equals(SocksVersion.SOCKS5)) {
            //socks proxy
            logger.debug("proxyType = " + version);
            p.addLast(new SocksPortUnificationServerHandler(),
                    SocksServerHandler.INSTANCE).remove(this);
        } else {
            //http/tunnel proxy
            logger.debug("proxyType = HTTP/TUNNEL");
            p.addLast(new HttpServerHeadDecoder()).remove(this);
        }
        msg.retain();
        ctx.fireChannelRead(msg);
    }
}
