package group24.escaperoom.game.entities.properties;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.ui.ContainerUI;
import group24.escaperoom.game.ui.GameDialog;

public class CoveringProperty extends ContainsItemProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Covers Items",
    "Covers other items",
    "Items with the covers items property can hold cover other items. \n(This is effectively the same as ContainsItems)",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Items Underneath";
  }

  public class LookUnderAction extends OpenAction {
    @Override
    public String getActionName() {
      return "Look Underneath";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      return new ActionResult()
        .showsDialog(
          new GameDialog.Builder("Was Covering", ctx.player)
            .withContent(new ContainerUI(CoveringProperty.this, ctx.player))
            .build()
        );
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  @Override
  public PropertyType getType() {
    return PropertyType.CoveringProperty;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new LookUnderAction());
  }

  @Override
  public CoveringProperty cloneProperty(Item newOwner){
    CoveringProperty p = new CoveringProperty();
    p.owner = newOwner;
    // should not allow multiple ContainsItemProperty items to contain
    // the same item, so simply returns a new empty property.
    // --> p.selectedItem.addAll(selectedItem);
    return p;
  }
}
