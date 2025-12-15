package cs1396.escaperoom.engine.assets.maps;

import java.util.Optional;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.services.GameStatistics;
import cs1396.escaperoom.services.User;

public class MapMetadata implements Json.Serializable {
  public static class MapStats {
    public static class WorldRecord {
      public long fastestms = -1;
      public String username = "No Record";
    }
    public static enum ValidStats {
      downloads,
      attempts,
      upvotes,
      downvotes,
    }

    public String creator;
    public String description;
    public long downloads = 0, attempts = 0, upvotes = 0, downvotes = 0, clears = 0; 
    public WorldRecord record = new WorldRecord();
    public GameStatistics clearStats;

    public static MapStats fromGameStats(GameStatistics gameStats){
      MapStats ms = new MapStats();
      ms.creator = User.getCredentials().username;
      ms.description = "";
      ms.clearStats = gameStats;

      return ms;
    }
  }

  public static class MapLocation {
    public String folderName;
    public String mapBasePath;
    public String mapContentPath;
    public String mapMainFilePath;
    public String mapMetadataPath;
    public String mapThumbnailPath;
    public boolean isDownloaded;

    public MapLocation(String folderName, boolean isDownloaded){
      this.folderName = folderName;
      this.isDownloaded = isDownloaded;

      if (isDownloaded) this.mapBasePath = FileUtils.getAppDataDir() + "/maps/downloaded/" + folderName;
      else this.mapBasePath = FileUtils.getAppDataDir() + "/maps/local/" + folderName;

      this.mapContentPath = mapBasePath + "/content";
      this.mapMainFilePath = mapBasePath + "/content/mapdata.json";
      this.mapMetadataPath = mapBasePath + "/metadata.json";
      this.mapThumbnailPath = mapBasePath + "/thumbnail.png";
    }
  }

  public String name = "";
  public String mapID = "";
  public MapLocation locations;
  public Optional<MapStats> stats = Optional.empty();
  public Optional<String> textureDirectory = Optional.empty();
  public Optional<String> objectDirectory = Optional.empty();
  public GameSettings gameSettings = new GameSettings();

  public MapMetadata(String name, boolean downloaded){
    this.locations = new MapLocation(name, downloaded);
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MapMetadata){
      MapMetadata other = MapMetadata.class.cast(obj);
      if (this.mapID.isEmpty() || other.mapID.isEmpty()){
        return this.name.equals(other.name);
      } else {
        return this.mapID.equals(other.mapID);
      }
    }
    return false;
  }

  public void setTextureDir(String absolutePath){
    textureDirectory = Optional.of(absolutePath);
  }

  public void setObjectDir(String absolutePath){
    objectDirectory = Optional.of(absolutePath);
  }

  public MapMetadata(){}

  @Override
  public void write(Json json) {
    json.setUsePrototypes(false);
    stats.ifPresent((s) -> {
      json.writeValue("stats", s);
    });
    json.writeValue("game_settings", gameSettings);
    json.writeValue("id", this.mapID);
    json.writeValue("name", this.name);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    this.mapID = jsonData.getString("id");
    this.name = jsonData.getString("name");

    JsonValue statsData = jsonData.get("stats");
    if (statsData != null) this.stats = Optional.of(json.readValue(MapStats.class, statsData));

    JsonValue settingsData = jsonData.get("game_settings");
    gameSettings = new GameSettings();
    if (settingsData != null) gameSettings = json.readValue(GameSettings.class, settingsData);
  }

  @Override
  public String toString() {
    Json json = new Json();
    json.setOutputType(JsonWriter.OutputType.json);
    return json.toJson(this);
  }
}
