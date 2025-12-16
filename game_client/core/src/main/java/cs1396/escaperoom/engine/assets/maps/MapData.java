package cs1396.escaperoom.engine.assets.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.game.world.Grid;

public class MapData {

  private static Logger log = Logger.getLogger(MapData.class.getName());

  // Default name of a grid
  public static final String DEFAULT_GRID_NAME = "Room 1";

  // Bundle a grid, it's name, and whether it is loaded 
  final class GridWrapper{
    String name;
    boolean loaded;
    Grid grid;

    public GridWrapper(String name, boolean loaded, Grid grid){
      this.name = name;
      this.loaded = loaded;
      this.grid = grid;
    }
  }

  // Map grid names to the GridWrapper
  Map<String, GridWrapper> stages = new HashMap<>();
  MapMetadata metadata;

  /**
   * @return all registered grid names
   */
  public Collection<String> getGridNames(){
    return stages.keySet();
  }

  public MapData(MapMetadata metadata){
    this.metadata = metadata;
  }

  /**
   * Set the starting grid
   *
   * @throws IllegalStateException if the grid name is not registered
   */
  public MapData registerStart(String gridName){
    if (!stages.containsKey(gridName)){
      throw new IllegalStateException("Cannot register start for nonexistent stage");
    }
    metadata.startingGrid = gridName;
    return this;
  }

  /**
   * Rename a grid stage
   *
   * @param oldName prior grid name
   * @param newName new grid name
   *
   * @throws IllegalStateException if the old name is not registered or if the new name already exists
   */
  public MapData renameStage(String oldName, String newName){
    if (!stages.containsKey(oldName)){
      throw 
        new IllegalStateException(
          String.format("Cannot rename grid \"%s\" since it is not registered", oldName)
        );
    }

    if (stages.containsKey(newName)){
      throw 
        new IllegalStateException(
          String.format(
            "Cannot rename grid \"%s\" to \"%s\" since that name already is registered",
            oldName,
            newName
          )
        );
    }

    stages.put(newName, stages.remove(oldName));

    return this;
  }
  
  /**
   * Register a grid by name, witout loading that grid
   */
  MapData registerGrid(String name){ return registerGrid(name, null); }

  /**
   * Register a grid.
   *
   * @param name to register by
   * @param grid the grid. can be null to lazy load this grid by name.
   *
   * @see MapData#registerGrid(String)
   */
  public MapData registerGrid(String name, @Null Grid grid){

    if (stages.containsKey(name)){
      throw 
        new IllegalStateException(
          String.format(
            "Cannot register grid \"%s\" since that name already is registered",
            name
          )
        );
    }

    GridWrapper wrapper = new GridWrapper(name, grid != null, grid);
    stages.put(name, wrapper);
    return this;
  }

  /**
   * Get the first grid of the map
   */
  public Grid getStartGrid(){ return getGrid(metadata.startingGrid); }

  /**
   * Get a grid by it's name
   */
  public Grid getGrid(String name){
    GridWrapper wrapper = this.stages.get(name);
    if (wrapper == null) {
      throw new IllegalStateException("Asked for unregistered grid: " + name);
    }

    if (!wrapper.loaded){
      wrapper.grid = MapLoader.loadGrid(name, metadata).orElse(null);
    } 

    if (wrapper.grid == null){
      log.severe("Failed to load requested grid: " + name);
    } else {
      wrapper.loaded = true;
    }

    return wrapper.grid;
  }

  public MapMetadata getMetadata(){
    return this.metadata;
  }
  
  // TODO: Make sure this interacts correctly with new metadata system
  public void setMetadata(MapMetadata metadata){
    this.metadata = metadata;
  }
}
