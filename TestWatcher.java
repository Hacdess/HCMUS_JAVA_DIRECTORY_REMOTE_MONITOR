import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class TestWatcher {
    // Thêm dòng này để tắt cảnh báo unchecked cast
    @SuppressWarnings("unchecked") 
    public static void main(String[] args) {
        try {
            String folderName = "E:/";
            Path path = Paths.get(folderName);
            
            if (!Files.exists(path)) {
                Files.createDirectory(path);
                System.out.println("-> Da tao thu muc: " + path.toAbsolutePath());
            }

            WatchService watcher = FileSystems.getDefault().newWatchService();
            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            System.out.println("-------------------------------------------------");
            System.out.println("DANG GIAM SAT THU MUC: " + path.toAbsolutePath());
            System.out.println("Hay vao thu muc tren va thu: Tao file, Sua file, Xoa file...");
            System.out.println("-------------------------------------------------");

            while (true) {
                WatchKey key;
                try {
                    key = watcher.take(); 
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == OVERFLOW) continue;

                    // Java sẽ không còn báo warning ở dòng này nữa
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    System.out.println("PHAT HIEN: " + kind + " - File: " + fileName);
                }

                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("Thu muc giam sat da bi xoa/doi ten. Dung chuong trinh.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}