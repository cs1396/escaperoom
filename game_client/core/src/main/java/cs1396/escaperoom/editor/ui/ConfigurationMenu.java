package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;

import cs1396.escaperoom.screens.AbstractScreen;

public class ConfigurationMenu<CC extends Actor & cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose> extends Menu {

  public static class VGroup extends VerticalGroup implements HandlesMenuClose  {
    @Override public void handle() {
    }
  }

  CC content;
  @FunctionalInterface
  public interface HandlesMenuClose {
    /**
     * Called when the menu is closed
     */
    public void handle();
  }

	public ConfigurationMenu(MenuEntry parent, String title, AbstractScreen screen) {
		super(parent, title, screen);
	}

	public ConfigurationMenu(MenuEntry parent, CC content, String title, AbstractScreen screen) {
		super(parent, title, screen);
    setContent(content);
	}

  public void setContent(CC configContent){
    content = configContent;
    add(content);
    pack();
  }

  @Override
  public void close() {
    super.close();
    if (content != null) content.handle();
  }
}
