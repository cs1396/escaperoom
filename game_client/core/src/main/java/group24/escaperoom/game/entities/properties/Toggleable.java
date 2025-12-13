package group24.escaperoom.game.entities.properties;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.PhantomProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.entities.properties.connectors.ConnectorSource;
import group24.escaperoom.game.entities.properties.util.PropertyMap;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEvent.EventType;

/**
 * An item implementing Toggleable should have a texture
 */
public class Toggleable extends PhantomProperty {
  private boolean isToggled = false;
  private String toggleVerb = "clicked...";
  private int width, height;
  private static final PropertyDescription description = new PropertyDescription(
    "Togglable",
    "Toggle between two states",
    "Items with this property provide the player with a Toggle action which toggles that item between two different states",
    new HashSet<>()
  );

  private void toggleTo(boolean toggleTo, GameContext ctx) {
    isToggled = toggleTo;
    ctx.map.getEventBus().post(
      new GameEvent.Builder(EventType.ItemStateChange, ctx)
        .message(owner.getItemName() + " " + toggleVerb)
        .build()
    );
    owner.getProperty(PropertyType.ConnectorSource, ConnectorSource.class).ifPresent((csp) -> {
      csp.setActive(isToggled, ctx);
    });
  }

  private class ToggleAction implements PlayerAction {

    @Override
    public String getActionName() {
      return "Toggle";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      Optional<FragileProperty> ofp = owner.getProperty(PropertyType.Fragile, FragileProperty.class);
      if (ofp.isPresent()) {

        FragileProperty fp = ofp.get();

        if (fp.isBroken()) {
          ctx.map.getEventBus().post(
            new GameEvent.Builder(EventType.ItemStateChange, ctx)
              .message(owner.getItemName() + " is broken and won't move")
              .build()
          );
          return ActionResult.DEFAULT;
        }

        if (fp.isTrue()) {
          ctx.map.getEventBus().post(
            new GameEvent.Builder(EventType.ItemStateChange, ctx)
              .message(owner.getItemName() + " was fragile and broke!")
              .build()
          );
          fp.setBroken(true);
        }

      } else {
        ctx.map.getEventBus().post(
          new GameEvent.Builder(EventType.ItemStateChange, ctx)
            .message(owner.getItemName() + " " + toggleVerb)
            .build()
        );
      }

      toggleTo(!isToggled, ctx);
      updateTexture();
      return ActionResult.DEFAULT;
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  @Override
  public String getDisplayName() {
    return "Toggleable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Toggleable;
  }

  public boolean isToggled() {
    return isToggled;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new ToggleAction());
  }

  @Override
  public void write(Json json) {
    json.writeValue("verb", toggleVerb);
    json.writeValue("toggled", isToggled);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    toggleVerb = jsonData.getString("verb", "clicked...");
    isToggled = jsonData.getBoolean("toggled", false);

    width = AnimatedProperty.PIXELS_PER_WORLD_UNIT * owner.getWidth();
    height = AnimatedProperty.PIXELS_PER_WORLD_UNIT * owner.getHeight();
    PropertyMap.onMapCompletion.add((Void) -> {
      updateTexture();
      return null;
    });
  }

  @Override
  public void updateTexture() {
    if (isToggled) {
      owner.adjustTextureRegion(width, 0, width, height);
    } else {
      owner.adjustTextureRegion(0, 0, width, height);
    }
  }

  @Override
  public void defaultConfiguration(Item owner) {
    super.defaultConfiguration(owner);
    width = owner.getTexture().getRegionWidth() / 2;
    height = owner.getTexture().getRegionHeight();
  }

  @Override
  public PropertyDescription getDescription() {
    return description;
  }
}
