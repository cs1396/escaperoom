package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import cs1396.escaperoom.engine.render.PixMapHelper;
import cs1396.escaperoom.game.entities.Item;

public class DrawingPane extends Actor {
  private final Pixmap pixmap;
  private final Texture texture;
  private Color currentColor = Color.BLACK;
  public final static Color canvasColor = new Color(254f / 255, 243f / 255, 192f / 255, 1f);
  private int lastX, lastY;
  private boolean drawing;
  private static int brushSize = 1; // pixel brush radius
  private Brush brush;
  private CircleBrush circleBrush = new CircleBrush();
  private RectangleBrush rectangleBrush = new RectangleBrush();
  private ItemBrush itemBrush = new ItemBrush();

  private class ItemBrush implements Brush {
    private int lastBrushSize = brushSize;
    private Item currentItem;
    private Pixmap itemPixmap;

    public void setItem(Item item) {
      currentItem = item;
      updateBrush();
    }

    private void updateBrush(){
      if (currentItem == null){
        return;
      }
      if (itemPixmap != null){
        itemPixmap.dispose();
      }
      itemPixmap = PixMapHelper.fromTextureRegion(new AtlasRegion(currentItem.getTexture()), brushSize);
      lastBrushSize = brushSize;
    }

    public void drawPixel(int x, int y) {
      if (currentItem == null){
        return;
      }
      if (itemPixmap == null) {
        updateBrush();
      }

      if (brushSize != lastBrushSize) {
        updateBrush();
      }

      pixmap.drawPixmap(itemPixmap, x - itemPixmap.getWidth() / 2, y - itemPixmap.getHeight()/2);
    }
  }

  private class CircleBrush implements Brush {
    public void drawPixel(int x, int y) {
      if (brushSize == 1) {
        pixmap.drawPixel(x, y);
      } else {
        pixmap.fillCircle(x, y, brushSize / 2);
      }
    }
  }

  private class RectangleBrush implements Brush {
    public void drawPixel(int x, int y) {
      if (brushSize == 1) {
        pixmap.drawPixel(x, y);
      } else {
        pixmap.fillRectangle(x, y, brushSize, brushSize);
      }
    }
  }

  private interface Brush {
    public void drawPixel(int x, int y);
  }

  public enum BrushShape {
    Circle,
    Square,
    Item,
  };

  public DrawingPane(Pixmap pixmap) {
    setSize(pixmap.getWidth(), pixmap.getHeight());
    this.pixmap = pixmap;
    texture = new Texture(pixmap);
    pixmap.setColor(canvasColor);
    updateTexture();

    addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        event.stop();
        drawing = true;
        int px = (int) x;
        int py = (int) y;
        drawPixel(px, py);
        lastX = px;
        lastY = py;
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        if (drawing) {
          event.stop();
          int px = (int) x;
          int py = (int) y;
          drawLine(lastX, lastY, px, py);
          lastX = px;
          lastY = py;
        }
      }

      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        event.stop();
        drawing = false;
      }
    });
  }

  public static int getBrushSize() {
    return brushSize;
  }

  private void drawPixel(int x, int y) {
    pixmap.setColor(currentColor);
    int invY = (int) getHeight() - y;
    brush.drawPixel(x, invY);
    updateTexture();
  }

  private void drawLine(int x1, int y1, int x2, int y2) {
    pixmap.setColor(currentColor);
    int invY1 = (int) getHeight() - y1;
    int invY2 = (int) getHeight() - y2;

    // bresenham’s line algorithm
    // https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(invY2 - invY1);
    int sx = x1 < x2 ? 1 : -1;
    int sy = invY1 < invY2 ? 1 : -1;
    int err = dx - dy;

    int cx = x1;
    int cy = invY1;
    while (true) {
      brush.drawPixel(cx, cy);

      if (cx == x2 && cy == invY2)
        break;
      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        cx += sx;
      }
      if (e2 < dx) {
        err += dx;
        cy += sy;
      }
    }
    updateTexture();
  }

  private void updateTexture() {
    texture.draw(pixmap, 0, 0);
  }

  public void setDrawColor(Color color) {
    this.currentColor = color;
    pixmap.setColor(currentColor);
  }

  public void setBrushSize(int size) {
    DrawingPane.brushSize = Math.max(1, size);
  }

  public void setBrushShape(BrushShape shape) {
    switch (shape) {
      case Circle:
        brush = circleBrush;
        break;
      case Square:
        brush = rectangleBrush;
        break;
      case Item:
        brush = itemBrush;
      default:
        break;
    }
  }

  public void setItemBrushItem(Item item) {
    itemBrush.setItem(Item.class.cast(item.clone(true)));
  }

  public void clear() {
    pixmap.setColor(canvasColor);
    pixmap.fill();
    updateTexture();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.draw(texture, getX(), getY(), getWidth(), getHeight());
  }
}
