package cs1396.escaperoom.ui;

import java.io.File;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import cs1396.escaperoom.engine.assets.utils.FileUtils;

public class FilePicker {
  public static Optional<File> pick(){
    File textureDir = new File(FileUtils.getAppDataDir() + "/textures");
    if (!textureDir.exists() && !FileUtils.tryCreateFolder(textureDir)){
      return Optional.empty();
    }
    return pick("Select",textureDir, null);
  }

  public static Optional<File> pick(String selectButton, File startPath, FileFilter filter){
    JFileChooser fileChooser = new JFileChooser();

    if (filter != null) fileChooser.setFileFilter(filter);

    int result = fileChooser.showDialog(null, selectButton);

    if (result == JFileChooser.APPROVE_OPTION){
      return Optional.of(fileChooser.getSelectedFile());
    }

    return Optional.empty();
  }
}
