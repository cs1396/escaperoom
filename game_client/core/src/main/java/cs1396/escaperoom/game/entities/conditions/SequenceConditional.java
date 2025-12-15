package cs1396.escaperoom.game.entities.conditions;

import cs1396.escaperoom.game.state.GameContext;

public class SequenceConditional extends KaryConditional {
  int nextStage = 0;
  State state = State.Progressing;

	@Override
	public boolean evaluate(GameContext ctx) {
    return state == State.Completed;
	}

  public enum State {
    Progressing,
    NeedsReset,
    Completed,
  }

  private void progress(GameContext ctx){
    // check their conditions
    for (int i = 0; i < children.size; i++){
      if (children.get(i).evaluate(ctx)){
        if (i == nextStage){
          nextStage = i+1;
          break;
        }
        if (i > nextStage){
          state = State.NeedsReset;
          return;
        }
      } else {
        if (i < nextStage){
          state = State.NeedsReset;
          return;
        }
      }
    }

    // if we have moved next stage to be greater than all our conditionals, then we are done!
    if (nextStage >= children.size){
      state = State.Completed;
    }
  }
  private void tryReset(GameContext ctx){
    boolean canReset = true;

    for (Conditional c : children){
      if (c.evaluate(ctx)){
        canReset = false;
      }
    }

    if (canReset){
      nextStage = 0;
      state = State.Progressing;
    }
  }

  @Override
  public boolean poll(GameContext ctx){
    // poll all our children 
    children.forEach((c) -> c.poll(ctx));

    switch (state){
		case Completed:
      return false;
		case NeedsReset:
      tryReset(ctx);
			break;
		case Progressing:
      progress(ctx);
			break;
    }

    return true;
  }

  @Override
  public boolean requiresPoll(){
    return true;
  }

	@Override
	public ConditionalType getType() {
    return ConditionalType.SequenceConditional;
	}

	@Override
	public String getName() {
    return "In Sequence...";
	}
}
