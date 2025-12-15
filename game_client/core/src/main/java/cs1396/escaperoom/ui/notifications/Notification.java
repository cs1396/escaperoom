package cs1396.escaperoom.ui.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class Notification {
  public enum Type {
    INFO, WARNING, ERROR, SUCCESS
  }

  private final Type type;
  private final String message;
  private final Actor source;
  private final float duration;
  private final boolean persistent;
  private final long timestamp;

  public Notification(Type type, String message) {
    this(type, message, null, false, 3f);
  }

  public Notification(Type type, String message, Actor source, boolean persistent, float duration) {
    this.type = type;
    this.message = message;
    this.source = source;
    this.persistent = persistent;
    this.duration = duration;
    this.timestamp = System.currentTimeMillis();
  }

  public Type getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public Actor getSource() {
    return source;
  }

  public float getDuration() {
    return duration;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean isPersistent() {
    return persistent;
  }

}
