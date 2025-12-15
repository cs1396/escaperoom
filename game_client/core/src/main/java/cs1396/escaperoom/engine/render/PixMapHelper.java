package cs1396.escaperoom.engine.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class PixMapHelper {

  // https://stackoverflow.com/a/56654438
  public static Pixmap fromTextureRegion(AtlasRegion region, int scale) {
    Texture texture = region.getTexture();
    TextureData textureData = texture.getTextureData();

    if (!textureData.isPrepared()) {
      textureData.prepare();
    }

    Pixmap texturePixmap = textureData.consumePixmap();
    Pixmap ret = new Pixmap(region.getRegionWidth() * scale,
        region.getRegionHeight() * scale,
        texturePixmap.getFormat());

    int srcX = region.getRegionX();
    int srcY = region.getRegionY();
    int srcWidth = region.getRegionWidth();
    int srcHeight = region.getRegionHeight();

    if (region.isFlipY()) {
      ret.drawPixmap(texturePixmap,
          srcX, srcY + srcHeight, srcWidth + 1, -srcHeight - 1, 
          0, 0, ret.getWidth(), ret.getHeight());
    } else {
      ret.drawPixmap(texturePixmap,
          srcX, srcY, srcWidth + 1, srcHeight + 1,
          0, 0, ret.getWidth(), ret.getHeight());
    }

    return ret;
  }
}
