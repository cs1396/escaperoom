package cs1396.escaperoom.engine.assets.maps;

import cs1396.escaperoom.game.world.Grid;

public class MapData{
  Grid grid;
  MapMetadata metadata;

  public MapData(Grid grid, MapMetadata metadata){
    this.grid = grid;
    this.metadata = metadata;
  }

  public Grid getGrid(){
    return this.grid;
  }
  public MapMetadata getMetadata(){
    return this.metadata;
  }
  
  public void setMetadata(MapMetadata metadata){
    this.metadata = metadata;
  }
}
