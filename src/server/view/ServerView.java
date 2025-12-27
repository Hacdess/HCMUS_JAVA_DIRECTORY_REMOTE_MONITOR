package src.server.view;

import java.awt.*;
import java.awt.event.*;
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
            RemoteBrowserDialog dialog = new RemoteBrowserDialog(this, clientName);
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

    // Dialog nhỏ mô phỏng JFileChooser cho filesystem của client
    private class RemoteBrowserDialog extends JDialog {
        private String clientName;
        private DefaultListModel<String> listModel = new DefaultListModel<>();
        private JList<String> listView = new JList<>(listModel);
        private JButton upBtn = new JButton("Up");
        private JButton selectBtn = new JButton("Select");
        private JButton refreshBtn = new JButton("Refresh");
        private String currentPath = ""; // empty = root
        private String selectedPath = null;

        public RemoteBrowserDialog(Frame owner, String clientName) {
            super(owner, "Browse: " + clientName, true);
            this.clientName = clientName;
            setSize(600, 400);
            setLayout(new BorderLayout());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(upBtn);
            top.add(refreshBtn);
            add(top, BorderLayout.NORTH);

            listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane sp = new JScrollPane(listView);
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(selectBtn);
            add(bottom, BorderLayout.SOUTH);

            // Load initial roots
            refreshList();

            upBtn.addActionListener(a -> {
                if (currentPath == null || currentPath.isEmpty()) return;
                java.io.File f = new java.io.File(currentPath);
                String parent = f.getParent();
                currentPath = (parent == null) ? "" : parent;
                refreshList();
            });

            refreshBtn.addActionListener(a -> refreshList());

            listView.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String sel = listView.getSelectedValue();
                        if (sel == null) return;
                        // sel contains full path
                        currentPath = sel;
                        refreshList();
                    }
                }
            });

            selectBtn.addActionListener(a -> {
                selectedPath = listView.getSelectedValue();
                dispose();
            });
        }

        private void refreshList() {
            listModel.clear();
            // Request children known locally first
            java.util.List<String> children = controller.getClientChildren(clientName, currentPath);
            if (children.isEmpty()) {
                // Ask client to list children
                controller.sendListRequest(clientName, currentPath == null ? "" : currentPath);
                // Wait briefly for responses to arrive
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                children = controller.getClientChildren(clientName, currentPath);
            }

            for (String c : children) listModel.addElement(c);
        }

        public String getSelectedPath() { return selectedPath; }
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