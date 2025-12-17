package cs1396.escaperoom.services;

import java.util.concurrent.CompletableFuture;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Result.Err;
import cs1396.escaperoom.engine.types.Result.Ok;
import cs1396.escaperoom.services.Networking.StatusCode;
import cs1396.escaperoom.services.Networking.UploadResponse;

public class MapUploader {
  public static CompletableFuture<Result<UploadResponse, String>> uploadMap(MapMetadata metadata) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new Err<>("You must be logged in to upload a map!"));
    }

    // ensure that the creator is set
    if (metadata.stats.isEmpty()) {
      return CompletableFuture.supplyAsync(() -> new Err<>("uploadMap needs a metadata with present stats"));
    }

    String metadataString = metadata.toString();

    return Networking.uploadUserMap(metadata.locations, metadataString).thenApply((resp) -> {
      if (resp.code != StatusCode.OK) {
        return new Err<>("Error uploading map: (code " + resp.code.name() + ")");
      }
      return new Ok<>(resp);
    });

  }
}
