package src.server.view;

import java.awt.*;
import javax.swing.*;
import src.server.controller.ServerController;

public class ServerView extends JFrame {
  private JTabbedPane tabbedPane; // Quản lý các tab client
  private JTextField pathText;
  private JButton startButton, stopButton;
  private ServerController controller;

  public ServerView() {
    setTitle("Server - Hệ thống giám sát từ xa");
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    // ===== Quản lý Tab =====
    tabbedPane = new JTabbedPane();
    add(tabbedPane, BorderLayout.CENTER);

    // ===== Panel điều khiển BOTTOM =====
    JPanel bottomPanel = new JPanel(new FlowLayout());
    
    bottomPanel.add(new JLabel("Đường dẫn giám sát:"));
    pathText = new JTextField(20);
    pathText.setText("D:/Default");
    bottomPanel.add(pathText);

    startButton = new JButton("Bắt đầu");
    stopButton = new JButton("Dừng");
    bottomPanel.add(startButton);
    bottomPanel.add(stopButton);

    add(bottomPanel, BorderLayout.SOUTH);

    startButton.addActionListener(e -> {
      String currentClient = getCurrentClientName();
      String path = pathText.getText();
      if (currentClient != null && !path.isEmpty()) {
        controller.sendStartRequest(currentClient, path);
      } else {
        JOptionPane.showMessageDialog(this, "Chưa chọn Client hoặc thiếu đường dẫn!");
      }
    });

    stopButton.addActionListener(e -> {
      String currentClient = getCurrentClientName();
      if (currentClient != null) {
        controller.sendStopRequest(currentClient);
      }
    });
  }

  public void setController(ServerController controller) {
    this.controller = controller;
  }

  public JTextArea addNewTab(String clientName) {
    JTextArea logText = new JTextArea();
    logText.setEditable(false);
    logText.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane logScroll = new JScrollPane(logText);

    // Panel trên cùng với nút Browse Remote
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton browseBtn = new JButton("Browse Remote...");
    topPanel.add(browseBtn);

    // Container cho tab: topPanel trên, log ở giữa
    JPanel container = new JPanel(new BorderLayout());
    container.add(topPanel, BorderLayout.NORTH);
    container.add(logScroll, BorderLayout.CENTER);

    // Thêm tab mới vào giao diện
    tabbedPane.addTab(clientName, container);
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

    // Khi bấm Browse, mở dialog duyệt từ xa (sử dụng dữ liệu do client gửi)
    browseBtn.addActionListener(e -> {
      RemoteBrowserDialog dialog = new RemoteBrowserDialog(this, controller, clientName);
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
      // Nếu user chọn đường dẫn, cập nhật ô pathText
      String selected = dialog.getSelectedPath();
      if (selected != null) {
        pathText.setText(selected);
      }
    });

    return logText;
}

  public String getCurrentClientName() {
    int index = tabbedPane.getSelectedIndex();
    if (index != -1) {
      return tabbedPane.getTitleAt(index);
    }
    return null;
  }

  public void removeTab(String clientName) {
    int index = tabbedPane.indexOfTab(clientName);
    if (index != -1) {
      tabbedPane.remove(index);
    }
  }
}