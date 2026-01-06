package cs1396.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConfigurationMenu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.editor.ui.PropertyConfiguration.Select;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.values.PhantomPropertyValue;
import cs1396.escaperoom.game.entities.properties.values.StringItemPropertyValue;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.SimpleUI;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24NumberInput;

public class ConnectorRelay extends Connector {
  private static final PropertyDescription description = new PropertyDescription(
    "Connector Relay",
    "Transforms signals",
    "Connector items can have different types, and propagate signals to other connectors of their same types.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription() {
    return description;
  }
  private RelayType relayType = RelayType.And;

  @Override
  public MenuType getInputType() {
    switch (relayType) {
      case Buffer:
      case Filter:
      case Clock:
        return MenuType.PopOut;
      default:
        return MenuType.None;

    }
  }

  private static class RelayContext extends PhantomPropertyValue implements StringItemPropertyValue {
    Boolean output = false;
    Boolean input1 = false;
    Boolean input2 = false;
    Boolean stored = false;
    int number = 500;
    Object state = null;

    @Override
    public String getValue() {
      return Integer.toString(this.number);
    }

    @Override
    public void setValue(String value) {
      try {
        this.number = Integer.parseInt(value);
      } catch (NumberFormatException nfe) {
      }
    }
  }

  public enum RelayType {
    And((ctx) -> {
      ctx.output = ctx.input1 && ctx.input2;
      return ctx;
    }),
    Clock((ctx) -> {
      final class TimerState {
        long last = -1L;
        long period = 500L;

        TimerState(int period) {
          this.period = period;
        }
      }

      if (ctx.state == null) {
        ctx.state = new TimerState(ctx.number);
      }

      TimerState ts = (TimerState) ctx.state;
      long now = System.currentTimeMillis();

      if (ts.last < 0L) {
        ts.last = now;
        ctx.output = false;
      }

      if (now - ts.last >= ts.period) {
        ctx.output = !ctx.output;
        ts.last = now;
      }

      return ctx;
    }),
    Nand((ctx) -> {
      ctx.output = !(ctx.input1 && ctx.input2);
      return ctx;
    }),
    Or((ctx) -> {
      ctx.output = ctx.input1 || ctx.input2;
      return ctx;
    }),
    Xor((ctx) -> {
      ctx.output = ctx.input1 ^ ctx.input2;
      return ctx;
    }),
    Buffer((ctx) -> {
      final class TimerState {
        Long time = -1L;
        int state; // 0 => green 1 => green persisting 2 => red
      }

      if (ctx.state == null) {
        ctx.state = new TimerState();
      }

      TimerState ts = (TimerState) ctx.state;
      if (ctx.input1) {
        ctx.output = true;
        ts.time = -1L;
        ts.state = 0;
      } else {
        switch (ts.state) {
          case 0:
            ts.time = System.currentTimeMillis();
            ts.state = 1;
            break;
          case 1:
            if (System.currentTimeMillis() > ts.time + ctx.number) {
              ctx.output = false;
              ts.state = 2;
            }
            break;
          case 2:
            ctx.output = false;
            break;
        }
      }
      return ctx;
    }),
    Filter((ctx) -> {
      final class TimerState {
        Long time = -1L;
        int state = 0; // 0 == red, 1 == green waiting, 2 == green
      }

      if (ctx.state == null) {
        ctx.state = new TimerState();
      }

      TimerState ts = (TimerState) ctx.state;
      if (ctx.input1) {
        switch (ts.state) {
          case 0:
            ts.time = System.currentTimeMillis();
            ts.state = 1;
            break;
          case 1:
            if (System.currentTimeMillis() > ts.time + ctx.number) {
              ts.time = -1L;
              ts.state = 2;
              ctx.output = true;
            }
            break;
          case 2:
            ctx.output = true;
        }

      } else {
        ts.state = 0;
        ts.time = -1L;
        ctx.output = false;
      }
      return ctx;
    }),
    SRLatch((ctx) -> {
      if (ctx.input1)
        ctx.stored = true;
      if (ctx.input2)
        ctx.stored = false;
      ctx.output = ctx.stored;
      return ctx;
    }),
    Not((ctx) -> {
      ctx.output = !ctx.input1;
      return ctx;
    });

    private Function<RelayContext, RelayContext> func2;

    private RelayType(Function<RelayContext, RelayContext> apply) {
      this.func2 = apply;
    }

    public RelayContext update(RelayContext ctx) {
      return func2.apply(ctx);
    }

    public Direction output() {
      return Direction.NORTH;
    }

    public Direction input1() {
      switch (this) {
        case Or:
        case And:
        case SRLatch:
        case Xor:
        case Nand:
          return Direction.EAST;
        case Filter:
        case Buffer:
        case Clock:
        case Not:
          return Direction.SOUTH;
      }
      return null;
    }

    public Optional<Direction> input2() {
      switch (this) {
        case Or:
        case And:
        case SRLatch:
        case Xor:
        case Nand:
          return Optional.of(Direction.WEST);
        case Filter:
        case Buffer:
        case Clock:
        case Not:
          return Optional.empty();
      }
      return null;
    }

  }

  public static enum Direction {
    NORTH(0, 1, 0),
    SOUTH(0, -1, 2),
    EAST(1, 0, 1),
    WEST(-1, 0, 3);

    public final int offsetX, offsetY;
    public final int pos;
    private static final Direction[] dirs = { NORTH, EAST, SOUTH, WEST };

    private Direction(int offX, int offY, int pos) {
      this.offsetX = offX;
      this.offsetY = offY;
      this.pos = pos;
    }

