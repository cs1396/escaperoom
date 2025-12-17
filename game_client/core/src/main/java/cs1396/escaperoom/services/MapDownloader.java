package cs1396.escaperoom.services;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Result.Err;
import cs1396.escaperoom.engine.types.Result.Ok;
import cs1396.escaperoom.services.Networking.StatusCode;

public class MapDownloader {
  public static Result<Void, String> prepareDownloadPath(MapMetadata metadata){
    if (!User.isLoggedIn()) {
      return new Err<>("You must be logged in to download a map");
    }

    // ensure that the id is set
    if (metadata.mapID.isEmpty()) {
      return  new Err<>("Map is missing a map ID!");
    }

    // ensure the downloads directory exists
    File downloadsDir = new File(FileUtils.getAppDataDir() + "maps/downloaded/");
    if (!downloadsDir.exists()) {
      if (!downloadsDir.mkdirs()) {
        return new Err<>("Unable to create downloads directory");
      }
    }

    // check if it exists, if so, abort. if not, create
    File mapDir = new File(metadata.locations.mapBasePath);
    if (mapDir.exists()) {
      return new Err<>("Map already downloaded");
    } else if (!mapDir.mkdirs()) {
      return  new Err<>("Unable to make map directory");
    }

    return Ok.unit();
  }

  public static CompletableFuture<Result<StatusCode, String>> downloadMap(MapMetadata metadata) {
    return prepareDownloadPath(metadata).andThenAsync(
      v -> Networking.downloadUserMap(metadata.mapID, metadata.locations.mapBasePath)
        .thenApply(rsp -> {
          if (rsp != StatusCode.OK) {
            return new Err<>("Error downloading map: (code " + rsp.name() + ")");
          }

          CompletableFuture<StatusCode> metaf = Networking.downloadMapMetadata(metadata.mapID, metadata.locations.mapBasePath);
          CompletableFuture<StatusCode> thumbf = Networking.downloadMapThumbnail(metadata.mapID, metadata.locations.mapBasePath);
          
          StatusCode meta = metaf.join();
          StatusCode thumb = thumbf.join();

          if (meta == StatusCode.OK && thumb == StatusCode.OK) {
            return new Ok<>(rsp);
          }

          FileUtils.deleteDirectory(new File(metadata.locations.mapBasePath));
          return new Err<>("failed to download map use data");
        })
    );
  }
}
