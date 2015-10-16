package com.dmitz.intellij.plugin.websocket.client.action;

import com.dmitz.intellij.plugin.websocket.client.connection.WebSocketClient;
import com.dmitz.intellij.plugin.websocket.client.ui.WebSocketClientToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class SendAction extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    {
      getApplication().executeOnPooledThread(() ->
          WebSocketClient.getInstance()
              .send(WebSocketClientToolWindow.getRequestValue()));
    }
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
