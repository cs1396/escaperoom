package cs1396.escaperoom.screens.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * A manager for your camera
 */
public class CamMan {
  private OrthographicCamera cam;
  

  final static float MAX_ZOOM = 8f;
  final static float MIN_ZOOM = 0.2f;
  final static float ZOOM_INCR = 0.03f;
  final static Interpolation transInterpolation = Interpolation.pow2Out;

  /**
   * @param camera to manage
   */
  public CamMan(OrthographicCamera camera){
    cam = camera;
  }

  /**
   * Camera translations directions
   */
  public enum Translation {
    Up,
    Down,
    Right,
    Left
  }

  /**
   * @param x coord
   * @param y coord
   *
   * @return the unprojected vector
   */
  public Vector2 unproject(float x, float y){
    Vector3 unproj = cam.unproject(new Vector3(x, y, 0));
    return new Vector2(unproj.x, unproj.y);
  }


  private float translationFactor(){
    float zoomProgress = MathUtils.map(MIN_ZOOM, MAX_ZOOM, 0f, 1f, cam.zoom);
    float transProgress = transInterpolation.apply(zoomProgress);
    return transProgress;
  }

  private float translationScalar(){
    float translationScalar =  MathUtils.map(0f, 1f, 0.03f, 1f, translationFactor());
    return translationScalar;
  }

  /**
   * Set the position of the camera absolutely
   *
   * @param dx amount 
   * @param dy amount 
   */
  public void translate(float dx, float dy){
    cam.translate(dx, dy);
  }

  /**
   * Set the position of the camera absolutely
   *
   * @param x coord 
   * @param y coord 
   */
  public void setPosition(float x, float y){
    cam.position.set(x, y, 0);
  }

  /**
   * @param translation direction to move
   */
  public void translate(Translation translation){
    switch (translation) {
      case Right:
        cam.position.add(translationScalar(), 0, 0);
        break;
      case Left:
        cam.position.add(-translationScalar(), 0, 0);
        break;
      case Up:
        cam.position.add(0, translationScalar(), 0);
        break;
      case Down:
        cam.position.add(0, -translationScalar(), 0);
        break;
    }
  }

  /**
   * Set the zoom to a particular value. The value will still be clamped to 
   * minimum and maximums
   */
  public void setZoom(float zoom) {
    cam.zoom = MathUtils.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
  }

  /**
   * Zoom out...
   */
  public void zoomOut(){
    cam.zoom = MathUtils.clamp(cam.zoom + ZOOM_INCR, MIN_ZOOM, MAX_ZOOM);
  }

  /**
   * Zoom in...
   */
  public void zoomIn(){
    cam.zoom = MathUtils.clamp(cam.zoom - ZOOM_INCR, MIN_ZOOM, MAX_ZOOM);
  }
}
