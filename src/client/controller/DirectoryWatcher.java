package src.client.controller;

import java.io.IOException;
import java.nio.file.*;

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
      Path path = Paths.get(this.path);

      if (!Files.exists(path) || !Files.isDirectory(path)) {
        // Lỗi: Đường dẫn không tồn tại
        return;
      }

      path.register(
        this.watchService, 
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY
      );

      // Thông báo: đang giám sát thư mục
      WatchKey key;

      while (isRunning) {
        try {
          key = this.watchService.take(); 
        } catch (InterruptedException e) {
          e.printStackTrace();
          return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();
          
          if (kind == StandardWatchEventKinds.OVERFLOW) continue;

          // Java sẽ không còn báo warning ở dòng này nữa
          @SuppressWarnings("unchecked")

          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path fileName = ev.context();

          // Thông báo sự kiện thay đổi thư mục
          String message = "NOTIFY|" + kind + "|" + fileName;
          clientController.sendMessage(message);
        }
      }

    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public void stopWatching() {
    isRunning = false;

    try {
      if (watchService != null) watchService.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
