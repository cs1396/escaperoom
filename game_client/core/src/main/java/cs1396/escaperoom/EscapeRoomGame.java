package cs1396.escaperoom;

import com.badlogic.gdx.Game;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.items.ItemLoader;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.screens.MainMenu;
import cs1396.escaperoom.screens.utils.ScreenManager;

public class EscapeRoomGame extends Game {
  public void create() {
    ItemLoader.LoadAllObjects();
    AssetManager.instance().finishLoading();
    ScreenManager.instance().initialize(this);
    CursorManager.setCursor(CursorType.Pointer);
    ScreenManager.instance().showScreen(new MainMenu());
  }
}
