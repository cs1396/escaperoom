package cs1396.escaperoom.game.ui;


import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.ui.ConfirmDialog;
import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;

/**
 * Game dialog is different than {@link ConfirmDialog} in that it closes when the player moves away
 *
 */
public class GameDialog extends G24Dialog {
  Player player;
  Vector2 playerPos;

  protected GameDialog(Builder builder) {
    super(builder);
    setModal(false);

    this.player = builder.player;
    this.playerPos = player.getCenter();
    button(new G24TextButton("Continue..."));
  }

  public static class Builder extends AbstractBuilder<GameDialog, Builder> {
    protected Player player;

    public Builder(String title, Player player){
      super(title);
      this.player = player;
    }

    protected Builder self() { return this; }
    public GameDialog build() { return new GameDialog(this); }
  }

  @Override
  public void act(float delta){
    super.act(delta);
    if (!playerPos.equals(player.getCenter())){
      hide(Actions.fadeOut(0.1f, Interpolation.fade));
    }
  }
}
