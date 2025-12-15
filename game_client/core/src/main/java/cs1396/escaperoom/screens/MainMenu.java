package cs1396.escaperoom.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.screens.MapSelectScreen.MapSelectScreenBuilder;
import cs1396.escaperoom.screens.utils.ScreenManager;

public class MainMenu extends MenuScreen {
  G24TextButton loginButton;
  private ChangeListener onLogin = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(new Login());
    }
  };

  G24TextButton myMapsButton;
  private ChangeListener onMyMaps = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(
          new MapSelectScreenBuilder(new MainMenu())
            .withMaps(MapLoader.discoverMaps())
            .edit()
            .play()
            .delete()
            .creation()
            .build());
    }
  };

  G24TextButton exitButton;
  private ChangeListener onExit = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      Gdx.app.exit();
    }
  };

  @Override
  public void init() {
    exitButton = new G24TextButton("Exit");
    exitButton.addListener(onExit);
    loginButton = new G24TextButton("Login");
    loginButton.addListener(onLogin);
    myMapsButton = new G24TextButton("My Maps");
    myMapsButton.addListener(onMyMaps);
    buildUI();
  }

  public void buildUI() {
    rootTable.defaults().pad(10);

    Image title_img = new Image(AssetManager.instance().loadTextureBlocking("textures/title.png"));
    title_img.setScaling(Scaling.contain);

    rootTable.add(title_img).center().padLeft(20).padRight(20).padBottom(0);
    rootTable.row();
    rootTable.add(loginButton).minWidth(300);
    rootTable.row();
    rootTable.add(myMapsButton).minWidth(300);
    rootTable.row();
    rootTable.add(exitButton).minWidth(300);
    rootTable.setFillParent(true);
  }
}
