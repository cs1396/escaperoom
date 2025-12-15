package cs1396.escaperoom.editor.tools;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.core.ActionHistory.EditorAction;
import cs1396.escaperoom.editor.core.DragManager.PlacementAction;
import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.TiledBrushable;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;

public class FillTool extends EditorTool {
  private Brush brush;
  boolean[][] visited = new boolean[AbstractScreen.WORLD_HEIGHT][AbstractScreen.WORLD_WIDTH];

  public FillTool(LevelEditor stage) {
    super(stage);
    resetVisted();
  }

  //----------------------------------------------------------------------------
  // Public API
  //----------------------------------------------------------------------------

  public Brush getBrush(){
    return brush;
  }

  public void setBrush(Brush b) {
    this.brush = b;
  }

  
  //----------------------------------------------------------------------------
  // Fill logic
  //----------------------------------------------------------------------------
  private interface FloodAction {
    public boolean visit(int x, int y);
    public void recordEditorAction();
  }

  private class Fill implements FloodAction {
    Array<PlacementAction> placements = new Array<>();
    public boolean visit(int x, int y){

      // Don't go over a barrier or replace another item 
      // with the same render priority
      Optional<Item[]> oItems = editor.getItemsAt(x, y);
      if (oItems.isPresent()) {
        for (Item i : oItems.get()) {
          if (i.hasProperty(PropertyType.Barrier) || brush.getItem().renderPriority() == i.renderPriority()) {
            return false;
          }
        }
      }

      brush.draw(x, y).ifPresent((i) -> {
        placements.add(new PlacementAction(i, editor));
      });
      return true;
    }
    public void recordEditorAction(){
      editor.recordEditorAction(new FloodFillAction(placements));
    }
  }

  private class Replace implements FloodAction {
    Item target;
    Array<ReplacementAction> replacements = new Array<>();
    public Replace(Item target){
      this.target = target;
    }

    public boolean visit(int x, int y){
      Item toReplace = editor.getItemsAt(x, y).flatMap(items -> {
        for (Item item : items){
          if (item.getItemName().equals(target.getItemName())){
            return Optional.of(item);
          }
        }
        return Optional.empty();
      }).orElse(null);

      if (toReplace == null) return false;

      // remove our old item
      toReplace.remove();

      brush.draw(x, y).ifPresent((i) -> {
        replacements.add(new ReplacementAction(toReplace, i));
      });
      return true;
    }

    public void recordEditorAction(){
      editor.recordEditorAction(new FloodReplaceAction(replacements));
    }
  }


  private void resetVisted() {
    for (int y = 0; y < AbstractScreen.WORLD_HEIGHT; y++) {
      for (int x = 0; x < AbstractScreen.WORLD_WIDTH; x++) {
        visited[y][x] = false;
      }
    }
  }

  private boolean validLocation(int x, int y){
    return !(x < 0 || x >= AbstractScreen.WORLD_WIDTH || y < 0 || y >= AbstractScreen.WORLD_HEIGHT || visited[y][x]);
  }

  private boolean flood(int x, int y, FloodAction action){
    if (!validLocation(x, y))  return false;

    visited[y][x] = true;

    if (!action.visit(x, y)) return false;


    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        if (Math.abs(dy) == Math.abs(dx)) {
          continue;
        }
        flood(x + dx, y + dy, action);
      }
    }
    return true;
  }

  private boolean startFlood(int x, int y) {
    if (!validLocation(x, y)) return false; 

    FloodAction action = editor.getItemsAt(x, y).map(items -> {
      for (Item item : items){
        if (item.renderPriority() == brush.getItem().renderPriority()){
          return new Replace(item);
        }
      }
      return new Fill();
    }).orElse(new Fill());

    if(flood(x, y, action)){
      action.recordEditorAction();
      return true;
    } else {
      return false;
    }

  }

  //----------------------------------------------------------------------------
  // Tool action
  //----------------------------------------------------------------------------

  private class FloodFillAction implements EditorAction {
    private Array<PlacementAction> placements;

    private FloodFillAction(Array<PlacementAction> placements) {
      this.placements = placements;
    }

    @Override
    public void doAction() {
      placements.forEach((pa) -> pa.doAction());
    }

    @Override
    public void undoAction() {
      placements.forEach((pa) -> pa.undoAction());
    }
  }

  private class ReplacementAction implements EditorAction {
    private Item removed;
    private Item added;

    private ReplacementAction(Item removed, Item added) {
      this.removed = removed;
      this.added = added;
    }

    @Override
    public void doAction() {
      removed.remove();
      editor.placeItem(added);
      added.getProperty(PropertyType.TiledBrushable, TiledBrushable.class).ifPresent((tbp) -> {
        tbp.refreshAdjacency(editor);
      });
    }

    @Override
    public void undoAction() {
      added.remove();
      editor.placeItem(removed);
      removed.getProperty(PropertyType.TiledBrushable, TiledBrushable.class).ifPresent((tbp) -> {
        tbp.refreshAdjacency(editor);
      });
    }
  }

  private class FloodReplaceAction implements EditorAction {
    private Array<ReplacementAction> replacements;

    private FloodReplaceAction(Array<ReplacementAction> replacements) {
      this.replacements = replacements;
    }

    @Override
    public void doAction() {
      replacements.forEach((pa) -> pa.doAction());
    }

    @Override
    public void undoAction() {
      replacements.forEach((pa) -> pa.undoAction());
    }
  }



  //---------------------------------------------------------------------------
  // Input handling
  //---------------------------------------------------------------------------

  private InputListener listener = new InputListener() {
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      if (startFlood((int) x, (int) y)){
        return true;
      }
      return false;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      resetVisted();
    }
  };


  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void select() {
    resetVisted();
    CursorManager.setCursor(CursorType.Fill);
    editor.addListener(listener);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(listener);
  }

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------
  
  @Override
  public String getName() {
    return "Fill Tool";
  }

  @Override
  public ToolType getType() {
    return ToolType.Fill;
  }

  @Override
  public  String getButtonStyle(){
    return "fill";
  };
}
