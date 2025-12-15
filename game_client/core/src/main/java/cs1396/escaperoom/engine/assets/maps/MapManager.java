package cs1396.escaperoom.engine.assets.maps;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.GdxRuntimeException;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.services.Networking;
import cs1396.escaperoom.services.Networking.StatusCode;

public class MapManager {

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

}
