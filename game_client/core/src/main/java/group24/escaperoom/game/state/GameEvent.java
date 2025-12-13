package group24.escaperoom.game.state;

import com.badlogic.gdx.utils.Null;

import group24.escaperoom.game.entities.Item;

public class GameEvent {

  public enum EventType {
    ItemObtained,      // Item obtained by player
    ItemStateChange,   // General state change
    ItemConnected,     // Connectable item became connected
    ItemDisconnected,  // Connectable item became disconnected
  }

  public final GameContext ctx;
  public final EventType type;
  public final @Null Item source;
  public final @Null Item target;
  public final @Null String message;

  @Override
  public String toString() {
    if (message != null){
      return message;
    }

    switch (type){
		case ItemObtained:
      return "Added " + source.getItemName() + " to inventory";
		default:
      return "";
    }
  }


  private GameEvent(EventType type, GameContext ctx, @Null Item source, @Null Item target, @Null String message){
    this.type = type;
    this.ctx = ctx;
    this.source = source;
    this.target = target;
    this.message = message;
  }

  public static class Builder {
    GameContext ctx;
    EventType type;
    @Null Item source;
    @Null Item target;
    String message = "";

    public Builder(EventType eventType, GameContext context){
      type = eventType;
      ctx = context;
    }

    public Builder target(Item targetItem){
      target = targetItem;
      return this;
    }

    public Builder message(String eventMessage){
      message = eventMessage;
      return this;
    }

    public Builder source(Item sourceItem){
      source = sourceItem;
      return this;
    }

    public GameEvent build(){
      return new GameEvent(type, ctx, source, target, message);
    }

  }
}
