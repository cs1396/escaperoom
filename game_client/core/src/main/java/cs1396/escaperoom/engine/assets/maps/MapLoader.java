package cs1396.escaperoom.engine.assets.maps;

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

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.UserAtlasBuilder;
import cs1396.escaperoom.engine.assets.items.ItemLoader;
import cs1396.escaperoom.engine.assets.items.ItemLoader.LoadedObjects;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.AbstractScreen;

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

  /**
   * Load a legacy map that defines {@code MAPDIR/content/mapdata.json} 
   * by transitioning that data into the new file struture
   */
  private static Optional<MapData> tryLoadLegacyMap(MapMetadata metadata){

    log.info("Legacy Map file detected, transitioning to new structure");
    // Transition from old file structure to new one
    if(!FileUtils.tryCreateFolder(new File(metadata.locations.mapGridPath))){
      return Optional.empty();
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
      return Optional.empty();
    }

    log.info(
      String.format("Removing old map data at %s", legacyMapFile.getAbsolutePath())
    );
    if (!legacyMapFile.delete()){
      log.warning(
        String.format("Error removing old map data at", legacyMapFile.getAbsolutePath())
      );
      return Optional.empty();
    }

    log.info("Updating and saving new metadata");
    metadata.startingGrid = MapData.DEFAULT_GRID_NAME;

    if (!MapSaver.updateMetadata(metadata)){
      log.warning("Failed to save new metadata");
      return Optional.empty();
    }

    return tryLoadMulitGridMap(metadata, false);
  }

  /**
   * Load a multigrid map
   *
   * @param metadata for the map 
   * @param create whether to create the map if it doesn't exist
   */
  private static Optional<MapData> tryLoadMulitGridMap(MapMetadata metadata, boolean create){

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
      return Optional.empty();
    }

    MapData mapData = new MapData(metadata);

    if (!gridFolderValid && create){
      log.info("Grid folder was invalid, and create set -> creating new map" );
      Grid grid = new Grid(AbstractScreen.WORLD_WIDTH, AbstractScreen.WORLD_HEIGHT);
      mapData.registerGrid(MapData.DEFAULT_GRID_NAME, grid);
      mapData.registerStart(MapData.DEFAULT_GRID_NAME);

      if (!MapSaver.saveMap(mapData)){
        log.warning(String.format("Failed to save new map %s", metadata.name));
        return Optional.empty();
      }
      return Optional.of(mapData);
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
      log.severe(
        String.format("Did not find starting grid \"%s\" in %s",
          metadata.startingGrid,
          gridFolder.getAbsolutePath()
        )
      );
    }

    return Optional.of(mapData);
  }

  /**
   * Try to load a map.
   *
   * @param metadata for the map 
   * @param create whether to create the map if it doesn't exist
   *
   * @return {@code Optional.empty()} on failure, {@code Optional.of(MapData)} on success
   */
  public static Optional<MapData> tryLoadMap(MapMetadata metadata, boolean create){
    LoadedObjects.clearUserItems();
    AssetManager.instance().invalidateTextureCache();

    if (!tryLoadTextures(metadata)) return Optional.empty();

    if (!tryLoadObjects(metadata)) return Optional.empty();

    File legacyMapPath = new File(metadata.locations.mapMainFilePath);
    if (legacyMapPath.exists()){
      return tryLoadLegacyMap(metadata);
    } else {
      return tryLoadMulitGridMap(metadata, create);
    }
  }

  /**
   * Pacakge private function to load a grid
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
      log.warning(String.format("Map directory (%s) does not contain map json or a grid directory", mapContentDir.getAbsolutePath()));
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

    return Optional.of(metadata);
  }
}
