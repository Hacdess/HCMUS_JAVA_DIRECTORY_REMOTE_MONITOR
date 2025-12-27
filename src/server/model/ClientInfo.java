package src.server.model;

public class ClientInfo {
  private String clientName;
  private String ip;
  private boolean isMonitoring;
  private String monitoringPath;

  public ClientInfo(String clientName, String ip) {
    this.clientName = clientName;
    this.ip = ip;
    this.isMonitoring = false;
    this.monitoringPath = "Chưa giám sát";
  }

    public String getClientName() { return clientName; }
    public String getIp() { return ip; }

    public boolean isMonitoring() { return isMonitoring; }
    public void setMonitoring(boolean monitoring) { isMonitoring = monitoring; }
    
    public String getMonitoringPath() { return monitoringPath; }
    public void setMonitoringPath(String monitoringPath) { this.monitoringPath = monitoringPath; }
}
