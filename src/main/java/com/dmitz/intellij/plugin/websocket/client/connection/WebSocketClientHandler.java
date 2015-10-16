package com.dmitz.intellij.plugin.websocket.client.connection;

import com.dmitz.intellij.plugin.websocket.client.event.Connected;
import com.dmitz.intellij.plugin.websocket.client.event.Disconnected;
import com.dmitz.intellij.plugin.websocket.client.event.Response;
import com.dmitz.intellij.plugin.websocket.client.service.EventBusService;
import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

  private final WebSocketClientHandshaker handshaker;
  private ChannelPromise handshakeFuture;
  private EventBus eventBus;

  public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
    this.handshaker = handshaker;
    this.eventBus = EventBusService.getInstance();
  }

  public ChannelFuture handshakeFuture() {
    return handshakeFuture;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    handshakeFuture = ctx.newPromise();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    handshaker.handshake(ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    eventBus.post(new Disconnected());
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel channel = ctx.channel();
    if (!handshaker.isHandshakeComplete()) {
      handshaker.finishHandshake(channel, (FullHttpResponse) msg);
      handshakeFuture.setSuccess();
      eventBus.post(new Connected());
      return;
    }

    if (msg instanceof FullHttpResponse) {
      FullHttpResponse response = (FullHttpResponse) msg;
      throw new IllegalStateException(
          "Unexpected FullHttpResponse (getStatus=" + response.status() +
              ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
    }

    WebSocketFrame frame = (WebSocketFrame) msg;
    if (frame instanceof TextWebSocketFrame) {
      TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      eventBus.post(new Response(textFrame.text()));
    } else if (frame instanceof CloseWebSocketFrame) {
      channel.close();
      eventBus.post(new Disconnected());
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    if (!handshakeFuture.isDone()) {
      handshakeFuture.setFailure(cause);
    }
    ctx.close();
  }
}
