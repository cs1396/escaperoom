package cs1396.escaperoom.screens.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ScreenUtils;

import cs1396.escaperoom.EscapeRoomGame;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.GameScreen;
import cs1396.escaperoom.screens.LevelEditor;

public class ScreenManager {
  private static ScreenManager instance = new ScreenManager();
  private ScreenType type;

  public enum ScreenType {
    Game,
    Editor,
    Menu,
  }

  private EscapeRoomGame game;

  public static ScreenManager instance() {
    return instance;
  }

  public void initialize(EscapeRoomGame game) {
    this.game = game;
  }

  public ScreenType getCurrentScreenType() {
    return type;
  }

  public AbstractScreen getCurrent() {
    return AbstractScreen.class.cast(game.getScreen());
  }
  public void showScreen(AbstractScreen nextScreen, boolean dispose) {
    if (nextScreen instanceof GameScreen) {
      type = ScreenType.Game;
    } else if (nextScreen instanceof LevelEditor) {
      type = ScreenType.Editor;
    } else {
      type = ScreenType.Menu;
    }

    Screen screen = game.getScreen();

    BackManager.setOwner(null);

    if (screen == null){
      game.setScreen(nextScreen);
      return;
    }

    // Heavily inspired by https://github.com/digital-thinking/libgdx-transitions -- to be fair the README says "Or just use the source"
    FrameBuffer currentFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
        false);
    FrameBuffer nextFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
        false);

    float transitionDuration = 0.1f;
    float time = 0.0f;
    Batch batch = new SpriteBatch();
    while (time < transitionDuration) {
      ScreenUtils.clear(0,0,0,1);
      float delta = Gdx.graphics.getDeltaTime();

      currentFB.begin();
      screen.render(delta);
      currentFB.end();

      nextFB.begin();
      nextScreen.render(delta);
      nextFB.end();

      Texture currentTexture = currentFB.getColorBufferTexture();
      Texture nextTexture = nextFB.getColorBufferTexture();

      float alpha = Interpolation.fade.apply(time / transitionDuration);

      batch.begin();
      batch.setColor(1, 1, 1, alpha);
      batch.draw(currentTexture, 0, 0, 0, 0, currentTexture.getWidth(), currentTexture.getHeight(), 1, 1, 0, 0,
          0, currentTexture.getWidth(), currentTexture.getHeight(), false, true);
      batch.setColor(1, 1, 1, 1 - alpha);
      batch.draw(nextTexture, 0, 0, 0, 0, nextFB.getWidth(), nextFB.getHeight(), 1, 1, 0, 0, 0,
          nextTexture.getWidth(), nextFB.getHeight(), false, true);
      batch.end();

      time += delta;
    }

    if (dispose) screen.dispose();

    BackManager.setOwner(nextScreen);
    game.setScreen(nextScreen);
  }

  public void showScreen(AbstractScreen nextScreen) {
    showScreen(nextScreen, true);
  }
}
