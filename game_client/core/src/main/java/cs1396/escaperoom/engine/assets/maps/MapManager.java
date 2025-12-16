package cs1396.escaperoom.engine.assets.maps;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.GdxRuntimeException;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.services.Networking;
import cs1396.escaperoom.services.Networking.StatusCode;

public class MapManager {
  private static Logger log = Logger.getLogger(MapManager.class.getName());

  public static Optional<Image> loadThumbNail(String path) {
    try {
      AssetManager.instance().load(path, Texture.class);
      AssetManager.instance().finishLoadingAsset(path);
      Image thumbnail = new Image(
          AssetManager.instance().get(path, Texture.class));
      return Optional.of(thumbnail);
    } catch (GdxRuntimeException gdxre) {
      System.err.println("failed to load thumbnail with path: " + path);
      return Optional.empty();
    }
  }

  /**
   * Fetch the thumbnail for a given map metadata, returning the path of that file
   *
   * We can't load the image on a non OpenGL thread
   */
  public static CompletableFuture<Optional<String>> fetchThumbnail(MapMetadata metadata) {
    if (metadata.locations.isDownloaded) {
      return CompletableFuture.supplyAsync(() -> Optional.of(metadata.locations.mapThumbnailPath));
    }

    String dataDir = FileUtils.getAppDataDir();
    File tempDir = new File(dataDir + "/cache");
    if (!tempDir.exists()) {
      if (!tempDir.mkdir()) {
        return CompletableFuture.supplyAsync(() -> Optional.empty());
      }
    }

    File thumbnailFile = new File(dataDir + "/cache/" + metadata.mapID + "thumbnail.png");
    if (thumbnailFile.exists()) {
      return CompletableFuture.supplyAsync(() -> Optional.of(thumbnailFile.getAbsolutePath()));
    }

    return Networking.downloadMapThumbnail(metadata.mapID, tempDir.getAbsolutePath()).thenApply((StatusCode s) -> {
      if (s == StatusCode.OK) {
        File tempThumbnailFile = new File(tempDir, "thumbnail.png");
        if (!tempThumbnailFile.exists()) {
          return Optional.empty();
        }
        tempThumbnailFile.renameTo(thumbnailFile);
        return Optional.of(thumbnailFile.getAbsolutePath());
      }
      return Optional.empty();
    });

  }

  public static Optional<MapMetadata> copy(MapMetadata from, String newName) {
    MapMetadata to = new MapMetadata(newName, false);
    if (new File(to.locations.mapBasePath).exists()) {
      return Optional.empty();
    }

    File mapDataPath = new File(from.locations.mapBasePath);
    File newMapDataPath = new File(to.locations.mapBasePath);

    if (!newMapDataPath.mkdirs()) {
      return Optional.empty();
    }

    // copy the dir to the new dir
    if (!FileUtils.copyDirectory(mapDataPath.toPath(), newMapDataPath.toPath())) {
      return Optional.empty();
    }

    // remove the old metadata
    File oldMetaData = new File(to.locations.mapMetadataPath);
    if (!oldMetaData.delete()) {
      return Optional.empty();
    }
    if (!MapSaver.updateMetadata(to)) {
      return Optional.empty();
    }

    return Optional.of(to);
  }

  /**
   * Base64 encode a string with a URL encoder
   */
  static String stringEncode(String encode){
    return Base64.getUrlEncoder().encodeToString(encode.getBytes());
  }

  /**
   * @return the encoded filename of the grid
   */
  static String gridFileName(String gridName){
    return stringEncode(gridName) + ".json";
  }

  /**
   * @return the name of the grid from a file name, {@code Optional.empty()} if 
   *         the filename is:
   *           - not a json file
   *           - not base64 encoded
   *           - not valid UTF-8 when decoded
   */
  static Optional<String> gridNameFromFileName(String fileName){
    int suffixIndex = fileName.lastIndexOf(".json");
    if (suffixIndex == -1) return Optional.empty();

    String encodedGridName = fileName.substring(0, suffixIndex);

    try {
      byte[] decoded = Base64.getUrlDecoder().decode(encodedGridName.getBytes());
      String gridName = new String(decoded, StandardCharsets.UTF_8);
      return Optional.of(gridName);
    } catch (IllegalArgumentException iae){
      log.warning("Grid file name " + fileName + " is not Base64 encoded");
      return Optional.empty();
    } catch (Exception e){
      log.warning("Failed to decode filename: " + fileName);
      return Optional.empty();
    }

  }

}
