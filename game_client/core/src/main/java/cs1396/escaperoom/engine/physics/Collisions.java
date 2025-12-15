package cs1396.escaperoom.engine.physics;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Collisions {
    public static boolean collides(Circle c, Rectangle r) {
        float DeltaX = c.x - Math.max(r.x, Math.min(c.x, r.x + r.width));
        float DeltaY = c.y - Math.max(r.y, Math.min(c.y, r.y + r.height));
        return (DeltaX * DeltaX + DeltaY * DeltaY) < (c.radius * c.radius);
    }

    public static Vector2 getNearestPoint(Rectangle target, Vector2 origin) {
        if (target.contains(origin)) {
            Vector2 _void = new Vector2();
            Vector2 pos = target.getCenter(_void);
            Vector2 bounds = target.getSize(_void).scl(.5f);

            Vector2 deltaToPositiveBounds = pos.add(bounds).sub(origin);
            Vector2 deltaToNegativeBounds = pos.sub(bounds).sub(origin).scl(-1);

            float smallestX = Math.min(deltaToPositiveBounds.x, deltaToNegativeBounds.x);
            float smallestY = Math.min(deltaToPositiveBounds.y, deltaToNegativeBounds.y);

            float smallestDistance = Math.min(smallestX, smallestY);

            if (smallestDistance == deltaToPositiveBounds.x){
                return new Vector2(pos.x + bounds.x, origin.y);

            } else if (smallestDistance == deltaToNegativeBounds.x){
                return new Vector2(pos.x - bounds.x, origin.y);

            } else if (smallestDistance == deltaToPositiveBounds.y){
                return new Vector2(origin.x, pos.y + bounds.y);

            } else {
                return new Vector2(origin.x, pos.y - bounds.y);
            }
        } else {
            return new Vector2(
                    clamp(origin.x, target.getX(), target.getX() + target.getWidth()),
                    clamp(origin.y, target.getY(), target.getY() + target.getHeight())
                );
        }
    }

    // NOTE: while Math.clamp seems to exist, it cannot be found by the compiler.
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

}
