package cs1396.escaperoom.engine.assets.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
  static Logger log = Logger.getLogger(FileUtils.class.getName());

  public static boolean tryCreateFolder(File dir){
    if (!dir.exists()){
      boolean created;
      try {
        // create the file
        created = dir.mkdir();
      } catch (Exception e) {
        log.warning("Unable to create dir " + dir.getAbsolutePath());
        e.printStackTrace();
        created = false;
      }

      // if it didnt error but didnt create exit here.
      if (!created) {
        return false;
      }
    }
    return true;
  }

  public static boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }

  public static boolean copyDirectory(Path src, Path dest) {
    try (Stream<Path> stream = Files.walk(src)) {
      stream.forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    } catch (Exception e){
      return false;
    }
    return true;
  }

  public static void copy(Path source, Path dest) {
    try {
      Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static String getAppDataDir() {
    String appFolder = "escaperoom";
    String os = System.getProperty("os.name").toLowerCase();
    String baseDir;

    if (os.contains("win")) {
      baseDir = System.getenv("APPDATA");
      if (baseDir == null) {
        baseDir = System.getenv("LOCALAPPDATA");
      }
      if (baseDir == null) {
        baseDir = System.getProperty("user.home");
      }
    } else if (os.contains("mac")) {
      baseDir = System.getProperty("user.home") + "/Library/Application Support";
    } else {
      String xdgDataHome = System.getenv("XDG_DATA_HOME");
      if (xdgDataHome != null && !xdgDataHome.isBlank()) {
        baseDir = xdgDataHome;
      } else {
        baseDir = System.getProperty("user.home") + "/.local/share";
      }
    }

    File dir = new File(baseDir, appFolder);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return dir.getAbsolutePath();
  }

  public static List<String> getFolders(String dir) {
    return Stream.of(new File(dir).listFiles())
        .filter(file -> file.isDirectory())
        .map(File::getName)
        .collect(Collectors.toList());
  }

  public static List<String> getFiles(String dir) {
    return Stream.of(new File(dir).listFiles())
        .filter(file -> !file.isDirectory())
        .map(File::getName)
        .collect(Collectors.toList());
  }
}
