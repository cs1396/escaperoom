package cs1396.escaperoom.editor.tools;

import cs1396.escaperoom.screens.LevelEditor;

import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.MapScreen;

public abstract class SelectionTool extends EditorTool implements Drawable {
  private NinePatch texture;
  protected Selection selection;
  protected boolean selecting;

  public SelectionTool(LevelEditor stage) {
    super(stage);

    AssetManager.instance().load("textures/selection_bkg.9.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/selection_bkg.9.png");
    Texture bkg = AssetManager.instance().get("textures/selection_bkg.9.png", Texture.class);
    texture = new NinePatch(bkg, 6, 6, 6, 6);
  }

  /**
   * Listener on the stage for select tool
   * 
   */
  private InputListener selectListener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      if (editor.collidesMenu(x, y)) {
        cancel();
        return false;
      };

      startSelection(x, y);
      return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      selection.setEnd(new Vector2(x, y));
      selection.updateArea();
      editor.getUI().getHints().coordHints.update(selection.area);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      endSelection();
    }
  };

  public void startSelection(float x, float y) {
    selecting = true;
    if (selection == null) selection = new Selection(new Rectangle(), new Array<>(), editor);
    else selection.area.set(MathUtils.floor(x),MathUtils.floor(y), 1, 1);
    editor.addOverlayDrawable(this);
    selection.setStart(new Vector2(x, y));
  }

  public Optional<Selection> getSelection() {
    return Optional.of(selection);
  }

  /**
   * End the logical selection
   */
  public void endSelection() {
    selecting = false;
    editor.removeOverlayDrawable(this);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(selectListener);
  }

  @Override
  public String getName() {
    return "Select Tool";
  }

  @Override
  public void select() {
    CursorManager.setCursor(CursorType.Select);
    editor.addListener(selectListener);
  }

  /**
   * @return the active selected area
   */
  public Optional<Rectangle> getSelectionArea() {
    if (selection == null) {
      return Optional.empty();
    }
    return Optional.of(selection.getArea());
  }

  public static class Selection {
    protected Rectangle area = new Rectangle(0,0,0,0); // in game Coords
    protected Array<Item> selectedItems = new Array<>();
    Vector2 start = new Vector2();
    Vector2 end = new Vector2();
    protected MapScreen screen = null;

    public Selection(Rectangle area, Array<Item> selectedItems, MapScreen screen) {
      this.area = area;
      this.selectedItems = selectedItems;
      this.screen = screen;
    }

    public void minimizeArea() {
      int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
      int maxX = -1, maxY = -1;

      for (Item i : selectedItems) {
        minX = i.getX() < minX ? (int) i.getX() : minX;
        minY = i.getY() < minY ? (int) i.getY() : minY;

        int upperX = (int) i.getX() + i.getWidth();
        int upperY = (int) i.getY() + i.getHeight();

        maxX = upperX > maxX ? upperX : maxX;
        maxY = upperY > maxY ? upperY : maxY;
      }
      this.area.set(minX, minY, maxX - minX, maxY - minY);
    }

    protected void addItem(Item item) {
      selectedItems.add(item);
    }

    /**
     * In Game coords
     */
    public void setStart(Vector2 vec) {
      this.start.set(vec);
    }

    /**
     * In Game coords
     */
    public void setEnd(Vector2 vec) {
      this.end.set(vec);
    }

    /**
     * @return the region that is selected
     */
    public Rectangle getArea() {
      return this.area;
    }

    public Array<Item> getItems() {
      return this.selectedItems;
    }

    public void setItems(Array<Item> items) {
      selectedItems.forEach((i) -> i.setSelected(false));
      selectedItems = items;
      selectedItems.forEach((i) -> i.setSelected(true));
      minimizeArea();
    }

    public void clearSelectedItems() {
      for (Item item : selectedItems) {
        item.setSelected(false);
      }
      selectedItems.clear();
    }

    public void updateArea() {
      float x = Math.min(start.x, end.x);
      float y = Math.min(start.y, end.y);
      float width = Math.abs(end.x - start.x);
      float height = Math.abs(end.y - start.y);

      Vector2 snapOrigin = snap(new Vector2(x, y), SnapMethod.Floor);
      Vector2 snapEnd = snap(new Vector2(x + width, y + height), SnapMethod.Ceil);

      area.set(
        MathUtils.round(snapOrigin.x),
        MathUtils.round(snapOrigin.y),
        MathUtils.round(snapEnd.x - snapOrigin.x),
        MathUtils.round(snapEnd.y - snapOrigin.y)
      );
    }

    /**
     * Give me a rectangle in game coordinates.
     */
    public void setNewArea(Rectangle rectangle) {
      setStart(new Vector2(rectangle.getX(), rectangle.getY()));
      setEnd(new Vector2(rectangle.getX() + rectangle.getWidth(),
                         rectangle.getY() + rectangle.getHeight()));
      updateArea();
    }

    private static enum SnapMethod {
      Ceil(f -> MathUtils.ceil(f)),
      Floor(f -> MathUtils.floor(f)),

      ;

      private Function<Float, Integer> method;

      private SnapMethod(Function<Float, Integer> method) {
        this.method = method;
      }
    }

    private Vector2 snap(Vector2 gameCoords, SnapMethod snap) {
      return new Vector2(
        snap.method.apply(gameCoords.x),
        snap.method.apply(gameCoords.y)
      );
    }
  }

  @Override
  public ToolType getType() {
      return ToolType.Selection;
  }

  @Override
  public int renderPriority(){
    return Integer.MAX_VALUE;
  }

  @Override
  public void draw(Batch batch){
    texture.draw(batch, selection.area.getX(), selection.area.getY(), 0f, 0f, selection.area.width * 16, selection.area.height * 16, 1f / 16f, 1f / 16f, 0);
  }

  @Override
  public Vector2 position(){
    return new Vector2(0,0);
  }

  @Override
  public int getTileDepth() {
    return 0;
  }

  
}
