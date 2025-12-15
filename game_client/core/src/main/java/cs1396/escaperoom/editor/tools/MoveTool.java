package cs1396.escaperoom.editor.tools;

import java.util.Optional;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.editor.core.ActionHistory.EditorAction;
import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.editor.tools.SelectionTool.Selection;

public class MoveTool extends EditorTool {

  // The touchdown point of a valid touchdown (on a select region or object)
  IntVector2 startPoint; 

  // Some inner tool which implements MovesItems
  Optional<MovesItems> activeTool;

  public MoveTool(Stage uiStage, LevelEditor editor) {
    super(editor);
    this.activeTool = Optional.empty();
  }

  //----------------------------------------------------------------------------
  // Movement interface
  //
  // Used for moving single or multiple items
  //----------------------------------------------------------------------------

  private interface MovesItems {
    /**
     * Coordinates are given relative to game stage
     */
    public void handleDrag(float x, float y);

    /**
     * Coordinates are given relative to game stage
     */
    public void handleUp(float x, float y);

    /**
     * Called when this tool is canceled
     */
    public void cancel();
  }

  //----------------------------------------------------------------------------
  // Move tools
  //----------------------------------------------------------------------------
  private class BulkMoveTool implements MovesItems {
    Array<MovingItem> items = new Array<>();
    Selection selection;
    Vector2 selectionOffset;

    public BulkMoveTool(Selection activeSelection) {
      this.selection = activeSelection;
      Array<Item> selection = activeSelection.getItems();
      Rectangle selectionArea = this.selection.getArea();
      selectionOffset = new Vector2(selectionArea.getX() - startPoint.x, selectionArea.getY() - startPoint.y);

      for (Item i : selection) {
        i.setAlpha(0.5f);
        this.items.add(new MovingItem(i, i.getX() - startPoint.x, i.getY() - startPoint.y));
      }
    }

    //-------------------------------------------------------------------------
    // Input handling
    //-------------------------------------------------------------------------
    
    @Override
    public void handleDrag(float x, float y) {
      Vector2 newPos = new Vector2(x, y);

      // remove all the selected items from the grid so we can "overlap ourselves"
      for (MovingItem i : items) {
        editor.grid.removeItem(i.item);
      }

      boolean illegalMove = false;
      // check if we can place every item legally
      for (MovingItem i : items) {
        if (!editor.canPlace(i.item, i.newPositionFor(newPos))) {
          CursorManager.setCursor(CursorType.InvalidMove);
          i.item.setColor(1, 0.5f, 0.5f, 0.5f);
          illegalMove = true;
        } else {
          i.item.setColor(1, 1, 1, 0.5f);
        }
      }

      if (illegalMove){
        // if we can't replace the items on the grid and bail
        for (MovingItem item : items) {
          editor.grid.placeItem(item.item);
        }
        return;
      }

      CursorManager.setCursor(CursorType.Move);

      // update their positions
      for (MovingItem i : items) {
        IntVector2 newPosI = i.newPositionFor(newPos);
        i.item.moveTo(newPosI.x, newPosI.y);
        i.item.setColor(1, 1, 1, 0.5f);
      }

      // add them back to the grid
      for (MovingItem item : items) {
        editor.grid.placeItem(item.item);
      }

      // update the position of the selection
      Vector2 selectionPt = new Vector2(MathUtils.floor(newPos.x + selectionOffset.x),
          MathUtils.floor(newPos.y + selectionOffset.y));
      selection.getArea().setPosition(selectionPt.x, selectionPt.y);

    }

    @Override
    public void handleUp(float x, float y) {
      for (MovingItem i : items) {
        i.pos.finalPosition = i.item.getPosition();
        i.item.setColor(1,1,1,1);
      }
      editor.recordEditorAction(new BulkMoveAction(items));
      this.items = new Array<>();
    }

    //-------------------------------------------------------------------------
    // Tool lifecycle
    //-------------------------------------------------------------------------

    @Override
    public void cancel() {
      for (MovingItem i : items) {
        i.returnToWhenceYouCame();
      }
    }

    // Inner class bundling position information
    // with the item that is being moved
    class MovingItem {
      Item item;
      PositionInfo pos;
      Vector2 offsets;

      public MovingItem(Item i, float offsetX, float offsetY) {
        this.item = i;
        this.pos = new PositionInfo();
        this.offsets = new Vector2(offsetX, offsetY);
        pos.originalPosition = new IntVector2(MathUtils.floor(i.getX()), MathUtils.floor(i.getY()));
      }

