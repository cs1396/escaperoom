package cs1396.escaperoom.editor.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.game.entities.Item;

public class ItemDecoration {
  private final HashMap<Decoration<?>, Object> decorations = new HashMap<>();

  //----------------------------------------------------------------------------
  // MERGERS
  //----------------------------------------------------------------------------

  /**
   * A {@link Merger} for a {@link Color}
   */
  private final static Merger<Color> ColorMerger = (Color one, Color two) -> {
      return new Color(
          (one.r + two.r) / 2,
          (one.g + two.g) / 2,
          (one.b + two.b) / 2,
          (one.a + two.a) / 2);
  };

  /**
   * A {@link Merger} for two {@link Boolean}
   */
  private final static Merger<Boolean> RequireBoth  = (Boolean one, Boolean two) -> one && two;
  private final static Merger<Boolean> Either  = (Boolean one, Boolean two) -> one || two;
  
  //----------------------------------------------------------------------------
  // ITEM DECORATIONS
  //----------------------------------------------------------------------------
  public static final Decoration<Color> COLOR = new Decoration<>(ColorMerger);
  public static final Decoration<Boolean> DIMMED = new Decoration<>(RequireBoth);
  public static final Decoration<Boolean> HIGHTLIGHT = new Decoration<>(Either);
  public static final Decoration<Boolean> GRAYSCALE = new Decoration<>(RequireBoth);

  /**
   * How two types should be merged
   */
  public interface Merger<T> {
    public T merge(T one, T two);
  }

  /**
   * A single decoration that is applied to an {@link Item}
   */
  public static class Decoration<T> {
    Merger<T> merger;

    private Decoration(Merger<T> decorationMerger) {
      merger = decorationMerger;
    }
  }


  /**
   * Set the value of a given {@link Decoration} to {@code val}
   *
   * @param decoration can be null. 
   *                   if so, this decoration is removed from this {@link ItemDecoration}
   */
  public <T> ItemDecoration set(@Null Decoration<T> decoration, T val) {
    if (val != null) decorations.put(decoration, val);
    else decorations.remove(decoration);

    return this;
  }

  /**
   * Merge this {@link ItemDecoration} with another
   */
  public ItemDecoration merge(ItemDecoration other) {
    for (Map.Entry<Decoration<?>, Object> entry : other.decorations.entrySet()) {
      Decoration<?> deco = entry.getKey();
      mergeProperty(deco, entry.getValue());
    }

    return this;
  }

  private <T> void mergeProperty(Decoration<T> decoration, Object newValue) {
    if (!decorations.containsKey(decoration)) {
      decorations.put(decoration, newValue);
    } else {
      T existing = (T) decorations.get(decoration);
      T merged = decoration.merger.merge(existing, (T) newValue);
      decorations.put(decoration, merged);
    }
  }

  public void applyTo(Item item) {
    if (has(COLOR))
      item.setColor(get(COLOR));
    if (has(GRAYSCALE) && get(GRAYSCALE))
      item.setColor(0.5f, 0.5f, 0.5f, 1f);
    if (has(DIMMED))
      item.setDimmed(get(DIMMED));
    if (has(HIGHTLIGHT))
      item.setHighlighed(get(HIGHTLIGHT));
  }

  /**
   * "Cancel out" this decoration
   */
  public void removeFrom(Item item){
    if (has(COLOR) || has(GRAYSCALE))
      item.setColor(1,1,1,1);
    if (has(DIMMED))
      item.setDimmed(false);
    if (has(HIGHTLIGHT))
      item.setHighlighed(false);
  }

  /**
   * Return whether or not this {@link ItemDecoration} would modify an Item
   */
  public boolean hasModifications(){
    return decorations.size() > 0;
  }

  private <T> T get(Decoration<T> property) {
    return (T) decorations.get(property);
  }

  private boolean has(Decoration<?> decoration) {
    return decorations.containsKey(decoration);
  }
}
