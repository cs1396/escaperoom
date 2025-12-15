package cs1396.escaperoom.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.game.entities.properties.ContainsItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ContainedItem;
import cs1396.escaperoom.screens.AbstractScreen;

public class ContainerUI extends Table {
  final static int ROWS = 2;
  final static int COLS = 4;
  private ContainsItemProperty innerProp;

  public ContainerUI(ContainsItemProperty containerProperty, Player player) {
    super(AbstractScreen.skin);
    this.innerProp = containerProperty;
    Array<ContainedItem> items = this.innerProp.getCurrentValues();
    for (int j = 0; j < ROWS; j++) {
      for (int i = 0; i < COLS; i++) {
        int ind = j * ROWS + i;
        if (ind >= items.size) {
          add(new ContainerItemSlot(player, containerProperty));
        } else {
          Item item = items.get(ind).getItem();
          add(new ContainerItemSlot(item, player, containerProperty));
        }
      }
      row();
    }
    row();
  }
}
