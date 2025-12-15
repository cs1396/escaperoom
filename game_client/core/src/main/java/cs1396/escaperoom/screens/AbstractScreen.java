package cs1396.escaperoom.screens;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.game.state.Pollable;
import cs1396.escaperoom.ui.notifications.NotificationBus;
import cs1396.escaperoom.ui.notifications.NotificationOverlay;

/**
 * Abstract class from which all screens extend
 */
public abstract class AbstractScreen extends Stage implements Screen {

  /**
   * Render constants
   */
  public static final int SCREEN_WIDTH = 1920,
                          SCREEN_HEIGHT = 1080,
                          WORLD_WIDTH = 48,
                          WORLD_HEIGHT = 27;

  /**
   * Skin used for all UI elements
   */
  public static final Skin skin = new Skin(Gdx.files.internal("cs1396/skin/skin.json"));

  /**
   * Input processors (e.g. the uiStage, the mainStage)
   */
  protected InputMultiplexer inputPlex; 

  /**
   * For drawing background textures outside the game world
   */
  protected FillViewport fillView; 
  
  private NotificationOverlay overlay;

  /**
   * @return the notification overlay for this screen
   */
  public NotificationOverlay getNotificationOverlay(){
    return overlay;
  }

  /**
   * We must have a separate stage for all UI
   *
   * This stage must have a {@link ScreenViewport} because all ui elements in
   * {@link com.badlogic.gdx.scenes.scene2d.ui}
   * are defined in *pixels*, this does not mesh with our gameworld unit.
   */
  private Stage uiStage;

  /**
   * An actor which our main camera will follow
   */
  protected Optional<Actor> followActor;

  private final HashSet<Pollable> pollables = new HashSet<>();

  /**
   * The batch used for drawing sprites
   */
  protected final SpriteBatch spriteBatch;

  /**
   * The sprites to draw during {@link AbstractScreen#render(float)}
   */
  protected final HashSet<RenderSprite> sprites = new HashSet<>();

  private class RenderSprite {
    private Texture t;
    private float x, y, w, h;
    RenderSprite(Texture t, float x, float y, float w, float h) {
      this.t = t;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
    private void draw(SpriteBatch b) {
      b.draw(t, x, y, w, h);
    }
  }

  /**
   * 
   */
  protected AbstractScreen() {
    super(new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, new OrthographicCamera()));
    uiStage = new Stage(new ScreenViewport());
    overlay = new NotificationOverlay();
    uiStage.addActor(overlay);
    fillView = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, new OrthographicCamera());

    inputPlex = new InputMultiplexer();
    spriteBatch = new SpriteBatch();
    followActor = Optional.empty();

    // PERF:
    // Right now we will just block until all assets are loaded
    AssetManager.instance().update();
    AssetManager.instance().finishLoading();

    ControlsManager.clearRegisteredInputs();
    BackManager.clearActions();
    BackManager.setOwner(this);
  }

  /**
   * This will be called when the scene is shown
   *
   * Use this function to add any UI or actors to the scene
   *
   * Note that ui should use super.uiStage and other actors
   * can be added directly with addActor
   */
  protected void init() { };

  @Override
  public void resize(int width, int height) {
    getViewport().update(width, height);
    uiStage.getViewport().update(width, height, true);
    fillView.update(width, height);
  }

  @Override
  public void act(float delta) {
    super.act(delta);

    ControlsManager.processInputs();

    HashSet<Pollable> toRemove = new HashSet<>();
    pollables.forEach((p) -> {
      if (!p.poll(delta)){
        toRemove.add(p);
      }
    });
    pollables.removeAll(toRemove);
  }

  public void addPollable(Pollable pollable){
    pollables.add(pollable);
  }

  public void removePollable(Pollable pollable){
    pollables.remove(pollable);
  }

  protected void addSprite(Texture t, float x, float y, float width, float height) {
    sprites.add(new RenderSprite(t, x, y, width, height));
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0,0,0,1, true);
    // apply the extended viewport for any sprites or drawn textures (the world)
    spriteBatch.setProjectionMatrix(fillView.getCamera().combined);
    fillView.apply(true);

    if (!sprites.isEmpty()) {
      spriteBatch.begin();
      sprites.forEach(s -> {
        s.draw(spriteBatch);
      });
      spriteBatch.end();
    }

    // draw any actors
    getViewport().apply();
    act(delta);
    followActor.ifPresent((actor) -> {
      getViewport().getCamera().position.set(actor.getX(Align.center), actor.getY(Align.center),
          getViewport().getCamera().position.z);
    });
    draw();

    // draw any UI
    uiStage.getViewport().apply(true);
    uiStage.act(delta);
    uiStage.draw();
  }

  /**
   * Add UI to the {@link AbstractScreen#uiStage}
   *
   * @param actor the actor to be addd to the ui stage
   */
  public void addUI(Actor actor) {
    uiStage.addActor(actor);
    overlay.toFront();
  }

  /**
   * Add some number of input processors to screen
   *
   * @param processors {@link InputProcessor} to be added
   *
   */
  protected void addInputProcessor(InputProcessor... processors) {
    for (InputProcessor proc : processors) {
      inputPlex.addProcessor(proc);
    }
  }

  /**
   * @return the stage that all UI should be rendered on
   */
  public Stage getUIStage() {
    return uiStage;
  }

  @Override
  public void show() {
    init();
    // The input multiplexer processes things in order, so we want input to first go
    // to the uiStage
    inputPlex.addProcessor(uiStage);
    inputPlex.addProcessor(this);
    Gdx.input.setInputProcessor(inputPlex);
  }

  @Override
  public void hide() { }

  @Override
  public void pause() { }

  @Override
  public void resume() { }

  /**
   * @param uiCoords a vector in UI coordinates
   * @return that vector in game coordinates
   *
   */
  public Vector2 UIcoordToGame(Vector2 uiCoords) {
    return screenToStageCoordinates(uiStage.stageToScreenCoordinates(uiCoords));
  }

  /**
   * @param gameCoords A vector in game coordinates
   * @return that vector in ui coordinates
   *
   */
  public Vector2 gameCoordToUI(Vector2 gameCoords) {
    return uiStage.screenToStageCoordinates(stageToScreenCoordinates(gameCoords));
  }

  @Override
  public void dispose(){
    super.dispose();
    uiStage.dispose();
    NotificationBus.get().removeListener(overlay);
  }
}
