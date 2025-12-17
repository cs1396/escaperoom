package cs1396.escaperoom.screens;

import java.util.Optional;
import java.util.function.Predicate;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.assets.items.ItemLoader.LoadedObjects;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.game.entities.properties.PlayerProperty;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.PlayerDetails;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.ScreenShotter;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class SinglePlayerGame extends GameScreen {

  G24TextButton editButton = new G24TextButton("Back to Editor");

  public SinglePlayerGame(MapData data, boolean verifying){
    super(data);
    applyGrid(data.getStartGrid());
    this.gameType = verifying? GameType.Verifying : GameType.Standard;

    if (verifying){
      actionlog.setVisible(false);
      render(0);
      ScreenShotter.takeScreenShot(data.getMetadata());
      actionlog.setVisible(true);
    }
  }

  public SinglePlayerGame(MapData mapdata) {
    super(mapdata);
    applyGrid(mapdata.getStartGrid());
    this.gameType = GameType.Editor;
  }

  private void applyGrid(Grid grid){
    loadGrid(grid);
    loadPlayer();
  }

  public void loadPlayer() {
    Optional<Item> playerStart = grid.findItemWhere(new Predicate<Item>() {
      @Override
      public boolean test(Item arg0) {
        return arg0.hasProperty(PropertyType.Player);
      }
    });

    Item playerItem = playerStart
                        .orElse(
                          new Item(LoadedObjects.getItem("GameControl", "Mr. E").get()
                        ));

    player.setPosition(playerItem.getX(), playerItem.getY());
    player.setSize(playerItem.getWidth(), playerItem.getHeight());
    playerItem.remove();

    PlayerDetails details = playerItem.getProperty(PropertyType.Player, PlayerProperty.class).get().getDetails();
    player.setTexture(playerItem.getTexture());
    player.renderPriority = playerItem.renderPriority();
    playerId = playerItem.getID();

    followActor = Optional.of(player);
    player.setDetails(details);
  }

  @Override
  protected void init() {
    editButton.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        if (editButton.isChecked()) {
          MapLoader.tryLoadMap(mapData.getMetadata())
            .inspect(
              g -> ScreenManager.instance().showScreen(new LevelEditor(g))
            )
            .inspect_err(
              e -> Notifier.error(e.reason())
            );
        }
      }
    });

    if (gameType == GameType.Editor) {
      rootTable.add(editButton).right().bottom().expandX();
    }
  }

  @Override
  public void render(float delta) {
    super.render(delta);
    player.stepAnimation(delta);
  }


  public void completeLevel(boolean success) {
    calculateStatistics(success);
    ScreenManager.instance().showScreen(new GameSummary(stats, mapData.getMetadata(), gameType));
  }
}
