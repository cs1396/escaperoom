package cs1396.escaperoom.game.entities.conditions;

import cs1396.escaperoom.game.state.GameContext;

public class XORConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    boolean left = this.left.evaluate(ctx);
    boolean right = this.right.evaluate(ctx);
    return (left || right) && !(left && right);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.XORConditional;
	}

	@Override
	public String getName() {
    return "...xor...";
	}
}

