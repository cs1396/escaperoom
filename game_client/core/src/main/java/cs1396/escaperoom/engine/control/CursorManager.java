package cs1396.escaperoom.engine.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import cs1396.escaperoom.engine.assets.AssetManager;

public class CursorManager {
  private static CursorType cur;
  public final static CursorType DEFAULT = CursorType.Pointer;

  public enum CursorType {
    Brush("brush_cursor.png", 16,16),
    Delete("delete_cursor.png",0,0),
    Fill("fill_cursor.png", 0,0),
    Hand("hand_cursor.png", 16,16),
    Move("move_cursor.png",0,0),
    Pointer("cursor.png", 0,0),
    Rotate("rotate_cursor.png",0,0),
    Select("select_cursor.png",16,16),
    CopyPaste("copy_cursor.png", 0,0),
    EyeDrop("eyedrop_cursor.png", 0, 31),
    InvalidCopyPaste("invalid_copy_cursor.png", 0,0),
    InvalidMove("invalid_move_cursor.png", 0,0),
    ;

    String asset;
    int hotSpotX, hotSpotY;
    CursorType(String asset, int hotSpotX, int hotSpotY){
      this.asset = "textures/" + asset;
      this.hotSpotX = hotSpotX;
      this.hotSpotY = hotSpotY;
    }
  }

  public static void setCursor(CursorType type){
    cur = type;
    AssetManager.instance().load(type.asset, Pixmap.class);
    AssetManager.instance().finishLoading();
    Pixmap cursorPixmap = AssetManager.instance().get(type.asset);
    Cursor cursor = Gdx.graphics.newCursor(cursorPixmap, type.hotSpotX, type.hotSpotY);
    Gdx.graphics.setCursor(cursor);

  }

  public static InputListener hoverHelper(CursorType type) {
    return new InputListener() {
      boolean dragging = false;

      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return false;
      }

      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (cur == DEFAULT && pointer == -1)
          setCursor(type);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer >= 0)
          dragging = true;

        if (pointer == -1 && !dragging && cur == type) {
          restoreDefault();
        }
      }
    };
  }


  public static void restoreDefault(){
    setCursor(DEFAULT);
  }

}
