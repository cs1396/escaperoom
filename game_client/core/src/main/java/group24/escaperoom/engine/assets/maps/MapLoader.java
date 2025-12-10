package group24.escaperoom.engine.assets.maps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.JsonReader;

import group24.escaperoom.engine.assets.AssetManager;
import group24.escaperoom.engine.assets.UserAtlasBuilder;
import group24.escaperoom.engine.assets.items.ItemLoader;
import group24.escaperoom.engine.assets.items.ItemLoader.LoadedObjects;
import group24.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import group24.escaperoom.engine.assets.utils.FileUtils;
import group24.escaperoom.game.world.Grid;
import group24.escaperoom.screens.AbstractScreen;

public class MapLoader {
  private static Logger log = Logger.getLogger(MapLoader.class.getName());

  public static Array<MapMetadata> discoverMaps(){
    Array<MapMetadata> maps = new Array<>();
    String mapsPath = FileUtils.getAppDataDir() +"/maps";

    File dataDir = new File(mapsPath);
    if (!dataDir.exists()){
      dataDir.mkdir();
    }

    String localLoc = mapsPath + "/local";
    File localDir = new File(localLoc);
    if (!localDir.exists()){
      localDir.mkdir();
    }

    String dlLoc = mapsPath + "/downloaded";
    File dlDir = new File(dlLoc);
    if (!dlDir.exists()){
      dlDir.mkdir();
    }

    for (String mapFolder : FileUtils.getFolders(localLoc)){
      tryLoadMetaData(new MapLocation(mapFolder, false)).ifPresent((m) -> maps.add(m));
    }
    for (String mapFolder : FileUtils.getFolders(dlLoc)){
      tryLoadMetaData(new MapLocation(mapFolder, true)).ifPresent((m) -> maps.add(m));
    }

    return maps;
  }

  public static Optional<MapData> tryLoadMap(MapMetadata data){
    return tryLoadMap(data, false);

  }

  public static Optional<MapData> tryLoadMap(MapMetadata data, boolean create){
    LoadedObjects.clearUserItems();
    AssetManager.instance().invalidateTextureCache();

    if (!tryLoadTextures(data)) return Optional.empty();

    if (!tryLoadObjects(data)) return Optional.empty();

    File mapDataPath = new File(data.locations.mapContentPath);
    if (!mapDataPath.exists()){
      if (create){
        Grid newGrid = new Grid(AbstractScreen.WORLD_WIDTH, AbstractScreen.WORLD_HEIGHT);
        if (!MapSaver.saveMap(newGrid, data)){
          log.warning(String.format("Failed to save new map %s", data.name));
        }
        return Optional.of(new MapData(newGrid, data));

      } else {
        log.warning(String.format("Cannot load map %s, file does not exist", mapDataPath.getAbsolutePath()));
        return Optional.empty();
      }

    }

    Grid grid = new Grid();
    File mainFile = new File(data.locations.mapMainFilePath);
    try {
      String jsonStr = Files.readString(mainFile.toPath());
      JsonReader reader = new JsonReader();

      grid.read(new Json(), reader.parse(jsonStr));
    } catch (Exception e) {
      log.severe(String.format("Error loading map json"));
      e.printStackTrace();

      return Optional.empty();
    }

    return Optional.of(new MapData(grid, data));
  }

  public static Optional<MapData> loadMap(MapLocation id) {
    return tryLoadMetaData(id).flatMap((meta) -> tryLoadMap(meta));
  }

  public static boolean reloadTextures(MapMetadata data){
    return tryLoadTextures(data, true);
  }

  private static boolean tryLoadTextures(MapMetadata data, boolean reload){
    if (data.textureDirectory.isPresent()) {
      Optional<TextureAtlas> maybeAtlas = tryBuildAtlas(data.textureDirectory.get(), reload);
      if (maybeAtlas.isEmpty()) {
        return false;
      }
      AssetManager.instance().registerUserAtlas(maybeAtlas.get());
    }
    return true;
  }

  public static boolean tryLoadTextures(MapMetadata data){
    return tryLoadTextures(data, true);
  }

