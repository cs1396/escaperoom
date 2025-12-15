package cs1396.escaperoom.screens;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.ui.widgets.G24Label;

public abstract class MenuScreen extends AbstractScreen {
  Texture background;
  Table rootTable;

  public MenuScreen() {
    AssetManager.instance().load("textures/brick.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/brick.png");
    background = AssetManager.instance().get("textures/brick.png", Texture.class);
    addSprite(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    rootTable = new Table();
    rootTable.setFillParent(true);
    addUI(rootTable);
  }

  public <T> void waitFor(CompletableFuture<T> future, CompletionHandler<T> onComplete, Actor loadActor) {
    rootTable.remove();
    addUI(loadActor);

    future.thenAccept((T t) -> {
      Gdx.app.postRunnable(() -> {
        onComplete.handle(t);
        loadActor.remove();
        addUI(rootTable);
      });
    });

  }

  public interface CompletionHandler<T> {
    public void handle(T result);
  }

  /**
   * Given some future, wait for that future off the render thread then run
   * {@code onComplete} on its result on the render thread
   */
  public <T> void waitFor(CompletableFuture<T> future, CompletionHandler<T> onComplete, String loadMessage) {
    G24Label l = new G24Label(loadMessage, "bubble");
    l.setPosition(getUIStage().getWidth() / 2, getUIStage().getHeight() / 2, Align.center);
    l.pack();
    addUI(l);

    l.addAction(Actions.repeat(10, Actions.sequence(
        Actions.delay(0.3f),
        Actions.run(() -> {
          l.setText(loadMessage + ".");
          l.pack();
        }),
        Actions.delay(0.3f),
        Actions.run(() -> {
          l.setText(loadMessage + "..");
          l.pack();
        }),
        Actions.delay(0.3f),
        Actions.run(() -> {
          l.setText(loadMessage + "...");
          l.pack();
        }),
        Actions.delay(0.3f),
        Actions.run(() -> {
          l.setText(loadMessage + "....");
          l.pack();
        }))));

    waitFor(future, onComplete, l);
  }

  /**
   * Given some future, wait for that future off the render thread then run
   * {@code onComplete} on its result on the render thread
   *
   * Displays "Loading" to the user.
   * Optionally, call
   * {@link MenuScreen#waitFor(CompletableFuture, Function, String)} to specify
   * the load message
   */
  public <T> void waitFor(CompletableFuture<T> future, CompletionHandler<T> onComplete) {
    waitFor(future, onComplete, "Loading");

  }
}
