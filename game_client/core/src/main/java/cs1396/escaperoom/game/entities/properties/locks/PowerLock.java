package cs1396.escaperoom.game.entities.properties.locks;

import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.LockingMethod;
import cs1396.escaperoom.game.entities.properties.connectors.ConnectorSink;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.state.GameEvent;
import cs1396.escaperoom.game.state.GameEventBus.GameEventListener;
import cs1396.escaperoom.screens.GameScreen;

public class PowerLock extends LockingMethod {

  protected class UnlockAction extends AbstractUnlockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      owner.get().getProperty(PropertyType.ConnectorSink, ConnectorSink.class).ifPresent((csp) -> {
        if (csp.isConnected()) {
          updateLocked(ctx, false);
        } 
      });
      return ActionResult.DEFAULT;
    }
  };

  protected class LockAction extends AbstractLockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      owner.get().getProperty(PropertyType.ConnectorSink, ConnectorSink.class).ifPresent((csp) -> {
        if (!csp.isConnected()) {
          updateLocked(ctx, true);
        } 
      });
      return ActionResult.DEFAULT;
    }
  };

  @Override
  public String getName() {
    return "Power Lock";
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.PowerLock;
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new PowerLock();
  }

  @Override
  protected AbstractLockAction getLockAction() {
    return new LockAction();
  }

  @Override
  protected AbstractUnlockAction getUnlockAction() {
    return new UnlockAction();
  }

  /**
   * Listen for {@link GameEvent}s to unlock and 
   * relock when this owner becomes powered or unpowered
   */
  GameEventListener powerListener = event -> {
    if (event.source != owner.get()) return; 

    switch (event.type){
      case ItemConnected:
        new UnlockAction().act(event.ctx);
        break;
      case ItemDisconnected:
        new LockAction().act(event.ctx);
        break;
      default:
        break;
    }
  };

  @Override
  public void onGameLoad(GameScreen screen) {
    screen.getEventBus().addListener(powerListener);
  }
}
