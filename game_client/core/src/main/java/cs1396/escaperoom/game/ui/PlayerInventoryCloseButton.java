package cs1396.escaperoom.game.ui;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class PlayerInventoryCloseButton extends G24TextButton {

    public PlayerInventoryCloseButton(String label, Player player) {
        super(label);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.setInventoryOpen(false);
            }
        });
    }
}
