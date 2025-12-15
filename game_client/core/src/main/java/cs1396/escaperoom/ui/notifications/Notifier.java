package cs1396.escaperoom.ui.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class Notifier {
    public static void info(String msg) { post(Notification.Type.INFO, msg, null, false); }
    public static void info(String msg, Actor source) { post(Notification.Type.INFO, msg, source, false); }

    public static void warn(String msg) { post(Notification.Type.WARNING, msg, null, false); }
    public static void warn(String msg, Actor source) { post(Notification.Type.WARNING, msg, source, false); }

    public static void error(String msg) { post(Notification.Type.ERROR, msg, null, false); }
    public static void error(String msg, Actor source) { post(Notification.Type.ERROR, msg, source, false); }

    private static void post(Notification.Type type, String msg, Actor source, boolean persistent) {
        NotificationBus.get().post(new Notification(type, msg, source, persistent, 3f));
    }
}

