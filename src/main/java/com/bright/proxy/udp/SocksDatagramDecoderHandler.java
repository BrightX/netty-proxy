package com.bright.proxy.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.socksx.v5.Socks5AddressDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;

/**
 * @author Bright Xu
 */
public class SocksDatagramDecoderHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    public static final Socks5AddressDecoder addressDecoder = Socks5AddressDecoder.DEFAULT;

    public SocksDatagramDecoderHandler() {
        // 不自动释放 msg
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf in = msg.content();
        int headerIdx = in.readerIndex();

        in.skipBytes(3);
        final Socks5AddressType dstAddrType = Socks5AddressType.valueOf(in.readByte());
        final String dstAddr = addressDecoder.decodeAddress(dstAddrType, in);
        final int dstPort = in.readUnsignedShort();

        int headerLen = in.readerIndex() - headerIdx;
        byte[] header = new byte[headerLen];
        in.slice(headerIdx, headerLen).readBytes(header);

        SocksDatagramMsg socksDatagramMsg = new SocksDatagramMsg(in, header, dstAddrType, dstAddr, dstPort);
        socksDatagramMsg.remoteAddress = msg.sender();
        socksDatagramMsg.localAddress = msg.recipient();
        ctx.fireChannelRead(socksDatagramMsg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}
