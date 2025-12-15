package cs1396.escaperoom.game.entities.conditions;

import cs1396.escaperoom.game.state.GameContext;

/**
 * {@link Conditional} corresponding to logical and.
 */
public class AndConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    return left.evaluate(ctx) && right.evaluate(ctx);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.AndConditional;
	}

	@Override
	public String getName() {
    return "...and...";
	}
}
