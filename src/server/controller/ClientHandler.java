package src.server.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import src.common.Protocol;

public class ClientHandler extends Thread {
  private Socket socket;
  private ServerController serverController;
  private DataInputStream in;
  private DataOutputStream out;
  private String clientName;
  private java.util.List<String> pendingTree = new java.util.ArrayList<>();
  private boolean isConnected = true;

  public ClientHandler(Socket socket, ServerController serverController) {
    this.socket = socket;
    this.serverController = serverController;
  }

  @Override
  public void run() {
    try {
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      while (isConnected) {
        String message = in.readUTF(); 
        System.out.println("DEBUG: Server nhận được -> " + message);
        processMessage(message);
      }

    } catch (IOException e) {
      System.out.println("Client đã ngắt kết nối.");
    } finally {
      closeConnection();
    }
  }

  private void processMessage(String message) {
    String[] parts = message.split("\\|");
    String cmd = parts[0];

    switch (cmd) {
      case Protocol.CMD_LOGIN -> {
        // LOGIN | TênMáy
        String desired = (parts.length > 1) ? parts[1] : "Unknown";
        String assigned = serverController.onClientLogin(this, desired);
        this.clientName = assigned;
        if (!pendingTree.isEmpty()) { // Tree tới lẹ hơn
          for (String p : pendingTree) {
            serverController.onClientTreeEntry(assigned, p);
          }
          pendingTree.clear();
        }
      }
      case Protocol.CMD_NOTIFY -> {
        // NOTIFY | ACTION | PATH
        if (parts.length >= 3) {
          serverController.logToClient(clientName, "Báo cáo: " + parts[1] + " -> " + parts[2]);
        }
      }
      case Protocol.CMD_TREE -> {
        // TREE | PATH
        if (parts.length >= 2) {
          String path = parts[1];
          if (this.clientName == null) {
            pendingTree.add(path); // Lỡ Tree đến lẹ hơn client thì cho vô phòng chờ
          } else {
            serverController.onClientTreeEntry(clientName, path);
          }
        }
      }
      default -> {}
    }
  }

  public void sendMessage(String message) {
    try {
      if (out != null) {
        out.writeUTF(message);
        out.flush();
      }
    } catch (IOException e) {
      if (clientName != null) serverController.logToClient(clientName, "Lỗi gửi tin: " + e.getMessage());
      else System.err.println("Lỗi gửi tin (chưa có clientName): " + e.getMessage());
    }
  }

  private void closeConnection() {
    isConnected = false;
    if (clientName != null) {
      serverController.onClientDisconnect(clientName);
    }
    try { if (socket != null) socket.close(); } catch (IOException e) {
      System.err.println("Lỗi đóng socket: " + e.getMessage());
    }
  }
}