
package dig;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;
 
/**
 * The game's main class, called as application events are fired.
 */
public class DigGame
    implements
        ApplicationListener
{

	
	public static class Hero {
		static float WIDTH;
		static float HEIGHT;
		static float MAX_VELOCITY = 10f;
		static float JUMP_VELOCITY = 25f;
		static float DAMPING = 0.975f;
		static float ACCEL = 15f;
 
		enum State
		{
			Standing, Walking, Jumping
		}
 
		final Vector2 position = new Vector2();
		final Vector2 velocity = new Vector2();
		State state = State.Walking;
		float stateTime = 0;
		boolean facesRight = true;
		boolean grounded = false;
	}
	
	public static class Explosion {
		static float WIDTH;
		static float HEIGHT;
		
		enum State {
			Exploding
		}
		
		final Vector2 position = new Vector2();
		State state = State.Exploding;
		float stateTime = 0;
	}
	
    // constant useful for logging
    public static final String LOG = DigGame.class.getSimpleName();
 
    // a libgdx helper class that logs the current FPS each second
    private FPSLogger fpsLogger;
    private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Animation stand;
	private Animation walk;
	private Animation jump;
	private Animation circles;
	private Hero hero;
	private Explosion explosion;
	private Pool<Rectangle> rectPool = new Pool<Rectangle>()
	{
		@Override
		protected Rectangle newObject()
		{
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<Rectangle>();
	
	private float scale = 1 / 16f;
 
	// arbitrary value, negative is down, positive is up
	// -1 feels like a "natural" amount
	private static final float GRAVITY = -1f;
 

	TextureAtlas atlas;
			
    @Override
    public void create()
    {
        Gdx.app.log( DigGame.LOG, "Creating game" );
        fpsLogger = new FPSLogger();
        
		// load all the different textures into memory
		atlas = new TextureAtlas(new FileHandle(new File("data/heroTextures.txt")));
		
		TextureRegion standText = atlas.findRegion("p1_stand");
		stand = new Animation(0, standText);
		jump = new Animation(0, atlas.findRegion("p1_jump"));
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
		
		walk = new Animation(0.10f, walking);
		walk.setPlayMode(Animation.LOOP);
 
		// figure out the width and height of the koala for collision
		// detection and rendering by converting a koala frames pixel
		// size into world units (1 unit == 16 pixels)
		Hero.WIDTH = scale * standText.getRegionWidth() / 2;
		Hero.HEIGHT = scale * standText.getRegionHeight() / 2;
		
		//load circle animation
		//get textures from file
		atlas = new TextureAtlas(new FileHandle(new File("data/CircleTextures.txt")));
		for (AtlasRegion t : atlas.getRegions()) {
			Gdx.app.log(DigGame.LOG, "Atlas region with name: " + t.name + " loaded.");
		}
		//make into animation
		//LOOP_RANDOM doesn't seem to work as I expected it to...
		circles = new Animation (0.2f, atlas.getRegions(), Animation.LOOP);
 
		// load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
		// 1 unit is 16 pixels so that one unit is one tile
		map = new TmxMapLoader().load("data/level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, scale);
 
		// create an orthographic camera, shows us 30x20 units of the world
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 30, 20);
		camera.update();
 
		// create the Koala we want to move around the world
		hero = new Hero();
		hero.position.set(20, 20);
		
		//create explosion
		explosion = new Explosion();
		explosion.position.set(5, 5);
    }
 
    @Override
    public void resize(
        int width,
        int height )
    {
        Gdx.app.log( DigGame.LOG, "Resizing game to: " + width + " x " + height );
    }
 
    @Override
    public void render()
    {
		// clear the screen
		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
		// get the delta time
		float deltaTime = Gdx.graphics.getDeltaTime();
 
		// update the koala (process input, collision detection, position update)
		updateHero(deltaTime);
 
		// let the camera follow the koala, x-axis only
		camera.position.x = hero.position.x;
		camera.update();
 
		// set the tile map renderer view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();
 
		// render the koala
		renderHero(deltaTime);
		
		// render explosion
		renderExplosion(deltaTime);
		
        // output the current FPS
        fpsLogger.log();
    }
    
    //private Vector2 tmp = new Vector2();

    private void updateHero(float deltaTime)
	{
		if (deltaTime == 0)
			return;
		hero.stateTime += deltaTime;
 
		// check input and apply to velocity & state
		if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.Q) || isTouched(0.75f, 1)) && hero.grounded)
		{
			hero.velocity.y += Hero.JUMP_VELOCITY;
			hero.state = Hero.State.Jumping;
			hero.grounded = false;
		}
 
		if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f))
		{
			//hero.velocity.x = -Hero.MAX_VELOCITY;
			hero.velocity.x -= Hero.ACCEL * deltaTime;
			if (hero.grounded)
				hero.state = Hero.State.Walking;
			hero.facesRight = false;
		}
 
		if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f))
		{
			//hero.velocity.x = Hero.MAX_VELOCITY;
			hero.velocity.x += Hero.ACCEL * deltaTime;
			if (hero.grounded)
				hero.state = Hero.State.Walking;
			hero.facesRight = true;
		}
 
		Gdx.app.log(DigGame.LOG, "hero.velocity.x = " + hero.velocity.x);
		
		// apply gravity if we are falling
		hero.velocity.add(0, GRAVITY);
 
		// clamp the velocity to the maximum, x-axis only
		if (Math.abs(hero.velocity.x) > Hero.MAX_VELOCITY)
		{
			hero.velocity.x = Math.signum(hero.velocity.x) * Hero.MAX_VELOCITY;
		}
 
		// clamp the velocity to 0 if it's < 1, and set the state to standing
		/*
		if (Math.abs(hero.velocity.x) < 0.001)
		{
			hero.velocity.x = 0;
			if (hero.grounded)
				hero.state = Hero.State.Standing;
		}*/
 
		// multiply by delta time so we know how far we go
		// in this frame
		hero.velocity.scl(deltaTime);
 
		// perform collision detection & response, on each axis, separately
		// if the hero is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		Rectangle heroRect = rectPool.obtain();
		heroRect.set(hero.position.x, hero.position.y, Hero.WIDTH, Hero.HEIGHT);
		int startX, startY, endX, endY;
		if (hero.velocity.x > 0)
		{
			startX = endX = (int) (hero.position.x + Hero.WIDTH + hero.velocity.x);
		}
		else
		{
			startX = endX = (int) (hero.position.x + hero.velocity.x);
		}
		startY = (int) (hero.position.y);
		endY = (int) (hero.position.y + Hero.HEIGHT);
		getTiles(startX, startY, endX, endY, tiles);
		heroRect.x += hero.velocity.x;
		for (Rectangle tile : tiles)
		{
			if (heroRect.overlaps(tile))
			{
				hero.velocity.x = 0;
				break;
			}
		}
		heroRect.x = hero.position.x;
 
		// if the hero is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom
		if (hero.velocity.y > 0)
		{
			startY = endY = (int) (hero.position.y + Hero.HEIGHT + hero.velocity.y);
		}
		else
		{
			startY = endY = (int) (hero.position.y + hero.velocity.y);
		}
		startX = (int) (hero.position.x);
		endX = (int) (hero.position.x + Hero.WIDTH);
		getTiles(startX, startY, endX, endY, tiles);
		heroRect.y += hero.velocity.y;
		for (Rectangle tile : tiles)
		{
			if (heroRect.overlaps(tile))
			{
				// we actually reset the hero y-position here
				// so it is just below/above the tile we collided with
				// this removes bouncing :)
				if (hero.velocity.y > 0)
				{
					hero.position.y = tile.y - Hero.HEIGHT;
					// we hit a block jumping upwards, let's destroy it!
					TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
					layer.setCell((int) tile.x, (int) tile.y, null);
					
				}
				else
				{
					hero.position.y = tile.y + tile.height;
					// if we hit the ground, mark us as grounded so we can jump
					hero.grounded = true;
				}
				hero.velocity.y = 0;
				break;
			}
			//prevents jumping mid air
			else {
				hero.grounded = false;
			}
		}
		rectPool.free(heroRect);
 
		// unscale the velocity by the inverse delta time and set 
		// the latest position
		hero.position.add(hero.velocity);
		hero.velocity.scl(1 / deltaTime);
 
		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		hero.velocity.x *= Hero.DAMPING;
 
	}

    
	private boolean isTouched(float startX, float endX)
	{
		// check if any finge is touch the area between startX and endX
		// startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
		for (int i = 0; i < 2; i++)
		{
			float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
			if (Gdx.input.isTouched(i) && (x >= startX && x <= endX))
			{
				return true;
			}
		}
		return false;
	}
    
	private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles)
	{
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
		rectPool.freeAll(tiles);
		tiles.clear();
		for (int y = startY; y <= endY; y++)
		{
			for (int x = startX; x <= endX; x++)
			{
				Cell cell = layer.getCell(x, y);
				if (cell != null)
				{
					Rectangle rect = rectPool.obtain();
					rect.set(x, y, 1, 1);
					tiles.add(rect);
				}
			}
		}
	}
	
	private void renderHero(float deltaTime)
	{
		// based on the Hero state, get the animation frame
		TextureRegion frame = null;
		switch (hero.state)
		{
			case Standing:
				frame = stand.getKeyFrame(hero.stateTime);
				break;
			case Walking:
				frame = walk.getKeyFrame(hero.stateTime);
				break;
			case Jumping:
				frame = jump.getKeyFrame(hero.stateTime);
				break;
			default:
				Gdx.app.log(DigGame.LOG, "PANIC! State = " + hero.state.name());
		}
 
		// draw the Hero, depending on the current velocity
		// on the x-axis, draw the Hero facing either right
		// or left
		SpriteBatch batch = renderer.getSpriteBatch();
		batch.begin();
		if (hero.facesRight)
		{
			batch.draw(frame, hero.position.x, hero.position.y, Hero.WIDTH, Hero.HEIGHT);
		}
		else
		{
			batch.draw(frame, hero.position.x + Hero.WIDTH, hero.position.y, -Hero.WIDTH, Hero.HEIGHT);
		}
		batch.end();
	}
	
	private void renderExplosion (float deltaTime) {
		explosion.stateTime = explosion.stateTime + deltaTime;
		TextureRegion frame = circles.getKeyFrame(explosion.stateTime);
		SpriteBatch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, explosion.position.x, explosion.position.y, 1, 1);
		batch.end();
	}
 
	
    @Override
    public void pause()
    {
        Gdx.app.log( DigGame.LOG, "Pausing game" );
    }
 
    @Override
    public void resume()
    {
        Gdx.app.log( DigGame.LOG, "Resuming game" );
    }
 
    @Override
    public void dispose()
    {
        Gdx.app.log( DigGame.LOG, "Disposing game" );
    }
}