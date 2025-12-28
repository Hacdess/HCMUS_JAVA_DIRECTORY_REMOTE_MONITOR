package src.client.controller;

import java.io.*;
import java.net.Socket;
import src.client.model.ClientModel;
import src.client.view.ClientView;
import src.common.Protocol;

public class ClientController {
  private ClientView view;
  private ClientModel model;
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private DirectoryWatcher currentWatcher;
  private boolean isConnected = false;

  public ClientController(ClientView view) {
    this.view = view;
    model = new ClientModel();  
  }

  // Hàm kết nối tới Server
  public void connectServer() {
    try {
      // Kết nối tới localhost (hoặc IP server) cổng 7075
      view.updateLog("Đang kết nối tới Server...");
      socket = new Socket(model.getServerIP(), model.getServerPort());
      model.setConnected(true);

      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
      isConnected = true;

      view.updateLog("Kết nối thành công!");
      view.setConnectionStatus("Đã kết nối");

      sendLoginInfo();

      // Đợi ngắn để server có thời gian xử lý LOGIN, rồi gửi hai ổ đĩa lớn nhất
      new Thread(() -> {
        // try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        SendTopRoots();
      }).start();

      Thread listenThread = new Thread(this::listenForServerCommands);
      listenThread.start();

    } catch (IOException e) {
      model.setConnected(false);
      view.updateLog("Lỗi kết nối: " + e.getMessage());
      view.setConnectionStatus("Mất kết nối");
    }
  }

  // Gửi tên máy cho Server biết
  private void sendLoginInfo() {
    sendMessage(Protocol.CMD_LOGIN + Protocol.SEPARATOR + model.getMachineName());
  }

  private void listenForServerCommands() {
    try {
      while (isConnected) {
        String message = in.readUTF();
        processMessage(message);
      }
    } catch (IOException e) {
      view.updateLog("Server đã ngắt kết nối.");
      isConnected = false;
      view.setConnectionStatus("Ngắt kết nối");
      stopMonitoring();
    }
  }

  private void processMessage(String message) {
    String[] parts = message.split("\\|");
    String cmd = parts[0];

    switch (cmd) {
      case Protocol.CMD_START -> {
        if (parts.length > 1) {
          String path = parts[1];
          startMonitoring(path);
        }
      }
      case Protocol.CMD_STOP -> stopMonitoring();

      case Protocol.CMD_LIST -> {
        if (parts.length > 1) {
          String target = parts[1];
          sendChildrenOf(target);
        }
      }
      default -> view.updateLog("Lệnh lạ từ Server: " + message);
    }
  }

  private void startMonitoring(String path) {
    // Nếu đang giám sát cái khác thì tắt
    stopMonitoring();

    model.setMonitoringPath(path);
    view.updateLog("Server yêu cầu giám sát: " + path);
    
    // Khởi tạo và chạy luồng Watcher mới
    // Truyền this vào để Watcher có thể gọi hàm sendMessage gửi ngược lại Server
    currentWatcher = new DirectoryWatcher(path, this);
    currentWatcher.start();
  }

  private void stopMonitoring() {
    if (currentWatcher != null) {
      currentWatcher.stopWatching(); // Gọi hàm dừng trong Watcher
      currentWatcher = null; // Hủy tham chiếu
      view.updateLog("Đã dừng giám sát.");
    }
  }

  public synchronized void sendMessage(String message) {
    try {
      if (isConnected && out != null) {
        out.writeUTF(message);
        out.flush();
      }
    } catch (IOException e) {
      view.updateLog("Lỗi gửi tin: " + e.getMessage());
    }
  }

  public ClientView getView() {
    return view;
  }

  private void SendTopRoots() {
    try {
      java.io.File[] roots = java.io.File.listRoots();
      if (roots == null || roots.length == 0) return;

      for (java.io.File r : roots) {
        try {
          String path = r.getAbsolutePath();
          sendMessage(Protocol.CMD_TREE + Protocol.SEPARATOR + path);
        } catch (Exception ignore) {}
      }
    } catch (Exception e) {
      view.updateLog("Lỗi gửi root drives: " + e.getMessage());
    }
  }

  // Server gửi LIST|path -> gửi các thư mục con ngay bên trong path
  private void sendChildrenOf(String target) {
    try {
      java.io.File dir = new java.io.File(target);
      if (!dir.exists() || !dir.isDirectory()) return;
      java.io.File[] children = dir.listFiles();
      if (children == null) return;
      for (java.io.File c : children) {
        if (c.isDirectory()) {
          sendMessage(Protocol.CMD_TREE + Protocol.SEPARATOR + c.getAbsolutePath());
        }
      }
    } catch (Exception e) {
      view.updateLog("Lỗi khi liệt kê thư mục: " + e.getMessage());
    }
  }
}