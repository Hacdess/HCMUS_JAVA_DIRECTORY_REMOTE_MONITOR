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
    private boolean isConnected = true;

    public ClientHandler(Socket socket, ServerController serverController) {
        this.socket = socket;
        this.serverController = serverController;
    }

    @Override
    public void run() {
        try {
            // 1. Mở luồng dữ liệu
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // 2. VÒNG LẶP LẮNG NGHE (QUAN TRỌNG NHẤT)
            while (isConnected) {
                // Code sẽ dừng ở đây chờ Client gửi tin tới
                String message = in.readUTF(); 
                
                System.out.println("DEBUG: Server nhận được -> " + message); // In ra để kiểm tra
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

        if (cmd.equals(Protocol.CMD_LOGIN)) {
            // LOGIN | TênMáy
            this.clientName = (parts.length > 1) ? parts[1] : "Unknown";
            // Gọi Controller để cập nhật giao diện (Thêm Tab)
            serverController.onClientLogin(clientName, this);
        } 
        else if (cmd.equals(Protocol.CMD_NOTIFY)) {
            // NOTIFY | ACTION | PATH
            if (parts.length >= 3) {
                serverController.logToClient(clientName, "Báo cáo: " + parts[1] + " -> " + parts[2]);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            if (out != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void closeConnection() {
        isConnected = false;
        if (clientName != null) {
            serverController.onClientDisconnect(clientName);
        }
        try { if (socket != null) socket.close(); } catch (Exception e) {}
    }
}