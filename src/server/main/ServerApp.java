package SRC.server.main;

import SRC.server.controller.ServerController;
import SRC.server.view.ServerView;

public class ServerApp {
  public static void main(String[] args) {
    ServerView view = new ServerView();
    ServerController controller = new ServerController(view);

    view.setVisible(true);
    controller.startServer();
  }
}