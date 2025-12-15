package cs1396.escaperoom.ui;

import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.InputEvent;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.game.entities.Item;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SelectableItemSlot extends ItemSlot {

  Function<Item, Void> onSelect = (i) -> {return null;};
  Function<Item, Void> onDeselect = (i) -> {return null;};

  public SelectableItemSlot() {
    super(Optional.empty());
  }

  public SelectableItemSlot(Item item) {
    this(Optional.of(item));
  }

  public void setOnSelect(Function<Item, Void> callBack){
    this.onSelect = callBack;
  }
  public void setOnDeselect(Function<Item, Void> callBack){
    this.onDeselect = callBack;
  }

  /**
   * Adds a visual selection to this item slot
   */
  public void setSelected(boolean select){
    inner.slotValues.ifPresent((sv) -> {
      if (select != sv.selected){
        inner.toggleSelectionOutline();
      }
    });
  }
  public void toggleSelect(){
    inner.slotValues.ifPresent((i) -> {
      setSelected(!i.selected);
    });
  }

  public SelectableItemSlot(Optional<Item> item) {
    super(item);
    item.ifPresent((i) -> {
      this.addListener(new ClickListener(){
        @Override
        public void clicked (InputEvent event, float x, float y) {
          toggleSelect();
          inner.slotValues.ifPresent((sv) ->{
            if(sv.selected){
              onSelect.apply(sv.item);
            } else {
              onDeselect.apply(sv.item);
            }
          });
        }
      });
      this.addListener(new InputListener() {
        @Override
        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
          inner.onHover();
        }
        @Override
        public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
          inner.onHoverExit();
        }
      });
    });
  }
}
