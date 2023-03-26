package com.bright.proxy;

import com.bright.proxy.udp.DatagramHandlerInit;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bright Xu
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        int port = 1080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup udpGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new MixinServerInitializer());

            Bootstrap udpBootstrap = new Bootstrap();
            udpBootstrap.group(udpGroup).channel(NioDatagramChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .handler(new DatagramHandlerInit());

            logger.info("Proxy Server starting...");
            ChannelFuture future = serverBootstrap.bind(port).sync();
            ChannelFuture future2 = udpBootstrap.bind(port).sync();
            logger.info("Proxy Server TCP started on " + future.channel().localAddress());
            logger.info("Proxy Server UDP started on " + future2.channel().localAddress());

            future2.channel().closeFuture().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            udpGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
