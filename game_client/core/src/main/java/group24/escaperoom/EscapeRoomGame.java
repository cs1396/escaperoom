package group24.escaperoom;

import com.badlogic.gdx.Game;

import group24.escaperoom.engine.assets.AssetManager;
import group24.escaperoom.engine.assets.items.ItemLoader;
import group24.escaperoom.engine.control.CursorManager;
import group24.escaperoom.engine.control.CursorManager.CursorType;
import group24.escaperoom.screens.MainMenu;
import group24.escaperoom.screens.utils.ScreenManager;

public class EscapeRoomGame extends Game {
  public void create() {
    ItemLoader.LoadAllObjects();
    AssetManager.instance().finishLoading();
    ScreenManager.instance().initialize(this);
    CursorManager.setCursor(CursorType.Pointer);
    ScreenManager.instance().showScreen(new MainMenu());
  }
}
