package cs1396.escaperoom.game.entities.conditions;
import cs1396.escaperoom.game.state.GameContext;

public class NotConditional extends UnaryConditional {
  @Override
  public boolean evaluate(GameContext ctx) {
    return !child.evaluate(ctx);
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.NotConditional;
  }

  @Override
  public String getName() {
    return "not...";
  }
}
