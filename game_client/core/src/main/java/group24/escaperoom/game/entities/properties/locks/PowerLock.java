package group24.escaperoom.game.entities.properties.locks;

import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.base.LockingMethod;
import group24.escaperoom.game.entities.properties.connectors.ConnectorSink;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEventBus.GameEventListener;
import group24.escaperoom.screens.GameScreen;

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
