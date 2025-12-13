package group24.escaperoom.game.entities.properties.connectors;

import group24.escaperoom.game.entities.Item;

public final class ConnectableItem {
    public final Item item;
    public final Connectable connectable;

    public ConnectableItem(Item item, Connectable connectable) {
      this.item = item;
      this.connectable = connectable;
    }
  }
