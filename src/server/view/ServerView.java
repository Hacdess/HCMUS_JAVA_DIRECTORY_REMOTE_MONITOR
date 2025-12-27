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

        // 2. Phần dưới: Panel điều khiển
        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        bottomPanel.add(new JLabel("Đường dẫn giám sát:"));
        pathText = new JTextField(20);
        pathText.setText("D:/TestMonitor"); // Giá trị mặc định test cho nhanh
        bottomPanel.add(pathText);

        startButton = new JButton("Bắt đầu");
        stopButton = new JButton("Dừng");
        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Sự kiện nút bấm
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

    // --- CÁC HÀM THAO TÁC VỚI TAB ---

    /**
     * Thêm một Tab mới khi có Client kết nối
     * Trả về JTextArea của tab đó để Controller ghi log
     */
    public JTextArea addNewTab(String clientName) {
        JTextArea logText = new JTextArea();
        logText.setEditable(false);
        logText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane paneScroll = new JScrollPane(logText);
        
        // Thêm tab mới vào giao diện
        tabbedPane.addTab(clientName, paneScroll);
        
        // Tự động chuyển sang tab mới
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        
        return logText;
    }

    /**
     * Lấy tên Client đang được chọn (dựa trên Title của Tab)
     */
    public String getCurrentClientName() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            return tabbedPane.getTitleAt(index);
        }
        return null;
    }

    /**
     * Xóa Tab khi Client ngắt kết nối (Tùy chọn)
     */
    public void removeTab(String clientName) {
        int index = tabbedPane.indexOfTab(clientName);
        if (index != -1) {
            tabbedPane.remove(index);
        }
    }
}