package com.bright.proxy.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

import java.net.URL;

public class HttpServerHeadDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    private final HeadLineByteProcessor headLineByteProcessor = new HeadLineByteProcessor();

    public HttpServerHeadDecoder() {
        super(false);
    }

    private static String[] splitInitialLine(AppendableCharSequence sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonSPLenient(sb, 0);
        aEnd = findSPLenient(sb, aStart);

        bStart = findNonSPLenient(sb, aEnd);
        bEnd = findSPLenient(sb, bStart);

        cStart = findNonSPLenient(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[]{
                sb.subStringUnsafe(aStart, aEnd),
                sb.subStringUnsafe(bStart, bEnd),
                cStart < cEnd ? sb.subStringUnsafe(cStart, cEnd) : ""};
    }

    private static int findNonSPLenient(AppendableCharSequence sb, int offset) {
        for (int result = offset; result < sb.length(); ++result) {
            char c = sb.charAtUnsafe(result);
            // See https:///www.rfc-editor.org/html/rfc7230#section-3.5
            if (isSPLenient(c)) {
                continue;
            }
            if (Character.isWhitespace(c)) {
                // Any other whitespace delimiter is invalid
                throw new IllegalArgumentException("Invalid separator");
            }
            return result;
        }
        return sb.length();
    }

    private static int findSPLenient(AppendableCharSequence sb, int offset) {
        for (int result = offset; result < sb.length(); ++result) {
            if (isSPLenient(sb.charAtUnsafe(result))) {
                return result;
            }
        }
        return sb.length();
    }

    private static boolean isSPLenient(char c) {
        // See https://www.rfc-editor.org/rfc/rfc7230#section-3.5
        return c == ' ' || c == (char) 0x09 || c == (char) 0x0B || c == (char) 0x0C || c == (char) 0x0D;
    }

    private static int findEndOfString(AppendableCharSequence sb) {
        for (int result = sb.length() - 1; result > 0; --result) {
            if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result + 1;
            }
        }
        return 0;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        AppendableCharSequence seq = headLineByteProcessor.parse(in);
        if (seq == null) {
            return;
        }
        if (seq.charAt(seq.length() - 1) == '\n') {
            HttpProxyRequestHead httpProxyRequestHead;
            String[] splitInitialLine = splitInitialLine(seq);
            String method = splitInitialLine[0];
            String uri = splitInitialLine[1];
            String protocolVersion = splitInitialLine[2];
            String host;
            int port;
            if ("CONNECT".equalsIgnoreCase(method)) {
                //https tunnel proxy
                HostAndPort hostAndPort = HostAndPort.fromString(uri);
                host = hostAndPort.getHost();
                port = hostAndPort.getPort();

                httpProxyRequestHead = new HttpProxyRequestHead(host, port, HttpProxyType.TUNNEL, protocolVersion, null);
            } else {
                //http proxy
                URL url = new URL(uri);
                host = url.getHost();
                port = url.getPort();
                if (port == -1) {
                    port = 80;
                }

                httpProxyRequestHead = new HttpProxyRequestHead(host, port, HttpProxyType.WEB, protocolVersion, in.resetReaderIndex());
            }
            ctx.pipeline().addLast(new HttpServerConnectHandler()).remove(this);
            ctx.fireChannelRead(httpProxyRequestHead);
        }
    }

    private static class HeadLineByteProcessor implements ByteProcessor {
        private AppendableCharSequence seq;

        public AppendableCharSequence parse(ByteBuf buffer) {
            this.seq = new AppendableCharSequence(Math.min(4096, buffer.readableBytes()));
            seq.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                return null;
            }
            buffer.readerIndex(i + 1);
            return seq;
        }

        @Override
        public boolean process(byte value) {
            char nextByte = (char) (value & 0xFF);
            if (nextByte == '\n') {
                int len = seq.length();
                if (len >= 1 && seq.charAtUnsafe(len - 1) == '\r') {
                    seq.append(nextByte);
                }
                return false;
            }
            //continue loop byte
            seq.append(nextByte);
            return true;
        }
    }

}
