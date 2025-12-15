package cs1396.escaperoom.ui;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.Player;

import com.badlogic.gdx.utils.Null;

public class InteractableItemSlot extends ItemSlot {
  protected Player actingPlayer;

  public InteractableItemSlot(Player actingPlayer) {
    this(Optional.empty(), actingPlayer);
  }

  public InteractableItemSlot(Item item, Player actingPlayer) {
    this(Optional.of(item), actingPlayer);
  }

  private ClickListener clickListener = new ClickListener() {
    public void clicked(InputEvent event, float x, float y) {
      inner.onClick(actingPlayer, InteractableItemSlot.this.getStage());
    }
  };

  private InputListener hoverListeners = new InputListener() {
    @Override
    public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
      inner.onHover();
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
      inner.onHoverExit();
    }
  };

  protected void addListeners() {
    this.addListener(clickListener);
    this.addListener(hoverListeners);
  }

  protected void removeListeners() {
    this.removeListener(clickListener);
    this.removeListener(hoverListeners);
  }
  protected void setItem(Item item){
      inner.setItem(item);
      addListeners();

  }

  protected Item removeItemFromSlot(){
      removeListeners();
      return inner.removeItem();
  }


  public InteractableItemSlot(Optional<Item> item, Player actingPlayer) {
    super(item);
    this.actingPlayer = actingPlayer;

    item.ifPresent((i) -> {
      addListeners();
    });
  }
}
