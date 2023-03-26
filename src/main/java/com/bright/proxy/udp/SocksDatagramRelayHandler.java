package com.bright.proxy.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

/**
 * @author Bright Xu
 */
public class SocksDatagramRelayHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof SocksDatagramMsg) {
            SocksDatagramMsg msg1 = (SocksDatagramMsg) msg;

            Bootstrap b = new Bootstrap();
            b.group(ctx.channel().eventLoop()).channel(NioDatagramChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx1, Object msg) {
                                    DatagramPacket dp = (DatagramPacket) msg;
                                    final CompositeByteBuf buf = Unpooled.compositeBuffer();
                                    buf.addComponents(true, Unpooled.wrappedBuffer(msg1.header), dp.content());
                                    DatagramPacket dp1 = new DatagramPacket(buf, msg1.remoteAddress, msg1.localAddress);
                                    ctx.channel().writeAndFlush(dp1).addListener(f -> {
                                        if (f.isSuccess()) {
                                            // 将数据包发送到客户端后，释放数据包，并关闭通道
                                            if (buf.refCnt() > 0) {
                                                // buf.release();
                                                ReferenceCountUtil.release(buf);
                                            }
                                            ch.close();
                                        }
                                    });
                                }
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });
            final DatagramPacket dp = new DatagramPacket(msg1.content,
                    new InetSocketAddress(msg1.dstAddr, msg1.dstPort));
            final ChannelFuture channelFuture = b.bind(0);
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    channelFuture.channel().writeAndFlush(dp).addListener(f -> {
                        // 将数据包发送到目标主机后，释放数据包
                        if (msg1.content.refCnt() > 0) {
                            ReferenceCountUtil.release(msg1.content);
                        }
                    });
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}
