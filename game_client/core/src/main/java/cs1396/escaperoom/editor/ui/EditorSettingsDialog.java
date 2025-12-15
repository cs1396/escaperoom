package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.ui.SettingsDialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.screens.OnlineMainMenu;
import cs1396.escaperoom.screens.MainMenu;
import cs1396.escaperoom.screens.MapSelectScreen.MapSelectScreenBuilder;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.services.User;

public class EditorSettingsDialog extends SettingsDialog {
  private class MainMenuButton extends G24TextButton {
    public MainMenuButton() {
      super("Main Menu");
      addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScreenManager
            .instance()
            .showScreen(User.isLoggedIn() ? new OnlineMainMenu() : new MainMenu());
        }
      });
    }
  }

  private class MapSelectButton extends G24TextButton {
    public MapSelectButton() {
      super("Map Select");
      addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScreenManager.instance().showScreen(
              new MapSelectScreenBuilder(User.isLoggedIn() ? new OnlineMainMenu() : new MainMenu())
                  .withMaps(MapLoader.discoverMaps())
                  .edit()
                  .play()
                  .delete()
                  .verify()
                  .creation()
                  .build());
        }
      });
    }
  }

  public EditorSettingsDialog() {
    super();
    button(new MainMenuButton());
    button(new MapSelectButton());
  }
}
