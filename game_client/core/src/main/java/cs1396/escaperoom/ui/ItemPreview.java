package cs1396.escaperoom.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ItemPreview extends Image {
    TextureRegion texture;

    public ItemPreview(TextureRegion region) {
        super(region);
        this.texture = region;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX() , getY() , 0, 0, getWidth(), getHeight(), 1, 1,
                getRotation());
    }
}
