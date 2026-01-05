package cs1396.escaperoom.tests.files;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.badlogic.gdx.Gdx;
import cs1396.escaperoom.tests.GdxTestExtension;


@ExtendWith(GdxTestExtension.class)
public class FileExists {
  @Test
	public void noteJsonExists() {
		assertTrue(Gdx.files
				.internal("objects/Clues/Note.json").exists(), "Expected objects/Clues/Note.json to exist in the assets folder");
	}
}
