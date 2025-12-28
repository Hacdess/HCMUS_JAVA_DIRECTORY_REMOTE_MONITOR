package SRC.server.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import SRC.common.Protocol;
import SRC.server.view.ServerView;

public class ServerController {
  private ServerView view;
  private boolean isRunning;
  private Map<String, ClientHandler> clients = new HashMap<>(); // Lưu tên clients
  private Map<String, JTextArea> clientLogs = new HashMap<>(); // Lưu logs của clients
  private Map<String, java.util.Set<String>> clientPaths = new HashMap<>(); // Lưu các đường dẫn của từng client

  public ServerController(ServerView view) {
    this.view = view;
    this.view.setController(this);
  }

  public void startServer() {
    new Thread(() -> {
      try (ServerSocket serverSocket = new ServerSocket(Protocol.DEFAULT_PORT)) {
        isRunning = true;

        while (isRunning) {
          Socket clientSocket = serverSocket.accept();          
          ClientHandler handler = new ClientHandler(clientSocket, this);
          handler.start(); 
        }
      } catch (IOException e) {
        System.err.println("Server socket error: " + e.getMessage());
      }
    }).start();
  }

  public synchronized String onClientLogin(ClientHandler handler, String desiredName) {
    String name = (desiredName == null || desiredName.isEmpty()) ? "Unknown" : desiredName;
    String assigned = name;

    // Chống duplicate tên client khi chạy nhiều client trên 1 máy
    int suffix = 1;
    while (clients.containsKey(assigned)) {
      assigned = name + "#" + suffix;
      suffix++;
    }

    clients.put(assigned, handler);
    final String toShow = assigned;
    SwingUtilities.invokeLater(() -> {
      JTextArea logText = view.addNewTab(toShow); // Thêm Tab mới
      clientLogs.put(toShow, logText);
      logText.append("--- Client [" + toShow + "] đã tham gia hệ thống ---\n");
    });

    return assigned;
  }

  // Gọi khi client gửi một entry (TREE|path)
  public synchronized void onClientTreeEntry(String clientName, String fullPath) {
    clientPaths.putIfAbsent(clientName, new java.util.HashSet<>());
    clientPaths.get(clientName).add(fullPath);
  }

  // Lấy các children trực tiếp đã biết cho clientName và parentPath
  public synchronized java.util.List<String> getClientChildren(String clientName, String parentPath) {
    java.util.List<String> res = new java.util.ArrayList<>();
    java.util.Set<String> set = clientPaths.get(clientName);
    if (set == null) return res;

    // root entries là những path có parent == null hoặc dạng root
    if (parentPath == null || parentPath.isEmpty()) {
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
        if (parent.equals(parentPath)) res.add(p); // Các con của parentPath
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
    JTextArea text = clientLogs.get(clientName);
    if (text != null) {
      SwingUtilities.invokeLater(() -> text.append(message + "\n"));
    }
  }

  public void sendStartRequest(String clientName, String path) {
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_START + "|" + path);
  }

  public void sendStopRequest(String clientName) {
    logToClient(clientName, "Báo cáo: Đã dừng giám sát");
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_STOP);
  }

  public void sendListRequest(String clientName, String path) {
    ClientHandler h = clients.get(clientName);
    if (h != null) h.sendMessage(Protocol.CMD_LIST + Protocol.SEPARATOR + path);
  }
}