package group24.escaperoom.game.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GameEventBus {
  private HashMap<GameEventListener, GameEventFilter> listeners  = new HashMap<>();
  private HashSet<GameEventListener> toRemove = new HashSet<>();

  /**
   * Clear all listeners from the event bus
   */
  public void reset(){
    listeners.clear();
  }

  public void addListener(GameEventListener listener){
    listeners.put(listener, null);
  } 

  public void addListener(GameEventListener listener, GameEventFilter filter){
    listeners.put(listener, filter);
  } 

  public void removeListener(GameEventListener listener){
    // A handler may try and remove itself from our map 
    // -> to avoid concurrent modification, store here and remove later
    toRemove.add(listener);
  } 

  public void post(GameEvent event){
    toRemove.forEach((l) -> listeners.remove(l));
    for (Map.Entry<GameEventListener, GameEventFilter> pair : listeners.entrySet()){
      GameEventListener listener = pair.getKey();
      GameEventFilter filter = pair.getValue();

      if (filter == null || filter.applies(event)){
        listener.handle(event);
      }
    }
  }

  @FunctionalInterface
  public interface GameEventListener {
    void handle(GameEvent event);
  }

  @FunctionalInterface
  public interface GameEventFilter {
    public boolean applies(GameEvent event);
  }

}