  private static boolean tryLoadObjects(MapMetadata data){
    if (data.objectDirectory.isPresent()) {
      try {
        ItemLoader.LoadUserObjects(data.objectDirectory.get());
      } catch (Exception e){
        e.printStackTrace();
        log.severe("Failed to load user objects");
        return false;
      }
    }
    return true;
  }


  private static Optional<TextureAtlas> tryBuildAtlas(String textureDirPath, boolean unloadPrevious) {
    File textureDir = new File(textureDirPath);
    if (!textureDir.exists()) {
      log.warning(String.format("Failed to build atlas, texture directory (%s) does not exist ", textureDirPath));
      return Optional.empty();
    }

    Optional<String> path = UserAtlasBuilder.buildAtlas(textureDir.getAbsolutePath());
    if (path.isEmpty()) {
      log.warning("Failed to build atlas, build atlas failed");
      return Optional.empty();
    }

    String atlasPath = path.get();

    if (!unloadPrevious && AssetManager.instance().isLoaded(atlasPath)){
      return Optional.of(AssetManager.instance().get(atlasPath));
    }

    try {

      if (unloadPrevious && AssetManager.instance().isLoaded(atlasPath)){
        AssetManager.instance().unload(atlasPath);
      }

      AssetManager.instance().load(atlasPath, TextureAtlas.class);
      AssetManager.instance().finishLoadingAsset(atlasPath);
      TextureAtlas t = AssetManager.instance().get(atlasPath);
      return Optional.of(t);
    } catch (Exception e) {
      e.printStackTrace();
      log.warning("Failed to build atlas");
      return Optional.empty();
    }
  }

  public static Optional<MapMetadata> get(MapLocation id) {
      return tryLoadMetaData(id);
  }

  private static Optional<MapMetadata> tryLoadMetaData(MapLocation locations) {
    File mapDir = new File(locations.mapBasePath);
    if (!mapDir.exists() || !mapDir.isDirectory()) {
      log.warning(
          String.format("Failed to load map directory %s, directory does not exist", mapDir.getAbsolutePath()));
      return Optional.empty();
    }

    File mapContentDir = new File(locations.mapContentPath);
    if (!mapContentDir.exists() || !mapContentDir.isDirectory()) {
      log.warning(
          String.format("Failed to load map content directory %s, directory does not exist", mapContentDir.getAbsolutePath()));
      return Optional.empty();
    }

    File mapData = null;
    boolean definesObjects = false;
    boolean definesTextures = false;

    for (File f : mapContentDir.listFiles()) {
      if (f.getName().equals("mapdata.json")) {
        log.info("-> Found mapdata.json");
        mapData = f;
      }
      if (f.getName().equals("textures")) {
        log.info("-> Found a texture directory");
        definesTextures = true;
      }
      if (f.getName().equals("objects")) {
        log.info("-> Found an object directory");
        definesObjects = true;
      }
    }

    if (mapData == null) {
      log.warning(String.format("Map directory (%s) does not contain map json", mapContentDir.getAbsolutePath()));
      return Optional.empty();
    }

    File metadataFile = new File(locations.mapMetadataPath);
    if (!metadataFile.exists()) {
      log.warning(String.format("Failed to load map metadata file %s, file does not exist", metadataFile.getAbsolutePath()));
      return Optional.empty();
    }

    MapMetadata metadata = new MapMetadata();
    JsonValue json;
    try {
      FileReader fr = new FileReader(metadataFile);
      json = new JsonReader().parse(fr);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      log.warning(String.format("Exception while reading metadata file (%s)", metadataFile.getAbsolutePath()));
      return Optional.empty();
    }

    metadata.read(new Json(), json);

    if (definesTextures) {
      metadata.setTextureDir(locations.mapContentPath + "/textures");
    }
    if (definesObjects) {
      metadata.setObjectDir(locations.mapContentPath + "/objects");
    }

    metadata.locations = locations;
    // metadata.name = locations.name;

    return Optional.of(metadata);
  }
}
