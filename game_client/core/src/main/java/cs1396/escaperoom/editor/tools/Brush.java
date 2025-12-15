package cs1396.escaperoom.editor.tools;


import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;

/**
 * Base class for Brushes
 */
public abstract class Brush extends EditorTool {

  /**
   * @param editor on which this brush will operate
   */
  public Brush(LevelEditor editor){
    super(editor);
  }

  @Override
  public void select() {
    CursorManager.setCursor(CursorType.Brush);
    editor.addListener(listener);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(listener);
  }

  private InputListener listener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      return handleTouchDown(x, y);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      handleTouchUp();
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      handleDrag(x, y);
    }
  };


  /**
   * @param x coord in game units where this brush should draw
   * @param y coord in game units where this brush should draw
   * @return the newly drawn item
   */
  abstract public Optional<Item> draw(int x , int y);

  /**
   * @param x touchdown coord
   * @param y touchdown coord
   * @return whether the brush is handling this event
   */
  abstract public boolean handleTouchDown(float x , float y);


  /**
   * Handle a touchup event
   */
  abstract public void handleTouchUp();

  /**
   * @param x drag coord
   * @param y drag coord
   */
  abstract public void handleDrag(float x, float y);

  /**
   * @return item that is drawn with this brush
   */
  abstract public Item getItem();

  /**
   * @return whether this brush is currently drawing
   */
  abstract public boolean isDrawing();

  @Override
  public ToolType getType() {
    return ToolType.Brush;
  }
}
