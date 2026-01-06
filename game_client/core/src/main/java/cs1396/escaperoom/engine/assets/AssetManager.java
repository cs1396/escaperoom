package cs1396.escaperoom.engine.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Result.IOErr;
import cs1396.escaperoom.engine.types.Result.Ok;

/**
 * A wrapper around {@link com.badlogic.gdx.assets.AssetManager}
 */
public class AssetManager extends com.badlogic.gdx.assets.AssetManager {
  Logger log = Logger.getLogger(AssetManager.class.getName());
  Optional<UserAtlas> userAtlas = Optional.empty();
  TextureAtlas defaultAtlas;
  HashMap<String, AtlasRegion> loadedTextures = new HashMap<>();

  public record UserAtlas(TextureAtlas atlas, String filename){
    @Override
    public final boolean equals(Object arg0) {
      if (arg0 instanceof UserAtlas ua){
        return ua.atlas == this.atlas;
      }
      return false;
    }
  };

  private static AssetManager mgr;

  /**
   * @return the singleton {@link AssetManager}
   */
  public static AssetManager instance(){
    if (mgr == null){
      mgr = new AssetManager();
    }
    return mgr;
  }


  private AssetManager() { 
    load("texture_atlas/textureAtlas.atlas", TextureAtlas.class);
    finishLoadingAsset("texture_atlas/textureAtlas.atlas");
    this.defaultAtlas = get("texture_atlas/textureAtlas.atlas");
  }

  /**
   * @param atlas to register
   */
  public void registerUserAtlas(UserAtlas atlas){
    if (userAtlas.filter(ua -> !ua.equals(atlas)).isPresent()){
      log.info("Registering new user atlas -> clearing old user textures");
      clearUserTextures();
    }

    log.info("Setting user atlas to " + atlas.filename);
    this.userAtlas = Optional.of(atlas);
  }

  public void clearUserTextures(){
    log.info("Clearing user textures");
    userAtlas.ifPresent((a) -> {
      // invalidate texture cache
      log.info("User atlas was registered, disposing and unloading atlas");
      invalidateTextureCache();
      unload(a.filename);
    });
    userAtlas = Optional.empty();
  }

  /**
   * Invalidate any cached textures in any of the {@link  TextureAtlas}s
   */
  public void invalidateTextureCache(){
    log.info("Invalidating texture cache");
    loadedTextures.clear();
  }

  public static Result<String, String> textureName(Path path){
    Logger log = Logger.getLogger(AssetManager.class.getName());
    try {
      String mimeType = Files.probeContentType(path);

      if (mimeType == null || !mimeType.equals("image/png")){
        return IOErr.withLog(
          String.format("Path (%s) is not a PNG", path.toString()),
          log,
          Level.INFO
        );
      }

      // SAFETY: `path.getFileName()` will not be null.
      // If it were, an exception would have already been thrown
      // by `Files.probeContentType`
      String fileName = path.getFileName().toString();

      int dotInd = fileName.lastIndexOf(".");

      if (dotInd == -1){
        return IOErr.withLog(
          "Path does not contain a PNG extension: " + path.toString(),
          log
        );
      }

      return new Ok<>(fileName.substring(0, dotInd));

    } catch (IOException ioe){
      return IOErr.withLog(
        "Failed to determine mimetype: " + ioe.getMessage(),
        log
      );
    } catch (SecurityException se){
      return IOErr.withLog(
        "Permission error while determining mimetype: " + se.getMessage(),
        log
      );
    }
  }

  /**
   * @param identifier the identifier of the region
   * @return the {@link AtlasRegion} or a placeholder texture if not found
   */
  public AtlasRegion getRegion(String identifier) {

    AtlasRegion cached = loadedTextures.get(identifier);
    if (cached != null){
      return cached;
    }

    AtlasRegion region = userAtlas.map((ua) -> ua.atlas().findRegion(identifier)).orElse(null);

    if (region != null){
      loadedTextures.put(identifier, region);
      return region;
    }  

    region = defaultAtlas.findRegion(identifier);
    if (region != null){
      loadedTextures.put(identifier, region);
      return region;
    }

    log.warning("Requested texture that does not exist! Requested: " + identifier);
    return defaultAtlas.findRegion("placeholder");
  }

  public Texture loadTextureBlocking(String path){
    try {
      AssetManager.instance().load(path, Texture.class);
      AssetManager.instance().finishLoadingAsset(path);
      return AssetManager.instance().get(path, Texture.class);
    } catch (Exception gdxre) {
      System.err.println("failed to load title img");
      return null;
    }
  }
}
