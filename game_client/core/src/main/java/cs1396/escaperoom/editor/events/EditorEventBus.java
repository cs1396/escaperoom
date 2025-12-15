package cs1396.escaperoom.editor.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EditorEventBus {
  private static final HashMap<EditorEventListener, EditorEventFilter> listeners  = new HashMap<>();
  private static final HashSet<EditorEventListener> toRemove = new HashSet<>();


  public void addListener(EditorEventListener listener){
    listeners.put(listener, null);
  } 

  public void addListener(EditorEventListener listener, EditorEventFilter filter){
    listeners.put(listener, filter);
  } 

  public void removeListener(EditorEventListener listener){
    // A handler may try and remove itself from our map 
    // -> to avoid concurrent modification, store here and remove later
    toRemove.add(listener);
  } 

  public static void post(EditorEvent event){
    toRemove.forEach((l) -> listeners.remove(l));
    for (Map.Entry<EditorEventListener, EditorEventFilter> pair : listeners.entrySet()){
      EditorEventListener listener = pair.getKey();
      EditorEventFilter filter = pair.getValue();

      if (filter == null || filter.applies(event)){
        listener.handle(event);
      }
    }
  }

  @FunctionalInterface
  public interface EditorEventListener {
    void handle(EditorEvent event);
  }

  @FunctionalInterface
  public interface EditorEventFilter {
    public boolean applies(EditorEvent e);
  }

}
