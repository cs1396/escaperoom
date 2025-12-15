package cs1396.escaperoom.game.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.game.entities.properties.TiledBrushable.TileType;
import cs1396.escaperoom.engine.types.Size;

public class FowTile {
  public static FowTile[][] fowTiles;
  public static Size gridSize;
  TileType inner = TileType.CENTER;
  int textureOriginX, textureOriginY;
  public AtlasRegion texture;

  public FowTile() {
    texture = new AtlasRegion(AssetManager.instance().getRegion("fow_tilemap"));
    textureOriginX = texture.getRegionX();
    textureOriginY = texture.getRegionY();
    Size offset = inner.getOffset();
    setRegion(offset.width, offset.height, 16, 16);
  }

  public void setRegion(int x, int y, int width, int height) {
    texture.setRegion(textureOriginX + x, textureOriginY + y, width, height);
  }

  public void setOrientation(TileType tileType) {
    this.inner = tileType;
    Size offset = inner.getOffset();
    setRegion(offset.width, offset.height, 16, 16);
  }

  private int getAdjancencies(boolean[][] revealed, Vector2 position, boolean recurse) {
    int adjancencies = 0;
    int checked = 0;

    Vector2 pos;
    for (int yoff = 1; yoff >= -1; yoff--) {
      for (int xoff = -1; xoff <= 1; xoff++) {
        if (xoff == 0 && yoff == 0)
          continue;

        pos = position.cpy();
        pos.x += xoff;
        pos.y += yoff;

        if (pos.x < 0 || pos.x >= gridSize.width || pos.y < 0 || pos.y >= gridSize.height) {
          adjancencies = adjancencies | (1 << (7 - checked));
          checked++;
          continue;
        }

        if (!revealed[(int) pos.y][(int) pos.x]) {
          adjancencies = adjancencies | (1 << (7 - checked));
        }
        checked++;

        if (recurse)
          updateTiles(revealed, pos, false);
      }
    }

    return adjancencies;
  }