      public IntVector2 newPositionFor(Vector2 point) {
        return new IntVector2(MathUtils.floor(point.x + this.offsets.x), MathUtils.floor(point.y + this.offsets.y));
      }

      public void returnToWhenceYouCame() {
        item.moveTo(pos.originalPosition.x, pos.originalPosition.y);
        item.setAlpha(1);
      }
    }

    // Bundle of original and new positions
    class PositionInfo {
      IntVector2 originalPosition;
      IntVector2 finalPosition;
    }

    private class BulkMoveAction implements EditorAction {
      private Array<MovingItem> items;

      public BulkMoveAction(Array<MovingItem> items) {
        this.items = items;
      }

      @Override
      public void doAction() {
        for (MovingItem info : items) {
          info.item.moveTo(info.pos.finalPosition.x, info.pos.finalPosition.y);
        }
      }

      @Override
      public void undoAction() {
        for (MovingItem info : items) {
          info.item.moveTo(info.pos.originalPosition.x, info.pos.originalPosition.y);
        }
      }
    }
  }

  public static class SoloMoveTool implements MovesItems {
    IntVector2 originalPosition;
    IntVector2 finalPosition;
    LevelEditor editor;
    Item target;

    public SoloMoveTool(Item target, LevelEditor editor) {
      this.editor = editor;
      this.target = target;
      originalPosition = target.getPosition().cpy();
    }

    //-------------------------------------------------------------------------
    // Public API 
    //-------------------------------------------------------------------------

    public Item getItem() {
      return this.target;
    }

    //-------------------------------------------------------------------------
    // Input handling
    //-------------------------------------------------------------------------

    @Override
    public void handleDrag(float x, float y) {
      IntVector2 newPos = new IntVector2(x,y);

      if (newPos.equals(target.getPosition())) return;

      target.setAlpha(0.5f);
      target.remove(true);
      if (editor.canPlace(target, newPos)) {
        CursorManager.setCursor(CursorType.Move);
        editor.placeItem(target);
        target.moveTo(newPos.x, newPos.y);
        target.setColor(1, 1, 1, 1);
      } else {
        CursorManager.setCursor(CursorType.InvalidMove);
        target.setColor(1, 0.5f, 0.5f, 0.5f);
        editor.placeItem(target);
      }
    }

    @Override
    public void handleUp(float x, float y) {
      finalPosition = target.getPosition().cpy();
      target.setColor(1,1,1,1);
      editor.recordEditorAction(new MoveAction());
    }

    //-------------------------------------------------------------------------
    // Tool lifecycle
    //-------------------------------------------------------------------------

    @Override
    public void cancel() {
      target.moveTo(originalPosition.x, originalPosition.y);
      target.setAlpha(1);
    }

    private class MoveAction implements EditorAction {
      @Override
      public void doAction() {
        target.moveTo(finalPosition.x, finalPosition.y);
      }

      @Override
      public void undoAction() {
        target.moveTo(originalPosition.x, originalPosition.y);
      }
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void select() {
    editor.addListener(moveListener);
    CursorManager.setCursor(CursorType.Move);
  }

  @Override
  public void cancel() {
    editor.removeListener(moveListener);
    activeTool.ifPresent((t) -> t.cancel());
    activeTool = Optional.empty();
    CursorManager.restoreDefault();
  }


  //---------------------------------------------------------------------------
  // Input handling
  //---------------------------------------------------------------------------

  private InputListener moveListener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      startPoint = new IntVector2(x, y);

      Optional<Selection> selection = editor.getSelectionTool().getSelection();
      if (selection.isPresent() && selection.get().getArea().contains(startPoint.asVector2())) {
        activeTool = Optional.of(new BulkMoveTool(selection.get()));
        return true;
      }

      // See if this touchdown is on a tile with items
      Optional<Item> oTarget = editor.priorityItemAt(startPoint.x, startPoint.y);
      if (oTarget.isPresent()){
        activeTool = Optional.of(new SoloMoveTool(oTarget.get(), editor));
        return true;
      }

      return false;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      activeTool.ifPresent((t) -> t.handleDrag(x, y));
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      activeTool.ifPresent((t) -> t.handleUp(x, y));
      activeTool = Optional.empty();
    }
  };

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------

  @Override
  public String getName() {
    return "Move";
  }

  @Override
  public String getButtonStyle() {
    return "moveButton";
  }

  @Override
  public ToolType getType() {
    return ToolType.Move;
  }

}
