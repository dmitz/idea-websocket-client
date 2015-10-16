package com.dmitz.intellij.plugin.websocket.client.event;

public class Response {
  private String value;

  public Response(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
