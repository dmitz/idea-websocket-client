package com.dmitz.intellij.plugin.websocket.client.connection;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

public class WebSocketClient implements WebsocketConnection {

  private Channel channel;
  private boolean connected;

  private WebSocketClient() {
  }

  private static class WebSocketClientHolder {
    private static final WebSocketClient INSTANCE = new WebSocketClient();
  }

  public static WebSocketClient getInstance() {
    return WebSocketClientHolder.INSTANCE;
  }

  @Override
  public void connect(String url) throws Exception {
    URI uri = new URI(url);
    setConnected(false);

    String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
    final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
    final int port;
    if (uri.getPort() == -1) {
      if ("http".equalsIgnoreCase(scheme)) {
        port = 80;
      } else if ("https".equalsIgnoreCase(scheme)) {
        port = 443;
      } else {
        port = -1;
      }
    } else {
      port = uri.getPort();
    }

    if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
      Notifications.Bus.notify(
          new Notification(
              "Websocket Client",
              "Unable to connect",
              "Only WS(S) is supported.",
              NotificationType.ERROR)
      );
      return;
    }

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
          uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
      WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler(handshaker);

      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
          .channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ChannelPipeline p = ch.pipeline();

              p.addLast(
                  new HttpClientCodec(),
                  new HttpObjectAggregator(8192),
                  webSocketClientHandler);
            }
          });

      channel = bootstrap.connect(uri.getHost(), port).sync().channel();
      webSocketClientHandler.handshakeFuture().sync();
      setConnected(true);

      for (; ; );
    } finally {
      group.shutdownGracefully();
      setConnected(false);
    }
  }

  @Override
  public void disconnect() {
    channel.disconnect();
    setConnected(false);
  }

  @Override
  public void send(String msg) {
    if ("bye".equals(msg.toLowerCase())) {
      channel.writeAndFlush(new CloseWebSocketFrame());
      try {
        channel.closeFuture().sync();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else if ("ping".equals(msg.toLowerCase())) {
      WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
      channel.writeAndFlush(frame);
    } else {
      WebSocketFrame frame = new TextWebSocketFrame(msg);
      channel.writeAndFlush(frame);
    }
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  private void setConnected(boolean value) {
    connected = value;
  }
}