package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

import cs1396.escaperoom.screens.AbstractScreen;

public class G24ImageButton extends ImageButton {
	public G24ImageButton() {
    this("default");
	}
	public G24ImageButton(String style) {
		super(AbstractScreen.skin);
    setStyle(AbstractScreen.skin.get(style, ImageButtonStyle.class));
	}

}
