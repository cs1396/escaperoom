package cs1396.escaperoom.game.entities.conditions;

import cs1396.escaperoom.game.state.GameContext;

public class OrConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    return left.evaluate(ctx) || right.evaluate(ctx);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.OrConditional;
	}

	@Override
	public String getName() {
    return "...or...";
	}
}
