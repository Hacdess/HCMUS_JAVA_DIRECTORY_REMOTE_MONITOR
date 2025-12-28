package src.client.controller;

import java.io.IOException;
import java.nio.file.*;
import src.common.Protocol;

// https://www.baeldung.com/java-nio2-watchservice
// https://docs.oracle.com/javase/8/docs/api/java/nio/file/WatchService.html
// https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystems.html

public class DirectoryWatcher extends Thread {
  private String path;
  private ClientController clientController;
  private WatchService watchService;
  private boolean  isRunning = true;

  public DirectoryWatcher(String path, ClientController clientController) {
    this.path = path;
    this.clientController = clientController;
  }

  @Override
  public void run() {
    try {
      this.watchService = FileSystems.getDefault().newWatchService();
      Path watchPath = Paths.get(this.path);

      if (!Files.exists(watchPath) || !Files.isDirectory(watchPath)) {
        String message = Protocol.CMD_NOTIFY + Protocol.SEPARATOR + "NOT_EXIST" + Protocol.SEPARATOR + this.path;
        clientController.sendMessage(message);
        clientController.getView().updateLog("Không tìm thấy đường dẫn: " + this.path);
        return;
      }

      clientController.getView().setMonitoringPath(path);
      String message = Protocol.CMD_NOTIFY + Protocol.SEPARATOR + "MONITORING" + Protocol.SEPARATOR + this.path;
      clientController.sendMessage(message);

      watchPath.register(
        this.watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY
      );

      WatchKey key;

      while (isRunning) {
        try {
          key = this.watchService.take();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        } catch (ClosedWatchServiceException cwse) {
          // WatchService đã bị đóng
          break;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == StandardWatchEventKinds.OVERFLOW) continue;

          @SuppressWarnings("unchecked")
          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path fileName = ev.context();

          Path dir = (Path) key.watchable();
          Path fullPath = dir.resolve(fileName);

          String action;
          if (kind == StandardWatchEventKinds.ENTRY_CREATE) action = Protocol.TYPE_CREATE;
          else if (kind == StandardWatchEventKinds.ENTRY_DELETE) action = Protocol.TYPE_DELETE;
          else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) action = Protocol.TYPE_MODIFY;
          else action = "UNKNOWN";

          message = Protocol.CMD_NOTIFY + Protocol.SEPARATOR + action + Protocol.SEPARATOR + fullPath.toString();
          clientController.sendMessage(message);

          String log = action + " -> " + fullPath.toString();
          clientController.getView().updateLog(log);

          if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            try {
              if (Files.isDirectory(fullPath)) {
                fullPath.register(
                  this.watchService,
                  StandardWatchEventKinds.ENTRY_CREATE,
                  StandardWatchEventKinds.ENTRY_DELETE,
                  StandardWatchEventKinds.ENTRY_MODIFY
                );
              }
            } catch (IOException e) {
              clientController.getView().updateLog("Không thể đăng ký thư mục con: " + e.getMessage());
            }
          }
        }

        boolean valid = key.reset();
        if (!valid) {
          break;
        }
      }

    } catch(IOException e) {
      clientController.getView().updateLog("Lỗi WatchService: " + e.getMessage());
    }
  }

  public void stopWatching() {
    isRunning = false;

    try {
      if (watchService != null) watchService.close();
    } catch (IOException e) {
      clientController.getView().updateLog("Lỗi khi đóng WatchService: " + e.getMessage());
    }
  }
}
