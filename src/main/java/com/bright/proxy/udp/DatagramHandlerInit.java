package com.bright.proxy.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Bright Xu
 */
public class DatagramHandlerInit extends ChannelInitializer<NioDatagramChannel> {
    @Override
    protected void initChannel(NioDatagramChannel ch) {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast("decoder", new SocksDatagramDecoderHandler());
        ch.pipeline().addLast("relay", new SocksDatagramRelayHandler());
        ch.pipeline().addLast("encoder", new SocksDatagramEncoderHandler());
    }
}
