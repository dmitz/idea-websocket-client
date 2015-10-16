package com.dmitz.intellij.plugin.websocket.client.action;

import com.dmitz.intellij.plugin.websocket.client.connection.WebSocketClient;
import com.dmitz.intellij.plugin.websocket.client.ui.WebSocketClientToolWindow;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class ConnectAction extends AnAction {
  private static final String NOTIFICATION_ID = "Websocket Client";

  @Override
  public void actionPerformed(AnActionEvent e) {
    String serverAddress = WebSocketClientToolWindow.getAddressValue();
    getApplication().executeOnPooledThread(() -> {
          try {
            WebSocketClient.getInstance().connect(serverAddress);
          } catch (Exception ex) {
            Notifications.Bus.notify(
                new Notification(
                    NOTIFICATION_ID,
                    "Unable to connect",
                    serverAddress,
                    NotificationType.ERROR)
            );
          }
        }
    );
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    if (WebSocketClient.getInstance().isConnected()) {
      e.getPresentation().setEnabled(false);
    } else {
      e.getPresentation().setEnabled(true);
    }
  }
}
