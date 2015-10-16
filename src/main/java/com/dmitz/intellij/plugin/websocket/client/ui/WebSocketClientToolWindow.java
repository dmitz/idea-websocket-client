package com.dmitz.intellij.plugin.websocket.client.ui;

import com.dmitz.intellij.plugin.websocket.client.event.Connected;
import com.dmitz.intellij.plugin.websocket.client.event.Disconnected;
import com.dmitz.intellij.plugin.websocket.client.event.Response;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class WebSocketClientToolWindow extends SimpleToolWindowPanel {

  private static EditorTextField requestTextField;
  private static EditorTextField responseTextField;
  private static JBTextField addressTextField;

  private static final String NOTIFICATION_ID = "Websocket Client";

  public WebSocketClientToolWindow(EventBus eventBus) {
    super(false, true);
    initializeToolWindow();
    eventBus.register(new EventHandler());
  }

  public static String getRequestValue() {
    return requestTextField.getText();
  }

  public static String getAddressValue() {
    return addressTextField.getText();
  }

  private void initializeToolWindow() {
    JBPanel mainPanel = new JBPanel(new BorderLayout());
    JBPanel addressPanel = new JBPanel(new FlowLayout(FlowLayout.LEFT));

    ActionToolbar rightToolbar = createToolbar();

    JBLabel addressLabel = new JBLabel("Location: ");

    addressTextField = new JBTextField(25);
    addressPanel.add(addressLabel);
    addressPanel.add(addressTextField);

    mainPanel.add(addressPanel, BorderLayout.NORTH);

    requestTextField = new EditorTextField();
    responseTextField = new EditorTextField();
    requestTextField.setPlaceholder("Request");
    responseTextField.setPlaceholder("Response");

    JBSplitter splitter = new JBSplitter(false, 0.5f);
    splitter.setShowDividerControls(true);
    splitter.setFirstComponent(requestTextField);
    splitter.setSecondComponent(responseTextField);

    mainPanel.add(splitter, BorderLayout.CENTER);

    setContent(mainPanel);
    setToolbar(rightToolbar.getComponent());
  }

  private ActionToolbar createToolbar() {
    String toolbarId = "WebsocketClient.Toolbar";
    DefaultActionGroup groupFromConfig =
        (DefaultActionGroup) ActionManager.getInstance().getAction(toolbarId);
    DefaultActionGroup group = new DefaultActionGroup(groupFromConfig);
    ActionManager am = ActionManager.getInstance();

    ActionToolbar toolbar = am.createActionToolbar(toolbarId, group, true);
    toolbar.setOrientation(SwingConstants.VERTICAL);

    return toolbar;
  }

  class EventHandler {
    @Subscribe
    public void handle(Connected event) {
      Notifications.Bus.notify(
          new Notification(
              NOTIFICATION_ID,
              "Connection Successfully",
              addressTextField.getText(),
              NotificationType.INFORMATION)
      );
    }

    @Subscribe
    public void handle(Disconnected event) {
      Notifications.Bus.notify(
          new Notification(
              NOTIFICATION_ID,
              "Disconnected",
              addressTextField.getText(),
              NotificationType.INFORMATION)
      );
    }

    @Subscribe
    public void handle(Response event) {
      String response = event.getValue();
      getApplication().invokeLater(() ->
              responseTextField.setText(response)
      );
    }
  }
}
