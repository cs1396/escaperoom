package cs1396.escaperoom.services;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.services.Networking.StatusCode;
import cs1396.escaperoom.services.Networking.UploadResponse;

public class MapUploader {
  public static class UploadOutput {
    public final Optional<String> reason;
    public final Optional<UploadResponse> response;

    public UploadOutput(String reason) {
      this.reason = Optional.of(reason);
      this.response = Optional.empty();
    }

    public UploadOutput(UploadResponse response) {
      this.reason = Optional.empty();
      this.response = Optional.of(response);
    }
  }

  public static CompletableFuture<UploadOutput> uploadMap(MapMetadata metadata) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new UploadOutput("You must be logged in to upload a map!"));
    }

    // ensure that the creator is set
    if (metadata.stats.isEmpty()) {
      return CompletableFuture.supplyAsync(() -> new UploadOutput("uploadMap needs a metadata with present stats"));
    }

    String metadataString = metadata.toString();

    return Networking.uploadUserMap(metadata.locations, metadataString).thenApply((resp) -> {
      if (resp.code != StatusCode.OK) {
        return new UploadOutput("Error uploading map: (code " + resp.code.name() + ")");
      }
      return new UploadOutput(resp);
    });

  }
}
