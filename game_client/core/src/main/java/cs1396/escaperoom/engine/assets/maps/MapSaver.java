package cs1396.escaperoom.engine.assets.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.game.world.Grid;

public class MapSaver {
  private static Logger log = Logger.getLogger(MapSaver.class.getName());

  private static boolean tryCreatePath(File file){
    if (!file.exists()){
      boolean created = true;
      try {
        // create the file
        created = file.createNewFile();
      } catch (Exception e) {
        log.warning("Unable to create file " + file.getAbsolutePath());
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

  public static boolean updateMetadata(MapMetadata metadata){
    // create the map folder if it doesn't exist
    File mapDir = new File(metadata.locations.mapBasePath);
    if (!FileUtils.tryCreateFolder(mapDir)){
      return false;
    }

    return saveMetadata(metadata);
  }

  public static boolean deleteMap(MapMetadata metadata){
    File mapDir = new File(metadata.locations.mapBasePath);
    return FileUtils.deleteDirectory(mapDir);
  }

  private static boolean saveMetadata(MapMetadata data) {
    // write the metadata to the map folder
    try {
      File metaDataFile = new File(data.locations.mapMetadataPath);
      FileOutputStream fout = new FileOutputStream(metaDataFile);
      Json j = new Json();
      j.setOutputType(JsonWriter.OutputType.json);
      fout.write(j.toJson(data).getBytes());
      fout.close();
    } catch (Exception e) {
      e.printStackTrace(); 
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} if saved and {@code false} if unable to be
   */
  public static boolean saveMap(Grid grid, MapMetadata metadata) {
    // create the app data directory if it doesn't exist
    File mapDir = new File(FileUtils.getAppDataDir());
    if (!FileUtils.tryCreateFolder(mapDir)){
      return false;
    }

    // create this maps folder if it doesn't exist
    File dir = new File(metadata.locations.mapBasePath);
    if (!FileUtils.tryCreateFolder(dir)){
      return false;
    }

    // create this maps content folder if it doesn't exist
    dir = new File(metadata.locations.mapContentPath);
    if (!FileUtils.tryCreateFolder(dir)){
      return false;
    }

    File map = new File(metadata.locations.mapMainFilePath);
    if (!tryCreatePath(map)){
      return false;
    }

    if (!saveMetadata(metadata)){
      return false;
    }

    // write the map to the map file
    try {
      FileOutputStream fout = new FileOutputStream(map);
      Json j = new Json();
      j.setOutputType(JsonWriter.OutputType.json);
      fout.write(j.toJson(grid).getBytes());
      fout.close();
    } catch (Exception e) {
      e.printStackTrace(); 
      return false;
    }

    return true;
  }
}
