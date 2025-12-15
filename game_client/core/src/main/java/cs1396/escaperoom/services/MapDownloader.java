package cs1396.escaperoom.services;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.services.Networking.StatusCode;

public class MapDownloader {
  public static class DownloadOutput {
    public final Optional<String> reason;
    public final Optional<StatusCode> response;

    public DownloadOutput(String reason) {
      this.reason = Optional.of(reason);
      this.response = Optional.empty();
    }

    public DownloadOutput(StatusCode response) {
      this.reason = Optional.empty();
      this.response = Optional.of(response);
    }
  }

  public static Optional<DownloadOutput> prepareDownloadPath(MapMetadata metadata){
    if (!User.isLoggedIn()) {
      return Optional.of(new DownloadOutput("You must be logged in to download a map"));
    }

    // ensure that the id is set
    if (metadata.mapID.isEmpty()) {
      return  Optional.of(new DownloadOutput("Map is missing a map ID!"));
    }

    // ensure the downloads directory exists
    File downloadsDir = new File(FileUtils.getAppDataDir() + "maps/downloaded/");
    if (!downloadsDir.exists()) {
      if (!downloadsDir.mkdirs()) {
        return Optional.of(new DownloadOutput("Unable to create downloads directory"));
      }
    }

    // check if it exists, if so, abort. if not, create
    File mapDir = new File(metadata.locations.mapBasePath);
    if (mapDir.exists()) {
      return Optional.of(new DownloadOutput("Map already downloaded"));
    } else if (!mapDir.mkdirs()) {
      return  Optional.of(new DownloadOutput("Unable to make map directory"));
    }

    return Optional.empty();
  }

  public static CompletableFuture<DownloadOutput> downloadMap(MapMetadata metadata) {

    Optional<DownloadOutput> oDO = prepareDownloadPath(metadata);
    if (oDO.isPresent()){
      return CompletableFuture.supplyAsync(() -> oDO.get());
    }

    return Networking.downloadUserMap(metadata.mapID, metadata.locations.mapBasePath).thenApply((rsp) -> {
      if (rsp != StatusCode.OK) {
        return new DownloadOutput("Error downloading map: (code " + rsp.name() + ")");
      }

      CompletableFuture<StatusCode> metaf = Networking.downloadMapMetadata(metadata.mapID, metadata.locations.mapBasePath);
      CompletableFuture<StatusCode> thumbf = Networking.downloadMapThumbnail(metadata.mapID, metadata.locations.mapBasePath);
      
      StatusCode meta = metaf.join();
      StatusCode thumb = thumbf.join();

      if (meta == StatusCode.OK && thumb == StatusCode.OK) {
        return new DownloadOutput(rsp);
      }

      FileUtils.deleteDirectory(new File(metadata.locations.mapBasePath));
      return new DownloadOutput("failed to download map use data");
    });

  }
}
