package com.bright.proxy.udp;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;

import java.net.InetSocketAddress;

/**
 * @author Bright Xu
 */
public class SocksDatagramMsg {
    public ByteBuf content;
    public byte[] header;
    public Socks5AddressType dstAddrType;
    public String dstAddr;
    public int dstPort;
    public InetSocketAddress remoteAddress;
    public InetSocketAddress localAddress;

    public SocksDatagramMsg(ByteBuf content, byte[] header, Socks5AddressType dstAddrType, String dstAddr, int dstPort) {
        this.content = content;
        this.header = header;
        this.dstAddrType = dstAddrType;
        this.dstAddr = dstAddr;
        this.dstPort = dstPort;
    }
}
