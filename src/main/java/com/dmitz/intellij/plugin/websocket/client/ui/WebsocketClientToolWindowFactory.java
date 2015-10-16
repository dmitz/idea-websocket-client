package com.dmitz.intellij.plugin.websocket.client.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.dmitz.intellij.plugin.websocket.client.service.EventBusService;
import org.jetbrains.annotations.NotNull;

public class WebsocketClientToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    WebSocketClientToolWindow clientToolWindow =
        new WebSocketClientToolWindow(EventBusService.getInstance());

    toolWindow.getComponent().getParent().add(clientToolWindow);
  }
}