  public void updateTiles(boolean[][] revealed, Vector2 position, boolean recurse) {
    if (!revealed[(int) position.y][(int) position.x]) {
      FowTile tilable = fowTiles[(int) position.y][(int) position.x];
      switch (getAdjancencies(revealed, position, recurse)) {
        // case of large block
        case 0b00001011:
        case 0b00001111:
        case 0b00101011:
        case 0b00101111:
        case 0b10101111:
        case 0b10001111:
        case 0b10101011:
        case 0b10001011:
          tilable.setOrientation(TileType.BLOCK_TL);
          break;
        case 0b00011111:
        case 0b00111111:
        case 0b10011111:
        case 0b10111111:
          tilable.setOrientation(TileType.BLOCK_T);
          break;
        case 0b00010110:
        case 0b00010111:
        case 0b10010110:
        case 0b10010111:
        case 0b10110111:
        case 0b00110111:
        case 0b10110110:
        case 0b00110110:
          tilable.setOrientation(TileType.BLOCK_TR);
          break;
        case 0b01101011:
        case 0b11101011:
        case 0b01101111:
        case 0b11101111:
          tilable.setOrientation(TileType.BLOCK_L);
          break;
        case 0b11111111:
          tilable.setOrientation(TileType.CENTER);
          break;
        case 0b11010110:
        case 0b11010111:
        case 0b11110110:
        case 0b11110111:
          tilable.setOrientation(TileType.BLOCK_R);
          break;
        case 0b01101000:
        case 0b01101001:
        case 0b11101000:
        case 0b11101001:
        case 0b11101101:
        case 0b01101101:
        case 0b11101100:
        case 0b01101100:
          tilable.setOrientation(TileType.BLOCK_BL);
          break;
        case 0b11111000:
        case 0b11111100:
        case 0b11111001:
        case 0b11111101:
          tilable.setOrientation(TileType.BLOCK_B);
          break;
        case 0b11010000:
        case 0b11010100:
        case 0b11110000:
        case 0b11110100:
        case 0b11110101:
        case 0b11010101:
        case 0b11110001:
        case 0b11010001:
          tilable.setOrientation(TileType.BLOCK_BR);
          break;
        case 0b01111111:
          tilable.setOrientation(TileType.BLOCK_NO_TL);
          break;
        case 0b11011111:
          tilable.setOrientation(TileType.BLOCK_NO_TR);
          break;
        case 0b11111011:
          tilable.setOrientation(TileType.BLOCK_NO_BL);
          break;
        case 0b11111110:
          tilable.setOrientation(TileType.BLOCK_NO_BR);
          break;

        // case of some line
        case 0b00001000:
        case 0b00001001:
        case 0b00101000:
        case 0b00101001:
        case 0b10101000:
        case 0b00101100:
        case 0b10101001:
        case 0b00101101:
        case 0b10101101:
        case 0b00001101:
        case 0b10001100:
        case 0b00001100:
        case 0b10001000:
        case 0b10101100:
        case 0b10001101:
          tilable.setOrientation(TileType.LINE_CAP_L);
          break;
        case 0b00010000:
        case 0b00010100:
        case 0b10010000:
        case 0b10010100:
        case 0b10110000:
        case 0b10010001:
        case 0b10110100:
        case 0b10010101:
        case 0b10110101:
        case 0b00010101:
        case 0b00110001:
        case 0b00010001:
        case 0b00110000:
        case 0b10110001:
        case 0b00110100:
        case 0b00110101:
          tilable.setOrientation(TileType.LINE_CAP_R);
          break;
        case 0b00011000:
        case 0b10011100:
        case 0b10111100:
        case 0b10011101:
        case 0b10111101:
        case 0b00111001:
        case 0b00111101:
        case 0b10111001:
        case 0b00011001:
        case 0b00011100:
        case 0b10011000:
        case 0b00111000:
        case 0b10111000:
        case 0b00011101:
        case 0b00111100:
        case 0b10011001:
          tilable.setOrientation(TileType.LINE_HORI);
          break;
        case 0b00000010:
        case 0b00000110:
        case 0b00000011:
        case 0b00000111:
        case 0b00100011:
        case 0b10000011:
        case 0b00100111:
        case 0b10000110:
        case 0b10000111:
        case 0b10100111:
        case 0b10100010:
        case 0b00100010:
        case 0b10000010:
        case 0b00100110:
        case 0b10100011:
        case 0b10100110:
          tilable.setOrientation(TileType.LINE_CAP_T);
          break;
        case 0b01000000:
        case 0b11000000:
        case 0b01100000:
        case 0b11100000:
        case 0b01100001:
        case 0b01100100:
        case 0b11100001:
        case 0b11000100:
        case 0b11100100:
        case 0b11100101:
        case 0b01000101:
        case 0b01000001:
        case 0b01000100:
        case 0b01100101:
        case 0b11000101:
        case 0b11000001:
          tilable.setOrientation(TileType.LINE_CAP_B);
          break;
        case 0b01000010:
        case 0b11100010:
        case 0b11100011:
        case 0b11100110:
        case 0b11100111:
        case 0b01000111:
        case 0b11000111:
        case 0b01100111:
        case 0b01100010:
        case 0b01000110:
        case 0b11000010:
        case 0b01000011:
        case 0b11000110:
        case 0b01100011:
        case 0b11000011:
        case 0b01100110:
          tilable.setOrientation(TileType.LINE_VERT);
          break;

        case 0b01010010:
        case 0b01110011:
        case 0b01010011:
        case 0b01110010:
          tilable.setOrientation(TileType.T_L);
          break;
        case 0b11001110:
        case 0b01001010:
        case 0b11001010:
        case 0b01001110:
          tilable.setOrientation(TileType.T_R);
          break;
        case 0b01011000:
        case 0b01011101:
        case 0b01011100:
        case 0b01011001:
          tilable.setOrientation(TileType.T_T);
          break;
        case 0b10111010:
        case 0b00011010:
        case 0b00111010:
        case 0b10011010:
          tilable.setOrientation(TileType.T_B);
          break;
        case 0b01011010:
          tilable.setOrientation(TileType.PLUS);
          break;

        case 0b01001000:
        case 0b01001100:
        case 0b01001001:
        case 0b11001000:
        case 0b11001001:
        case 0b11001101:
        case 0b11001100:
        case 0b01001101:
          tilable.setOrientation(TileType.CORNER_BL);
          break;
        case 0b00001010:
        case 0b10001010:
        case 0b00101010:
        case 0b00001110:
        case 0b00101110:
        case 0b10101110:
        case 0b10101010:
        case 0b10001110:
          tilable.setOrientation(TileType.CORNER_TL);
          break;
        case 0b00010010:
        case 0b00110010:
        case 0b10010010:
        case 0b00010011:
        case 0b10010011:
        case 0b10110011:
        case 0b00110011:
        case 0b10110010:
          tilable.setOrientation(TileType.CORNER_TR);
          break;
        case 0b01010000:
        case 0b01010001:
        case 0b01010100:
        case 0b01110000:
        case 0b01110100:
        case 0b01110101:
        case 0b01010101:
        case 0b01110001:
          tilable.setOrientation(TileType.CORNER_BR);
          break;

        case 0b11010010:
        case 0b11110010:
        case 0b11010011:
        case 0b11110011:
          tilable.setOrientation(TileType.RIGHT_CORNER_BL);
          break;
        case 0b01101010:
        case 0b11101010:
        case 0b01101110:
        case 0b11101110:
          tilable.setOrientation(TileType.LEFT_CORNER_BR);
          break;
        case 0b01111000:
        case 0b01111001:
        case 0b01111100:
        case 0b01111101:
          tilable.setOrientation(TileType.BOT_CORNER_TL);
          break;
        case 0b00011011:
        case 0b00111011:
        case 0b10011011:
        case 0b10111011:
          tilable.setOrientation(TileType.TOP_CORNER_BL);
          break;
        case 0b01001011:
        case 0b01001111:
        case 0b11001011:
        case 0b11001111:
          tilable.setOrientation(TileType.LEFT_CORNER_TR);
          break;
        case 0b01010110:
        case 0b01010111:
        case 0b01110110:
        case 0b01110111:
          tilable.setOrientation(TileType.RIGHT_CORNER_TL);
          break;
        case 0b00011110:
        case 0b10011110:
        case 0b00111110:
        case 0b10111110:
          tilable.setOrientation(TileType.TOP_CORNER_BR);
          break;
        case 0b11011000:
        case 0b11011100:
        case 0b11011001:
        case 0b11011101:
          tilable.setOrientation(TileType.BOT_CORNER_TR);
          break;

        case 0b11011010:
          tilable.setOrientation(TileType.CORNER_NOT_TL);
          break;
        case 0b01111010:
          tilable.setOrientation(TileType.CORNER_NOT_TR);
          break;
        case 0b01011011:
          tilable.setOrientation(TileType.CORNER_NOT_BR);
          break;
        case 0b01011110:
          tilable.setOrientation(TileType.CORNER_NOT_BL);
          break;

        case 0b11111010:
          tilable.setOrientation(TileType.CORNER_DB_B);
          break;
        case 0b01111011:
          tilable.setOrientation(TileType.CORNER_DB_L);
          break;
        case 0b11011110:
          tilable.setOrientation(TileType.CORNER_DB_R);
          break;
        case 0b01011111:
          tilable.setOrientation(TileType.CORNER_DB_T);
          break;

        case 0b01111110:
          tilable.setOrientation(TileType.CORNER_DIAG_BR);
          break;
        case 0b11011011:
          tilable.setOrientation(TileType.CORNER_DIAG_TR);
          break;
        case 0b00000000:
          tilable.setOrientation(TileType.SOLO);
          break;

        default:
          tilable.setOrientation(TileType.CENTER);
          break;
      }
    }
  }

}
