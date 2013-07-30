package entities;

import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
	private float width, height;
	private float maxVelo, minVelo;
	private float damping; // Slow down
	private State state; 
	
	private final Vector2 position; // Use sets here,
	private final Vector2 velocity;
	private float stateTime; // Time of 
	
	public Entity() {
		this.position = new Vector2();
		this.velocity = new Vector2();
	}
	
	public void update() {
		// Handle physics, general updates
	}
	
	public void render() {
		// Render sprites: Note: can we sprite batch all draws at once or is there a limit? If not open the batch inside of the container and pass it in, if there is then consider the cost of the draws
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public float getMaxVelocity() {
		return maxVelo;
	}
	
	public float getMinVelocity() {
		return minVelo;
	}
	
	public Vector2 getPosition() { // Should this be exposed? Perhaps not once more entity things are added, exposing a vector is dangerous when physics should be handled in update
		return position;
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
}
