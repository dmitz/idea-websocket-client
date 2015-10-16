package com.dmitz.intellij.plugin.websocket.client.action;

import com.dmitz.intellij.plugin.websocket.client.connection.WebSocketClient;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class DisconnectAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    getApplication().executeOnPooledThread(() -> {
      try {
        WebSocketClient.getInstance().disconnect();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    if (WebSocketClient.getInstance().isConnected()) {
      e.getPresentation().setEnabled(true);
    } else {
      e.getPresentation().setEnabled(false);
    }
  }
}
