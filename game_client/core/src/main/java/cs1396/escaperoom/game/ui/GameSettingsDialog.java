package cs1396.escaperoom.game.ui;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.screens.GameScreen;
import cs1396.escaperoom.screens.GameSummary;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.SettingsDialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class GameSettingsDialog extends SettingsDialog {
  private class SurrenderButton extends G24TextButton {
    SurrenderButton(GameScreen screen) {
      super("Give Up");
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          screen.calculateStatistics(false);

          ScreenManager
            .instance()
            .showScreen(
              new GameSummary(
                screen.stats,
                screen.getMapData().getMetadata(),
                screen.getGameType()
              )
            );
        }
      });

    }

  }
  public GameSettingsDialog(GameScreen game){
    super();
    button(new SurrenderButton(game));
  }
}
