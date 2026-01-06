package cs1396.escaperoom.ui.dnd;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ItemPayload extends DragAndDrop.Payload{
  public ItemPayload(Item i){
    setDragActor(new G24Label(i.getItemName(), "bubble"));
    setObject(i);
  }

  public Item getItem(){
    return Item.class.cast(getObject());
  }
}
