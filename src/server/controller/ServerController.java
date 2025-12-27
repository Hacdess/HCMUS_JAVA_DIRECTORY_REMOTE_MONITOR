package src.server.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import src.common.Protocol;
import src.server.view.ServerView;

public class ServerController {
  private ServerView view;
  private boolean isRunning;
  private Map<String, ClientHandler> clients = new HashMap<>();
  private Map<String, JTextArea> clientLogs = new HashMap<>();
  // Lưu các đường dẫn đã được client gửi (dùng để duyệt từ xa)
  private Map<String, java.util.Set<String>> clientPaths = new HashMap<>();

  public ServerController(ServerView view) {
    this.view = view;
    this.view.setController(this);
  }

  public void startServer() {
    new Thread(() -> {
      try (ServerSocket serverSocket = new ServerSocket(Protocol.DEFAULT_PORT)) {
        isRunning = true;
        System.out.println("Server đang chạy tại cổng " + Protocol.DEFAULT_PORT);

        while (isRunning) {
          Socket clientSocket = serverSocket.accept();
          System.out.println("Có kết nối mới!");
          
          // Tạo và chạy luồng xử lý riêng cho Client này
          ClientHandler handler = new ClientHandler(clientSocket, this);
          handler.start(); 
        }
      } catch (IOException e) {
        System.err.println("Server socket error: " + e.getMessage());
      }
    }).start();
  }

  // --- CÁC HÀM XỬ LÝ GIAO DIỆN ---

  public synchronized String onClientLogin(ClientHandler handler, String desiredName) {
    String name = (desiredName == null || desiredName.isEmpty()) ? "Unknown" : desiredName;
    String assigned = name;
    int suffix = 1;
    while (clients.containsKey(assigned)) {
      assigned = name + "#" + suffix;
      suffix++;
    }

    clients.put(assigned, handler);
    final String toShow = assigned;
    SwingUtilities.invokeLater(() -> {
      JTextArea txtLog = view.addNewTab(toShow); // Thêm Tab mới
      clientLogs.put(toShow, txtLog);
      txtLog.append("--- Client [" + toShow + "] đã tham gia hệ thống ---\n");
    });

    return assigned;
  }

  // Gọi khi client gửi một entry (TREE|path)
  public synchronized void onClientTreeEntry(String clientName, String fullPath) {
      clientPaths.putIfAbsent(clientName, new java.util.HashSet<>());
      clientPaths.get(clientName).add(fullPath);
      logToClient(clientName, "Tree: " + fullPath);
  }

  // Lấy các children trực tiếp đã biết cho clientName và parentPath
  // Nếu parentPath == null hoặc empty string, trả về các root đã biết
  public synchronized java.util.List<String> getClientChildren(String clientName, String parentPath) {
    java.util.List<String> res = new java.util.ArrayList<>();
    java.util.Set<String> set = clientPaths.get(clientName);
    if (set == null) return res;

    if (parentPath == null || parentPath.isEmpty()) {
      // root entries: những path có parent == null hoặc dạng root
      for (String p : set) {
          java.io.File f = new java.io.File(p);
          String parent = f.getParent();
          if (parent == null) res.add(p);
      }
      return res;
    }

    for (String p : set) {
      java.io.File f = new java.io.File(p);
      String parent = f.getParent();
      if (parent == null) continue;
      try {
          if (parent.equals(parentPath)) res.add(p);
      } catch (Exception ignore) {}
    }

    return res;
  }

  public synchronized void onClientDisconnect(String clientName) {
    clients.remove(clientName);
    clientLogs.remove(clientName);
    SwingUtilities.invokeLater(() -> view.removeTab(clientName));
  }

  public void logToClient(String clientName, String message) {
    JTextArea txt = clientLogs.get(clientName);
    if (txt != null) {
        SwingUtilities.invokeLater(() -> txt.append(message + "\n"));
    }
  }

  public void sendStartRequest(String clientName, String path) {
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_START + "|" + path);
  }

  public void sendStopRequest(String clientName) {
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_STOP);
  }

  public void sendListRequest(String clientName, String path) {
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_LIST + Protocol.SEPARATOR + path);
  }
}