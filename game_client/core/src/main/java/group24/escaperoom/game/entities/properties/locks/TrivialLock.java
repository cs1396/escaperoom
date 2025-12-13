package group24.escaperoom.game.entities.properties.locks;

import group24.escaperoom.game.entities.properties.base.LockingMethod;
import group24.escaperoom.game.state.GameContext;

public class TrivialLock extends LockingMethod {

  protected class UnlockAction extends AbstractUnlockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      updateLocked(ctx, false, "", owner.get().getItemName() + " was unlocked!");
      return ActionResult.DEFAULT;
    }
  };

	@Override
	public String getName() {
    return "Unlocked";
	}

	@Override
	public LockingMethodType getType() {
    return LockingMethodType.TrivialLock;
	}

  @Override
  protected LockingMethod getEmptyMethod() {
    return new TrivialLock();
  }

  @Override
  protected AbstractLockAction getLockAction() {
    return null;
  }

  @Override
  protected AbstractUnlockAction getUnlockAction() {
    return new UnlockAction();
  }

}
