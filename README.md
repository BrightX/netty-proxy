# Netty Proxy

> 基于 Netty 的网络代理服务器
> * 支持 `socks4a` / `socks5` / `http` 代理；
> * 支持 `IPv4` 和 `IPv6`。

#### How to use

```bash
# to build jar package
mvn package
```

```bash
cd target/

# run
# command: java -jar netty-proxy-1.0.jar [port]
# port is optional, default 1080 .

java -jar netty-proxy-1.0.jar 1080
```

```out
2023-03-26 22:19:08.399  INFO [           main] com.bright.proxy.Application             : Proxy Server starting...
2023-03-26 22:19:09.046  INFO [           main] com.bright.proxy.Application             : Proxy Server TCP started on /0:0:0:0:0:0:0:0:1080
2023-03-26 22:19:09.046  INFO [           main] com.bright.proxy.Application             : Proxy Server UDP started on /0:0:0:0:0:0:0:0:1080
```

#### ***Note***

* 仅限于局域网络内部使用；
* Java 1.8 or later.

### Not Fixed

* 需要添加 username / password 认证。
* UDP 代理一直监听是否合适 ？？？
