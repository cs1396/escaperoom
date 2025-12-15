package cs1396.escaperoom.engine.assets.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
  // Much of the code here is taken and modified from https://www.baeldung.com/java-compress-and-uncompress

  /**
   * Create a zip archive of the given filepath. Accepts folders or files.
   *
   * Creates the zip archive in the same directory as the source, with the same
   * name as the source but with the extention set to `.zip`
   *
   * Returns {@code true} if successful, {@code false} otherwise
   */
  public static boolean CreateArchive(String filepath) {
    try {
      FileOutputStream fos = new FileOutputStream(String.format("%s.zip", filepath));
      ZipOutputStream zipOut = new ZipOutputStream(fos);

      File fileToZip = new File(filepath);
      addFileToZip(fileToZip, fileToZip.getName(), zipOut);
      zipOut.close();
      fos.close();
    } catch (IOException e) {
      new File(String.format("%s.zip", filepath)).delete();
      e.printStackTrace();
      return false;
    }

    return true;
  }
  private static void addFileToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }

    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
      }
      zipOut.closeEntry();

      File[] children = fileToZip.listFiles();
      for (File childFile : children) {
        addFileToZip(childFile, fileName + "/" + childFile.getName(), zipOut);
      }

      return;
    }

    FileInputStream fis = new FileInputStream(fileToZip);
    zipOut.putNextEntry(new ZipEntry(fileName));

    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }

    fis.close();
  }

  /**
   * Unzip the given archive into given destination.
   * 
   * It is assumed that the destination is a directory which exists.
   *
   * Returns {@code true} if successful, {@code false} otherwise
   */
  public static boolean UnpackArchive(String filepath, File destination) {
    byte[] buffer = new byte[1024];
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(filepath))) {

      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = newFile(destination, zipEntry);

        if (zipEntry.isDirectory()) {
          if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        } else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          // write file content
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }

        zipEntry = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }
  private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }
}
