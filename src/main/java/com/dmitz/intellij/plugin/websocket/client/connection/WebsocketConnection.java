package com.dmitz.intellij.plugin.websocket.client.connection;

public interface WebsocketConnection {

  void connect(String url) throws Exception;

  void disconnect();

  void send(String message);

  boolean isConnected();
}
