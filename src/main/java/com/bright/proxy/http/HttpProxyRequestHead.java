package com.bright.proxy.http;

import io.netty.buffer.ByteBuf;

public class HttpProxyRequestHead {
    private String host;
    private int port;
    private HttpProxyType proxyType;
    private String protocolVersion;

    private ByteBuf byteBuf;

    public HttpProxyRequestHead(String host, int port, HttpProxyType proxyType, String protocolVersion, ByteBuf byteBuf) {
        this.host = host;
        this.port = port;
        this.proxyType = proxyType;
        this.protocolVersion = protocolVersion;
        this.byteBuf = byteBuf;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(HttpProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
