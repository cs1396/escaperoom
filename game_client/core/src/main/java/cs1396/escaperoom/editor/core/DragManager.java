package cs1396.escaperoom.editor.core;

import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;

import cs1396.escaperoom.editor.core.ActionHistory.EditorAction;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.screens.MapScreen;

public class DragManager {
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(DragManager.class.getName());
  private final DragAndDrop dragAndDrop;
  private Item inFlightItem = null;
  private final LevelEditor screen;
  private final Actor proxy;

  public DragAndDrop getDragAndDrop() {
    return dragAndDrop;
  }

  public Item getInFlightItem() {
    return inFlightItem;
  }

  public DragManager(LevelEditor screen, Actor roomProxy) {
    this.screen = screen;
    this.dragAndDrop = new DragAndDrop();
    this.proxy = roomProxy;
    dragAndDrop.addTarget(new EditorTarget(proxy));
  }

  private class EditorTarget extends DragAndDrop.Target {
    public EditorTarget(Actor target) {
      super(target);
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
      // Calculate game coordinates
      IntVector2 gameCoords = IntVector2.fromVector2(
          screen.getViewport().unproject(
              new Vector2(
                  Gdx.input.getX(),
                  Gdx.input.getY())));

      // Ensure in bounds
      if (!gameCoords.contained(0, 0, screen.grid.getWidth() - 1, screen.grid.getHeight() - 1))
        return false;

      // Initialze the item if needed
      if (inFlightItem == null) {
        inFlightItem = ((Item) payload.getObject()).clone();
        inFlightItem.setAlpha(0.75f);
      }

      if (screen.canPlace(inFlightItem, gameCoords)) {
        if (screen.placeItem(inFlightItem)) {
          screen.recordEditorAction(new PlacementAction(inFlightItem, screen));
        }

        if (inFlightItem.getPosition().equals(gameCoords)) {
          return true;
        }
        // Set position
        inFlightItem.moveTo(gameCoords.x, gameCoords.y);
        return true;
      }
      return false;
    }

    @Override
    public void reset(Source source, Payload payload) {
      if (inFlightItem == null)
        return;

      inFlightItem.setAlpha(1);
      inFlightItem.setContained(false);
      inFlightItem.remove();
      inFlightItem = null;
      CursorManager.restoreDefault();
    }

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
      if (inFlightItem == null)
        return;

      inFlightItem.setAlpha(1);
      inFlightItem = null;
    }
  }

  /**
   * Represents a placement operation
   */
  public static class PlacementAction implements EditorAction {
    final Item obj;
    final MapScreen screen;

    public PlacementAction(Item obj, MapScreen screen) {
      this.obj = obj;
      this.screen = screen;
    }

    @Override
    public void doAction() {
      screen.placeItem(obj);
    }

    @Override
    public void undoAction() {
      obj.remove();
    }
  }
}
