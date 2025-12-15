package cs1396.escaperoom.game.ui;

import java.util.logging.Logger;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.ui.widgets.G24Dialog;

public class PlayerInventoryDialog extends G24Dialog {
  Logger log = Logger.getLogger(PlayerInventoryDialog.class.getName());
  Player player;

  public PlayerInventoryDialog(Player player) {
    super("Player Inventory");
    this.player = player;
    Array<Item> inventory;
    inventory = player.getInventory();
    populateContentTable(inventory);
    button(new PlayerInventoryCloseButton("Close Inventory", player));
    setModal(false);
  }

  public void inventoryChanged() {
    getContentTable().clear();
    populateContentTable(player.getInventory());
    setPosition(Math.round((player.getGameScreen().getUIStage().getWidth() - getWidth()) / 2), 10);
  }

  @Override
  public Dialog show(Stage stage) {
		show(stage, Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
    setPosition(Math.round((player.getGameScreen().getUIStage().getWidth() - getWidth()) / 2), 10);
    BackManager.addBack(() -> {
      player.setInventoryOpen(false);
      hide();
    });

    return this;
  }

  private void populateContentTable(Array<Item> inventory) {
    Table tableOfItems = new Table(AbstractScreen.skin);

    tableOfItems.padLeft(10);
    tableOfItems.padRight(10);

    for (int k = 0; k < inventory.size; k++) {
      tableOfItems.add(new PlayerInventoryItemSlot(inventory.get(k), player));
    }
    // Add a single empty slot
    tableOfItems.add(new PlayerInventoryItemSlot(player)); 

    tableOfItems.pack();

    getContentTable().add(tableOfItems);
    getContentTable().pack();
    pack();
  }
}
