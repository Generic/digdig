package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

import dig.DigGame;

public class Hero extends Entity {
	static float MAX_VELOCITY = 10f;
	static float JUMP_VELOCITY = 25f;
	static float DAMPING = 0.975f;
	static float ACCEL = 15f;
	
	private boolean facesRight;
	private boolean grounded;
	
	public Hero() {
		super();
		
	}
	
	public void a() {
		return;
	}
	
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if (deltaTime == 0)
			return;
 
		// check input and apply to velocity & state
		if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.Q)) && grounded)
		{
			getVelocity().y += JUMP_VELOCITY;
			setState(State.Jumping);
			grounded = false;
		}
 
		if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A))
		{
			//velocity.x = -MAX_VELOCITY;
			getVelocity().x -= ACCEL * deltaTime;
			if (grounded) {
				setState(State.Walking);
			}
			facesRight = false;
		}
 
		if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D))
		{
			//velocity.x = MAX_VELOCITY;
			getVelocity().x += ACCEL * deltaTime;
			if (grounded) {
				setState(State.Walking);
			}
			facesRight = true;
		}
 
		Gdx.app.log(DigGame.LOG, "velocity.x = " + getVelocity().x);
		
		// apply gravity if we are falling
		getVelocity().add(0, GameConstants.GRAVITY);
 
		// clamp the velocity to the maximum, x-axis only
		if (Math.abs(getVelocity().x) > MAX_VELOCITY)
		{
			getVelocity().x = Math.signum(getVelocity().x) * MAX_VELOCITY;
		}
 
		// clamp the velocity to 0 if it's < 1, and set the state to standing
		/*
		if (Math.abs(velocity.x) < 0.001)
		{
			velocity.x = 0;
			if (grounded)
				state = State.Standing;
		}*/
 
		// multiply by delta time so we know how far we go
		// in this frame
		getVelocity().scl(deltaTime);
 // REPLACE
		// perform collision detection & response, on each axis, separately
		// if the hero is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		Rectangle heroRect = rectPool.obtain();
		heroRect.set(getPosition().x, getPosition().y, WIDTH, HEIGHT);
		int startX, startY, endX, endY;
		if (getVelocity().x > 0)
		{
			startX = endX = (int) (getPosition().x + WIDTH + velocity.x);
		}
		else
		{
			startX = endX = (int) (getPosition().x + velocity.x);
		}
		startY = (int) (getPosition().y);
		endY = (int) (getPosition().y + HEIGHT);
		getTiles(startX, startY, endX, endY, tiles);
		heroRect.x += velocity.x;
		for (Rectangle tile : tiles)
		{
			if (heroRect.overlaps(tile))
			{
				velocity.x = 0;
				break;
			}
		}
		heroRect.x = getPosition().x;
 
		// if the hero is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom
		if (velocity.y > 0)
		{
			startY = endY = (int) (getPosition().y + HEIGHT + velocity.y);
		}
		else
		{
			startY = endY = (int) (getPosition().y + velocity.y);
		}
		startX = (int) (getPosition().x);
		endX = (int) (getPosition().x + WIDTH);
		getTiles(startX, startY, endX, endY, tiles);
		heroRect.y += velocity.y;
		for (Rectangle tile : tiles)
		{
			if (heroRect.overlaps(tile))
			{
				// we actually reset the hero y-getPosition() here
				// so it is just below/above the tile we collided with
				// this removes bouncing :)
				if (velocity.y > 0)
				{
					getPosition().y = tile.y - HEIGHT;
					// we hit a block jumping upwards, let's destroy it!
					TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
					layer.setCell((int) tile.x, (int) tile.y, null);
					
				}
				else
				{
					getPosition().y = tile.y + tile.height;
					// if we hit the ground, mark us as grounded so we can jump
					grounded = true;
				}
				velocity.y = 0;
				break;
			}
			//prevents jumping mid air
			else {
				grounded = false;
			}
		}
		rectPool.free(heroRect);
 
		// unscale the velocity by the inverse delta time and set 
		// the latest getPosition()
		getPosition().add(velocity);
		velocity.scl(1 / deltaTime);
 
		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		velocity.x *= DAMPING;
	}
}
