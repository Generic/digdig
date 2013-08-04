package entities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import dig.DigGame;;

public class EntityFactory {
	private static EntityFactory instance;
	
	public static EntityFactory getInstance() {
		if (instance == null) {
			instance = new EntityFactory();
		}
		return instance;
	}
	
	private Map<String, Entity> entities; // Name, Entity
	
	private EntityFactory() {
		entities = new HashMap<String, Entity>();
		initialize();
	}
	
	public void initialize() {
		Entity e = null;
		// Initialize Hero

		// load all the different textures into memory
		TextureAtlas atlas = new TextureAtlas(new FileHandle(new File("data/heroTextures.txt")));
		
		TextureRegion standText = atlas.findRegion("p1_stand");
		Animation stand = new Animation(0, standText);
		Animation jump = new Animation(0, atlas.findRegion("p1_jump"));
		Array<TextureRegion> walking = new Array<TextureRegion>(11);
		
		for (int i = 1; i <= 11; i++) {
			// i < 10 ? "0" + i : i
			// puts a 0 in front of the number if it is less than 10 to get 01, 02, 03... 09, 10, 11
			String regionName = "p1_walk" + (i < 10 ? "0" + i : i);
			TextureRegion tempRegion = atlas.findRegion(regionName);
			if (tempRegion == null) {
				Gdx.app.log(DigGame.LOG, "Null region: " + regionName);
			}
			walking.add(tempRegion);
		}
		
		Animation walk = new Animation(0.10f, walking);
		walk.setPlayMode(Animation.LOOP);
		
		e = new Hero();
		e.addAnimation("stand", stand);
		e.addAnimation("jump", jump);
		e.addAnimation("walk", walk);
		

		// size into world units (1 unit == 16 pixels)
		e.setWidth(GameConstants.SCALE * standText.getRegionWidth() / 2);
		e.setHeight(GameConstants.SCALE * standText.getRegionHeight() / 2);
		
		
	}
}
