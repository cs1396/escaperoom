package cs1396.escaperoom.services;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;

/**
 * A permanent record for a given player that records global statistics
 */
public class PlayerRecord {
  public int attempts = 0;
  public int clears = 0;
  public String username;
  public Array<String> clearList = new Array<>();

  @Override
  public String toString() {
    Json json = new Json();
    json.setOutputType(JsonWriter.OutputType.json);
    return json.toJson(this);
  }

  public void update(GameStatistics stats, MapMetadata metadata) {
    if (metadata.mapID.isEmpty()) {
      return;
    }
    if (!clearList.contains(metadata.mapID, false)){
      clearList.add(metadata.mapID);
    }
    attempts += 1;
    if (stats.completedSucessfully) {
      clears += 1;
    }
  }
}
