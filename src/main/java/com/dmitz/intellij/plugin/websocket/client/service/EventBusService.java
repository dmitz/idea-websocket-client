package com.dmitz.intellij.plugin.websocket.client.service;

import com.google.common.eventbus.EventBus;

public class EventBusService {

  private EventBusService() {
  }

  private static class EventBusServiceHolder {
    public static final EventBus INSTANCE = new EventBus();
  }

  public static EventBus getInstance() {
    return EventBusServiceHolder.INSTANCE;
  }
}
