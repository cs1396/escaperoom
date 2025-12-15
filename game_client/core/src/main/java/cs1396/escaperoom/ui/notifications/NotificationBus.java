package cs1396.escaperoom.ui.notifications;

import java.util.HashSet;

import com.badlogic.gdx.Gdx;

public class NotificationBus {
  private static final NotificationBus inst = new NotificationBus();
  private final HashSet<NotificationListener> listeners  = new HashSet<>();

  public static NotificationBus get() {return inst;}

  public void addListener(NotificationListener listener){
    listeners.add(listener);
  } 

  public void removeListener(NotificationListener listener){
    listeners.remove(listener);
  } 

  public void post(Notification notification){
    Gdx.app.postRunnable(() -> {
      for (NotificationListener l : listeners){
        l.onNotify(notification);
      }
    });
  }

  public interface NotificationListener {
    void onNotify(Notification notification);
  }

}
