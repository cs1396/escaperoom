package cs1396.escaperoom.game.entities.properties.values;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.game.entities.Item;

public class Style implements ItemPropertyValue, Json.Serializable {
  public String name, texture;
  private Item owner;

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public Style() {}

  public Style(String name, String texture){
    this.name = name;
    this.texture = texture;
  }

  public String getStyleName(){
    return this.name;
  }

  public void apply(Item to) {
    owner = to;

    AtlasRegion ar = new AtlasRegion(this.getTexture());
    ar.setRegionWidth(to.getTexture().getRegionWidth());
    ar.setRegionHeight(to.getTexture().getRegionHeight());
    to.setTexture(ar);
  }

  @Override
  public void write(Json json) {
    json.writeValue("name", name);
    json.writeValue("texture", texture);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    this.name = jsonData.getString("name", "");
    this.texture = jsonData.getString("texture", "");
    int pngInd = this.texture.lastIndexOf(".png");
    if (pngInd != -1){
      this.texture = this.texture.substring(0, pngInd);
    }
  }

  public AtlasRegion getTexture() {
    return AssetManager.instance().getRegion(this.texture);
  }


  @Override
  public MenuEntry getDisplay(Menu parent){
    return new MenuEntryBuilder(parent, getStyleName()).onSelect(() -> {
      apply(owner);
    }).build();
  }
}

