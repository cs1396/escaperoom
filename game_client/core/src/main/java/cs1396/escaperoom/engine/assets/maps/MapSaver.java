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

  /**
   * Reinspect the map file structure to update the metadata
   * to reflect user defined objects and textures.
   * 
   * Saves the metadata to disk after update
   */
  public static boolean updateMetadata(MapMetadata metadata){
    // create the map folder if it doesn't exist
    File mapContentDir = new File(metadata.locations.mapContentPath);
    if (!mapContentDir.exists() || !mapContentDir.isDirectory()) {
      log.warning(
          String.format("Failed to load map content directory %s, directory does not exist", mapContentDir.getAbsolutePath()));
      return false;
    }

    boolean definesObjects = false;
    boolean definesTextures = false;

    for (File f : mapContentDir.listFiles()) {
      if (f.getName().equals("textures")) {
        log.info("-> Found a texture directory");
        definesTextures = true;
      }
      if (f.getName().equals("objects")) {
        log.info("-> Found an object directory");
        definesObjects = true;
      }
    }

    if (definesTextures) {
      metadata.setTextureDir(metadata.locations.mapContentPath + "/textures");
    }

    if (definesObjects) {
      metadata.setObjectDir(metadata.locations.mapContentPath + "/objects");
    }

    return saveMetadata(metadata);
  }

  public static boolean deleteMap(MapMetadata metadata){
    File mapDir = new File(metadata.locations.mapBasePath);
    return FileUtils.deleteDirectory(mapDir);
  }

  private static boolean saveMetadata(MapMetadata data) {
    // write the metadata to the map folder
    File metaDataFile = new File(data.locations.mapMetadataPath);

    log.info("Writing metadata to " + metaDataFile.getAbsolutePath());

    try (FileOutputStream fout = new FileOutputStream(metaDataFile)) {
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
  public static boolean saveMap(MapData data) {
    File dir = new File(data.metadata.locations.mapGridPath);
    if (!dir.mkdirs()){
      return false;
    }

    // save the metadata
    if (!saveMetadata(data.metadata)){
      return false;
    }


    for (String gridName : data.getGridNames()){
      File gridFile = new File(data.metadata.locations.mapGridPath + MapManager.gridFileName(gridName));

      log.info("Saving grid file for grid \"" + gridName + "\" at " + gridFile.getAbsolutePath());

      if (!FileUtils.tryCreatePath(gridFile)){
        return false;
      }

      if (!saveGrid(data.getGrid(gridName), gridFile)){
        return false;
      }
    }

    return true;
  }

  private static boolean saveGrid(Grid grid, File writeTo){
    try (FileOutputStream fout = new FileOutputStream(writeTo)){
      Json j = new Json();
      j.setOutputType(JsonWriter.OutputType.json);
      fout.write(j.toJson(grid).getBytes());
      return true;
    } catch (Exception e) {
      e.printStackTrace(); 
      return false;
    }
  }
}