    Direction adjust(int rotation) {

      // ex. NORTH rotate 90 == EAST
      // cout = 1
      // pos = 0;
      // dirs[1] = EAST

      int count = Math.abs(rotation / 90);
      int newInd = (pos + count) % 4;
      return dirs[newInd];
    }
  }

  private RelayContext rlyCtx = new RelayContext();

  @Override
  public void propagate(GameContext ctx, HashSet<Integer> seen) {
    updateInputs(ctx);
    // in case we don't have inputs, apply
    rlyCtx = relayType.update(this.rlyCtx);
    connected = rlyCtx.output;
    updateColor();
    IntVector2 position = owner.getPosition();
    IntVector2 pos;

    Direction outputDirs = relayType.output().adjust((int) owner.getRotation());
    pos = position.cpy();
    pos.x += outputDirs.offsetX;
    pos.y += outputDirs.offsetY;
    Utils.connectableAt(pos, ctx.map, type).ifPresent((i) -> {
      if (!seen.contains(i.item.getID())) {
        i.connectable.acceptSignalFrom(this, position, ctx, seen);
      }
    });
  }

  @Override
  public IntVector2[] connectionDirections() {
    Direction d = relayType.output().adjust((int) owner.getRotation());
    IntVector2 outputVec = new IntVector2(d.offsetX, d.offsetY);
    d = relayType.input1().adjust((int) owner.getRotation());
    IntVector2 input1 = new IntVector2(d.offsetX, d.offsetY);
    Optional<Direction> oD = relayType.input2().map((dir) -> dir.adjust((int) owner.getRotation()));

    if (oD.isPresent()) {
      IntVector2[] ret = {
          outputVec,
          input1,
          new IntVector2(oD.get().offsetX, oD.get().offsetY)
      };
      return ret;
    } else {
      IntVector2[] ret = {
          outputVec,
          input1,
      };
      return ret;
    }

  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  public void updateInputs(GameContext ctx) {
    IntVector2 position = owner.getPosition();

    Direction input1Dir = relayType.input1().adjust((int) owner.getRotation());
    IntVector2 cpy = position.cpy();

    cpy.x += input1Dir.offsetX;
    cpy.y += input1Dir.offsetY;

    Utils.connectableAt(cpy, ctx.map, getConnectorType()).ifPresent((c) -> {
      rlyCtx.input1 = c.connectable.isConnected();
    });

    relayType.input2().ifPresent((i2) -> {
      Direction input2 = i2.adjust((int) owner.getRotation());
      IntVector2 cpy2 = position.cpy();

      cpy2.x += input2.offsetX;
      cpy2.y += input2.offsetY;

      Utils.connectableAt(cpy2, ctx.map, getConnectorType()).ifPresent((c) -> {
        rlyCtx.input2 = c.connectable.isConnected();
      });
    });
  }

  @Override
  public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen) {

    IntVector2 position = owner.getPosition();

    Direction input1Dir = relayType.input1().adjust((int) owner.getRotation());
    IntVector2 cpy = position.cpy();

    cpy.x += input1Dir.offsetX;
    cpy.y += input1Dir.offsetY;

    if (cpy.equals(pos)) {
      rlyCtx.input1 = source.isConnected();
      rlyCtx = relayType.update(this.rlyCtx);

      connected = rlyCtx.output;
      updateColor();
      propagate(ctx, seen);
      return;
    }

    relayType.input2().ifPresent((i2) -> {
      Direction input2 = i2.adjust((int) owner.getRotation());
      IntVector2 cpy2 = position.cpy();

      cpy2.x += input2.offsetX;
      cpy2.y += input2.offsetY;
      if (cpy2.equals(pos)) {
        rlyCtx.input2 = source.isConnected();
        rlyCtx = relayType.update(rlyCtx);
        connected = rlyCtx.output;

        updateColor();
        propagate(ctx, seen);
      }
    });
  }

  @Override
  public void setActive(boolean connected, GameContext ctx) {
    this.connected = connected;
    propagate(ctx, new HashSet<>());
  }

  @Override
  public String getDisplayName() {
    return "Relay (" + type.name() + "): " + relayType.name();
  }

  @Override
  public ConfigurationMenu<SimpleUI> getPopOut(MenuEntry parent) {
    LevelEditor screen = (LevelEditor) parent.getScreen();

    StringItemPropertyValue contents = (StringItemPropertyValue) getCurrentValue();
    G24NumberInput textField = new G24NumberInput(contents.getValue());
    textField.setMaxLength(200);

    HorizontalGroup row = new HorizontalGroup();
    row.space(10);
    row.addActor(new G24Label(getDisplayName() + ":"));

    textField.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        contents.setValue(textField.getText());
      }
    });
    textField.pack();
    row.addActor(textField);

    return new ConfigurationMenu<>(parent, new SimpleUI(row), "Connector", screen);
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ConnectorRelay;
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("relay_type", relayType.name());
    json.writeValue("delay", rlyCtx.number);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    super.read(json, jsonData);
    this.relayType = RelayType.valueOf(jsonData.getString("relay_type"));
    this.rlyCtx.number = jsonData.getInt("delay", 500);
  }

  @Override
  public PhantomPropertyValue getCurrentValue() {
    return rlyCtx;
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config =  super.getCustomItemConfigurationMenu().get();

    config.addSelect(
      "Relay type",
      null,
      new Select<RelayType>(
        (val) -> this.relayType = val,
        (val) -> {},
        RelayType.values(),
        (val) -> val.name(),
        1,
        relayType
      )
    );

    return Optional.of(config);
  }
}
