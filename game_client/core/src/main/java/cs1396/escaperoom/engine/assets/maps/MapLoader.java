package cs1396.escaperoom.engine.assets.maps;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.JsonReader;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.AssetManager.UserAtlas;
import cs1396.escaperoom.engine.assets.UserAtlasBuilder;
import cs1396.escaperoom.engine.assets.items.ItemLoader;
import cs1396.escaperoom.engine.assets.items.ItemLoader.LoadedObjects;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Result.Err;
import cs1396.escaperoom.engine.types.Result.IOErr;
import cs1396.escaperoom.engine.types.Result.Ok;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.AbstractScreen;

public class MapLoader {
  private static Logger log = Logger.getLogger(MapLoader.class.getName());

  public record MapLoadErr(String reason) {
    public Err<MapData, MapLoadErr> asErr(){
      return new Err<>(this);
    }
  }

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
      tryLoadMetaData(new MapLocation(mapFolder, false)).inspect((m) -> maps.add(m));
    }

    for (String mapFolder : FileUtils.getFolders(dlLoc)){
      tryLoadMetaData(new MapLocation(mapFolder, true)).inspect((m) -> maps.add(m));
    }

    return maps;
  }

  public static Result<MapData, MapLoadErr> tryLoadMap(MapMetadata data){
    return tryLoadMap(data, false);
  }

  /**
   * Load a legacy map that defines {@code MAPDIR/content/mapdata.json} 
   * by transitioning that data into the new file struture
   */
  private static Result<MapData, MapLoadErr> tryLoadLegacyMap(MapMetadata metadata){

    log.info("Legacy Map file detected, transitioning to new structure");
    // Transition from old file structure to new one
    if(!FileUtils.tryCreateFolder(new File(metadata.locations.mapGridPath))){
      return new MapLoadErr("").asErr();
    }

    File legacyMapFile = new File(metadata.locations.mapMainFilePath);
    File newMapFile = new File(
      metadata.locations.mapGridPath + MapManager.gridFileName(MapData.DEFAULT_GRID_NAME)
    );

    log.info(
      String.format("Copying %s to %s", legacyMapFile.getAbsolutePath(), newMapFile.getAbsolutePath())
    );
    FileUtils.copy(legacyMapFile.toPath(), newMapFile.toPath());

    if (!newMapFile.exists()){
      log.info(
        String.format("Error Copying %s to %s",
          legacyMapFile.getAbsolutePath(),
          newMapFile.getAbsolutePath()
        )
      );
      return new MapLoadErr("Failed to transition legacy map data").asErr();
    }

    log.info(
      String.format("Removing old map data at %s", legacyMapFile.getAbsolutePath())
    );
    if (!legacyMapFile.delete()){
      log.warning(
        String.format("Error removing old map data at", legacyMapFile.getAbsolutePath())
      );
      return new MapLoadErr("Failed to delete legacy map data").asErr();
    }

    log.info("Updating and saving new metadata");
    metadata.startingGrid = MapData.DEFAULT_GRID_NAME;

    if (!MapSaver.updateMetadata(metadata)){
      log.warning("Failed to save new metadata");
      return new MapLoadErr("Failed to transition from legacy map data").asErr();
    }

    return tryLoadMulitGridMap(metadata, false);
  }

  /**
   * Load a multigrid map
   *
   * @param metadata for the map 
   * @param create whether to create the map if it doesn't exist
   */
  private static Result<MapData, MapLoadErr> tryLoadMulitGridMap(MapMetadata metadata, boolean create){

    File gridFolder = new File(metadata.locations.mapGridPath);

    boolean gridFolderExists = gridFolder.exists();
    boolean gridFolderEmpty = true;
    File[] gridFiles = null;

    if (gridFolderExists) {
      gridFiles = gridFolder.listFiles();
      gridFolderEmpty = gridFiles == null ? true : gridFiles.length == 0;
    }

    boolean gridFolderValid = gridFolderExists && !gridFolderEmpty;

    // If the folder doesn't exist and we aren't creating -> invalid
    if (!gridFolderValid && !create){
      log.warning("Cannot load map with no defined grids: " + gridFolder.getAbsolutePath());
      return new MapLoadErr("Cannot load map with no defined grids: " + gridFolder.getAbsolutePath()).asErr();
    }

    MapData mapData = new MapData(metadata);

    if (!gridFolderValid && create){
      log.info("Grid folder was invalid, and create set -> creating new map" );
      Grid grid = new Grid(AbstractScreen.WORLD_WIDTH, AbstractScreen.WORLD_HEIGHT);
      mapData.registerGrid(MapData.DEFAULT_GRID_NAME, grid);
      mapData.registerStart(MapData.DEFAULT_GRID_NAME);

      if (!MapSaver.saveMap(mapData)){
        log.warning(String.format("Failed to save new map %s", metadata.name));
        return new MapLoadErr("Failed to save new map " + metadata.name).asErr();
      }
      return new Ok<>(mapData);
    }
    
    boolean foundStart = false;
    for (File maybeGrid : gridFiles){
      Optional<String> maybeGridName = MapManager.gridNameFromFileName(maybeGrid.getName());
      if (maybeGridName.isPresent()){
        String gridName = maybeGridName.get();

        mapData.registerGrid(gridName);

        if (gridName.equals(metadata.startingGrid)){
          foundStart = true;
          mapData.registerStart(gridName);
        } 

      }
    }

    if (!foundStart){
      String errMsg = String.format(
        "Did not find starting grid \"%s\" in %s",
          metadata.startingGrid,
          gridFolder.getAbsolutePath()
        );

      log.severe(errMsg);
      return new MapLoadErr(errMsg).asErr();
    }

    return new Ok<>(mapData);
  }

  /**
   * Try to load a map.
   *
   * @param metadata for the map 
   * @param create whether to create the map if it doesn't exist
   *
   * @return {@code Err(MapLoadErr)} on failure, {@code Ok(MapData)} on success
   */
  public static Result<MapData, MapLoadErr> tryLoadMap(MapMetadata metadata, boolean create){
    LoadedObjects.clearUserItems();
    File legacyMapPath = new File(metadata.locations.mapMainFilePath);

    return tryLoadTextures(metadata)
      .mapErr(MapLoadErr::new)
      .andThen(__ -> tryLoadObjects(metadata).mapErr(MapLoadErr::new))
      .andThen(__ -> {
        if (legacyMapPath.exists()){
          return tryLoadLegacyMap(metadata);
        } else {
          return tryLoadMulitGridMap(metadata, create);
        }
      });
  }

  /**
   * Package private function to load a grid
   *
   * @param gridName name of the grid
   * @param metadata metadata of the map
   */
  static Optional<Grid> loadGrid(String gridName, MapMetadata metadata){
    return loadGrid(new File(metadata.locations.mapGridPath + MapManager.gridFileName(gridName)));
  }

  private static Optional<Grid> loadGrid(File gridPath){
    log.info("Loading grid from file: " + gridPath.getAbsolutePath());

    if (!gridPath.exists()){ 
      log.info("Grid file (" + gridPath.getAbsolutePath() + ") did not exist");
      return Optional.empty();
    }

    Grid grid = new Grid();
    try {
      String jsonStr = Files.readString(gridPath.toPath());
      JsonReader reader = new JsonReader();

      grid.read(new Json(), reader.parse(jsonStr));
    } catch (Exception e) {
      log.severe(String.format("Error loading grid json"));
      e.printStackTrace();

      return Optional.empty();
    }

    log.info("Load sucessful (" + gridPath.getAbsolutePath() + ")");
    return Optional.of(grid);

  }

  public static Result<MapData, MapLoadErr> loadMap(MapLocation id) {
    return tryLoadMetaData(id)
      .mapErr(MapLoadErr::new)
      .flatMap(meta -> tryLoadMap(meta));
  }

  public static boolean reloadTextures(MapMetadata data){
    return tryLoadTextures(data, true).isOk();
  }

  private static Result<Void, String> tryLoadTextures(MapMetadata data, boolean reload){
    if (data.textureDirectory.isPresent()) {
      log.info("Texture directory is present at (" + data.textureDirectory.get() + ") -> building atlas");
      return tryBuildAtlas(data.textureDirectory.get(), reload).match(
        atlas -> {
          log.info("Successfully built atlas -> registering");
          AssetManager.instance().registerUserAtlas(atlas);
          return Ok.unit();
        }, 
        e -> {
          log.info("Error building atlas: "  + e);
          return new Err<>(e);
        }
      );
    }
    return Ok.unit();
  }

  public static Result<Void, String> tryLoadTextures(MapMetadata data){
    return tryLoadTextures(data, true);
  }

  private static Result<Void, String> tryLoadObjects(MapMetadata data){
    if (data.objectDirectory.isPresent()) {
      try {
        ItemLoader.LoadUserObjects(data.objectDirectory.get());
      } catch (Exception e){
        e.printStackTrace();
        return IOErr.withLog("Failed to load user objects", log);
      }
    }
    return Ok.unit();
  }


  private static Result<UserAtlas, String> tryBuildAtlas(String textureDirPath, boolean reload) {
    File textureDir = new File(textureDirPath);

    if (!textureDir.exists()) {
      return IOErr.withLog(String.format("Failed to build atlas, texture directory (%s) does not exist ", textureDirPath), log);
    }

    return UserAtlasBuilder.buildAtlas(textureDir.getAbsolutePath()).flatMap(atlas -> {
      String atlasPath = atlas.getAbsolutePath();
      try {
        if (!reload && AssetManager.instance().isLoaded(atlasPath)){
          return new Ok<>(AssetManager.instance().get(atlasPath));
        }

        AssetManager.instance().load(atlasPath, TextureAtlas.class);
        AssetManager.instance().finishLoadingAsset(atlasPath);
        TextureAtlas t = AssetManager.instance().get(atlasPath);
        return new Ok<>(new UserAtlas(t, atlasPath));
      } catch (Exception e) {
        e.printStackTrace();
        return IOErr.withLog("Failed to build atlas", log);
      }
    });
  }

  public static Optional<MapMetadata> get(MapLocation id) {
      return tryLoadMetaData(id).ok();
  }

  private static Result<MapMetadata, String> tryLoadMetaData(MapLocation locations) {
    File mapDir = new File(locations.mapBasePath);
    if (!mapDir.exists() || !mapDir.isDirectory()) {
      return IOErr.withLog("Failed to load map directory " + mapDir.getAbsolutePath() + ", directory does not exist", log);
    }

    File mapContentDir = new File(locations.mapContentPath);
    if (!mapContentDir.exists() || !mapContentDir.isDirectory()) {
      return IOErr.withLog("Failed to load map content directory " + mapDir.getAbsolutePath() + ", directory does not exist", log);
    }

    File mapData = null;
    boolean definesObjects = false;
    boolean definesTextures = false;
    boolean definesGrids = false;

    for (File f : mapContentDir.listFiles()) {
      if (f.getName().equals("mapdata.json")) {
        log.info("-> Found mapdata.json");
        mapData = f;
      }
      if (f.getName().equals("grids")){
        log.info("-> Found mapdata.json");
        definesGrids = true;
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

    if (mapData == null && !definesGrids) {
      return IOErr.withLog(
        String.format("Map directory (%s) does not contain map json nor a grid directory with grid files", mapContentDir.getAbsolutePath()),
        log
      );
    }

    File metadataFile = new File(locations.mapMetadataPath);
    if (!metadataFile.exists()) {
      return IOErr.withLog(
        String.format("Failed to load map metadata file %s, file does not exist", metadataFile.getAbsolutePath()),
        log
      );
    }

    MapMetadata metadata = new MapMetadata();
    JsonValue json;
    try {
      FileReader fr = new FileReader(metadataFile);
      json = new JsonReader().parse(fr);
    } catch (Exception e) {
      e.printStackTrace();
      return IOErr.withLog(
        String.format("Exception while reading metadata file (%s)", metadataFile.getAbsolutePath()),
        log
      );
    }

    metadata.read(new Json(), json);

    if (definesTextures) {
      metadata.setTextureDir(locations.mapContentPath + "/textures");
    }
    if (definesObjects) {
      metadata.setObjectDir(locations.mapContentPath + "/objects");
    }

    metadata.locations = locations;

    return new Ok<>(metadata);
  }
}
