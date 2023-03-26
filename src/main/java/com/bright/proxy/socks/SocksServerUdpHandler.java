package com.bright.proxy.socks;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;

import java.net.InetSocketAddress;

/**
 * @author Bright Xu
 */
public class SocksServerUdpHandler extends SimpleChannelInboundHandler<SocksMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage message) {
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        boolean localIsIPv6 = (localAddress.getAddress().getAddress().length) == 16;
        Socks5AddressType dstAddrType = localIsIPv6 ? Socks5AddressType.IPv6 : Socks5AddressType.IPv4;
        String host = localAddress.getHostString();
        int port = localAddress.getPort() & 0xFFFF;
        ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                Socks5CommandStatus.SUCCESS, dstAddrType, host, port));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}
