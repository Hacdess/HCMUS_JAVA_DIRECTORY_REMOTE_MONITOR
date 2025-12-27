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
                e.printStackTrace();
            }
        }).start();
    }

    // --- CÁC HÀM XỬ LÝ GIAO DIỆN ---
    public synchronized void onClientLogin(String clientName, ClientHandler handler) {
        clients.put(clientName, handler);
        SwingUtilities.invokeLater(() -> {
            JTextArea txtLog = view.addNewTab(clientName); // Thêm Tab mới
            clientLogs.put(clientName, txtLog);
            txtLog.append("--- Client [" + clientName + "] đã tham gia hệ thống ---\n");
        });
    }

    public synchronized void onClientDisconnect(String clientName) {
        clients.remove(clientName);
        clientLogs.remove(clientName);
        SwingUtilities.invokeLater(() -> view.removeTab(clientName));
    }

    public void logToClient(String clientName, String msg) {
        JTextArea txt = clientLogs.get(clientName);
        if (txt != null) {
            SwingUtilities.invokeLater(() -> txt.append(msg + "\n"));
        }
    }

    // Gửi lệnh Start/Stop (Giữ nguyên như cũ)
    public void sendStartRequest(String clientName, String path) {
        ClientHandler h = clients.get(clientName);
        if (h != null) h.sendMessage(Protocol.CMD_START + "|" + path);
    }

    public void sendStopRequest(String clientName) {
        ClientHandler h = clients.get(clientName);
        if (h != null) h.sendMessage(Protocol.CMD_STOP);
    }
}