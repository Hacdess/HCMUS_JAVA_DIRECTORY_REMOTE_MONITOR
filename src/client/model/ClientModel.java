package src.client.model;

import java.net.UnknownHostException;
import src.common.Protocol;

public class ClientModel {
  private String serverIP;
  private int serverPort;
  private String machineName;
  private String monitoringPath;
  private boolean isConnected;

  public ClientModel() {
    // Cấu hình mặc định
    this.serverIP = "localhost";
    this.serverPort = Protocol.DEFAULT_PORT;
    this.monitoringPath = "Chưa có";
    this.isConnected = false;
    
    // Tự động lấy tên máy tính
    try {
      this.machineName = java.net.InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      this.machineName = "Unknown Client";
    }
  }

  // --- GETTERS & SETTERS (Để Controller gọi) ---

  public String getServerIP() { return serverIP; }
  public void setServerIP(String serverIP) { this.serverIP = serverIP; }

  public int getServerPort() { return serverPort; }
  public void setServerPort(int serverPort) { this.serverPort = serverPort; }

  public String getMachineName() { return machineName; }
  
  public String getMonitoringPath() { return monitoringPath; }
  public void setMonitoringPath(String monitoringPath) { this.monitoringPath = monitoringPath; }

  public boolean isConnected() { return isConnected; }
  public void setConnected(boolean connected) { isConnected = connected; }
}