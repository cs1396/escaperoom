package cs1396.escaperoom.editor.item.ui;

import java.io.File;
import java.util.Optional;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.ui.FilePicker;

public class TexturePicker {
  public static Optional<String> pickTexture(MapMetadata metadata){
    // filter pngs
    FileFilter filter = new FileNameExtensionFilter("PNGs", "png");

    // start in the home directory
    File home = new File(System.getProperty("user.home"));

    Optional<File> picked = FilePicker.pick("Select Texture", home, filter);

    if (picked.isEmpty()) return Optional.empty();

    // ensure texture directory exists
    if (!makeTextureDirectory(metadata)) return Optional.empty();

    // copy our texture file into the texture dir
    FileUtils.copy(picked.get().toPath(), new File(metadata.textureDirectory.get(), picked.get().getName()).toPath());

    // return the name of our file
    return Optional.of(picked.get().toPath().getFileName().toString());
  }

  private static boolean makeTextureDirectory(MapMetadata metadata){
    String texturePath = metadata.textureDirectory.orElse(metadata.locations.mapContentPath + "/textures");

    File textureDir = new File(texturePath);

    if (!textureDir.exists() && !FileUtils.tryCreateFolder(textureDir)){
      return false;
    }

    metadata.textureDirectory = Optional.of(texturePath);
    return true;
  }
}
