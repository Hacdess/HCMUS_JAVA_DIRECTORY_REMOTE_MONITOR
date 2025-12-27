package src.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ClientView extends JFrame{
  private JTextArea logArea;
  private JLabel statusLabel;
  private JLabel pathLabel;

  public ClientView() {
    setTitle("Client - Hệ thống giám sát");
    setSize( 500, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setLocationRelativeTo(null); // center frame
    setLayout(new BorderLayout(10, 10));


    // ===== THÔNG TIN (NORTH) =====
    JPanel panelInfo = new JPanel();
    panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
    panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 5 , 10));

    statusLabel = new JLabel("Trạng thái: Đang khởi động...");
    statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
    statusLabel.setForeground(Color.BLUE);

    pathLabel = new JLabel("Đang giám sát: [Chưa nhận lệnh]");
    pathLabel.setFont(new Font("Arial", Font.PLAIN, 12));

    panelInfo.add(statusLabel);
    panelInfo.add(pathLabel);
    add(panelInfo, BorderLayout.NORTH);

    // ===== LOG (CENTER) =====
    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JScrollPane paneScroll = new JScrollPane(logArea);
    paneScroll.setBorder(BorderFactory.createTitledBorder("Nhật ký hoạt động (Log)"));
    add(paneScroll, BorderLayout.CENTER);

    // ===== Footer (SOUTH) =====
    JLabel footerLabel = new JLabel("Lê Trung Kiên - 23127075");
    footerLabel.setHorizontalAlignment(JLabel.CENTER);
    footerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    add(footerLabel, BorderLayout.SOUTH);
  }

  public void updateLog(String message) {
    SwingUtilities.invokeLater(() -> {
      String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
      logArea.append("[" + time + "]" + message + "\n");

      logArea.setCaretPosition(logArea.getDocument().getLength());
    });
  }

  public void setConnectionStatus(String status) {
    SwingUtilities.invokeLater(() -> {
      statusLabel.setText("Trạng thái: " + status);
      if (status.contains("thành công") || status.contains("Đã kết nối")) {
        statusLabel.setForeground(new Color(0, 150, 0)); // Màu xanh lá đậm
      } else {
        statusLabel.setForeground(Color.RED);
      }
    });
  }

  public void setMonitoringPath(String path) {
    SwingUtilities.invokeLater(() -> {
      pathLabel.setText("Đang giám sát: " + path);
      pathLabel.setForeground(Color.BLUE);
    });
  }
}
