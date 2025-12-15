package cs1396.escaperoom.game.ui;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.properties.ContainsItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ContainedItem;
import cs1396.escaperoom.game.state.GameEvent.EventType;
import cs1396.escaperoom.game.state.GameEventBus.GameEventListener;
import cs1396.escaperoom.screens.GameScreen;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.ui.InteractableItemSlot;
import cs1396.escaperoom.ui.dnd.ItemPayload;

public class ContainerItemSlot extends InteractableItemSlot {
  ContainsItemProperty prop;
  ContainerItemSlotSource source;
  ContainerItemSlotTarget target;
  GameEventListener listener;

  public class ContainerItemSlotTarget extends DragAndDrop.Target {

    ContainerItemSlot slot;
    public ContainerItemSlotTarget(ContainerItemSlot slot) {
      super(slot);
      this.slot = slot;
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
      slot.inner.onHover();
      return true;
    }

		public void reset (Source source, Payload payload) {
      slot.inner.onHoverExit();
		}

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
      if (source instanceof ContainerItemSlotSource){
        return;
      }
      Item i = ItemPayload.class.cast(payload).getItem();
      setItem(i);
      // ensure the item is removed
      i.remove();
    }
  }


  @Override 
  public void setItem(Item i){
    super.setItem(i);
    if (source == null){
      source = new ContainerItemSlotSource(this);
    }
    if (listener == null){
      listener = (ev) -> {
        if (ev.source.equals(i)){
          removeItemFromSlot();
        }
      };
    }

    GameScreen game = actingPlayer.getGameScreen();
    game.getEventBus().addListener(listener, (ev) -> ev.type == EventType.ItemObtained);
    game.getDragAndDrop().addSource(source);
    game.getDragAndDrop().removeTarget(target);

    prop.addValue(new ContainedItem(i, prop.getOwner()));
  }

  @Override 
  public Item removeItemFromSlot(){
    Item removed = super.removeItemFromSlot();
    GameScreen gameScreen = actingPlayer.getGameScreen();
    gameScreen.getDragAndDrop().removeSource(source);
    if (target == null){
      target = new ContainerItemSlotTarget(this);
    }

    gameScreen.getDragAndDrop().addTarget(target);
    prop.removeItem(removed);
    gameScreen.getEventBus().removeListener(listener);
    return removed;
  }


  public class ContainerItemSlotSource extends DragAndDrop.Source {
    ContainerItemSlot slot;
    ContainerItemSlotSource(ContainerItemSlot slot) {
      super(slot);
      this.slot = slot;
      slot.addListener(CursorManager.hoverHelper(CursorType.Hand));
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      slot.inner.slotValues.get().itemPreview.setVisible(false);
      return new ItemPayload(slot.inner.slotValues.get().item);
    }

    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
      if (target == null || target instanceof ContainerItemSlotTarget) {
        slot.inner.slotValues.get().itemPreview.setVisible(true);
      } else {
        removeItemFromSlot();
      }
    }
  }



  public ContainerItemSlot(Player actingPlayer, ContainsItemProperty containsProp) {
    super(actingPlayer);
    this.prop = containsProp;
    this.target = new ContainerItemSlotTarget(this);
    actingPlayer.getGameScreen()
      .getDragAndDrop()
      .addTarget(target);
  }

  public ContainerItemSlot(Item item, Player actingPlayer, ContainsItemProperty containsProp) {
    super(Optional.of(item), actingPlayer);
    this.prop = containsProp;
    this.source = new ContainerItemSlotSource(this);
    listener = (ev) -> {
      if (ev.source.equals(item)){
        removeItemFromSlot();
      }
    };
    actingPlayer.getGameScreen()
      .getEventBus()
      .addListener(listener, (ev) -> ev.type == EventType.ItemObtained);
    actingPlayer.getGameScreen()
      .getDragAndDrop()
      .addSource(source);
  }

}
