package src.client.main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import src.client.controller.ClientController;
import src.client.view.ClientView;

public class ClientApp {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        ClientView view = new ClientView();

        ClientController controller = new ClientController(view);

        // Hiển thị giao diện và chờ người dùng bấm Kết nối
        view.addConnectListener(e -> {
          new Thread(() -> controller.connectServer()).start();
        });

        view.setVisible(true);
      } catch (Exception e) {
        String msg = "Lỗi khởi tạo Client: " + e.getMessage();
        System.err.println(msg);
        JOptionPane.showMessageDialog(null, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
      }
    });   
  }
}
