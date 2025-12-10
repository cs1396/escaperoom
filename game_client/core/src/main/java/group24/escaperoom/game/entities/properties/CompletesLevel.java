package group24.escaperoom.game.entities.properties;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.PhantomProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.ui.GameDialog;
import group24.escaperoom.screens.SinglePlayerGame;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24Dialog.Content;

public class CompletesLevel extends PhantomProperty {
  private static final PropertyDescription description = new PropertyDescription(
    "Completes level",
    "Provides game ending action",
    "Completes level items provide the special player action \"Claim your victory\"",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }


  public class WinAction implements PlayerAction {

	@Override
	public String getActionName() {
      return "Complete Level";
	}

    @Override
    public ActionResult act(GameContext ctx) {
      G24TextButton winButton = new G24TextButton("Claim your victory");
      winButton.addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          SinglePlayerGame screen = (SinglePlayerGame) owner.map;
          screen.completeLevel(true);
        }
        
      });

      return new ActionResult().showsDialog(
        new GameDialog.Builder("Congratulations!", ctx.player)
          .withContent(winButton)
          .build()
      );
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new WinAction());
  }

  @Override
  public String getDisplayName() {
    return "Completes Level";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.CompletesLevel;
  }
}   
