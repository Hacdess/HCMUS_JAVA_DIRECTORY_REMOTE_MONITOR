package src.server.main;

import src.server.controller.ServerController;
import src.server.view.ServerView;

public class ServerApp {
    public static void main(String[] args) {
        ServerView view = new ServerView();
        ServerController controller = new ServerController(view);

        view.setVisible(true);
        controller.startServer(); // Giờ hàm này chạy ngầm, không block giao diện nữa
    }
}