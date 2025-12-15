package cs1396.escaperoom.game.entities.properties.values;

import cs1396.escaperoom.game.entities.Item;

public class ContainedItem implements ItemPropertyValue {
  public Item inner;

  public ContainedItem(Item item, Item container) {
    this.inner = item;
  }

  public Item getItem() {
    return inner;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ContainedItem) {
      return ((ContainedItem) obj).getItem().getID() == this.getItem().getID();
    }
    return false;
  }
}

