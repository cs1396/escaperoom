package cs1396.escaperoom.ui;

import java.util.zip.Deflater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;

public class ScreenShotter {
  public static void takeScreenShot(MapMetadata metadata) {

    int TARG_DIM = 800;
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();


    Pixmap pixmap = Pixmap.createFromFrameBuffer((width - TARG_DIM) / 2, (height  - TARG_DIM) / 2, TARG_DIM, TARG_DIM);
    FileHandle fh = new FileHandle(metadata.locations.mapThumbnailPath);
    PixmapIO.writePNG(fh, pixmap, Deflater.BEST_COMPRESSION, true);
    pixmap.dispose();
  }

}
