package entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class EntityContainer {
	private static EntityContainer instance;
	
	public static EntityContainer getInstance() {
		if (instance == null) {
			instance = new EntityContainer();
		}
		return instance;
	}
	

	private List<Entity> entities;
	
	private EntityContainer() {
		entities = new ArrayList<Entity>();
	}
	
	// TODO: getEntities in (x, y, x2, y2) bounds
	// TODO: getEntities of type X
	
	/**
	 * Calls the update function of all Entitys contained within the container
	 * @param deltaTime Time changed since last update
	 */
	public void update(float deltaTime) {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.update(deltaTime);
		}
	}
	
	/**
	 * Returns List<Entity> where all the Entitys' x,y are within the rectangle created by points (x1, y1) and (x2, y2)
	 * @param x1 
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public List<Entity> getEntities(float x1, float y1, float x2, float y2) {
		Rectangle bounds = new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
		return getEntities(bounds);
	}
	
	public List<Entity> getEntities(Rectangle bounds) {
		List<Entity> ret = new ArrayList<Entity>();
		
		for (Entity e : entities) {
			if (bounds.contains(e.getPosition())) {
				ret.add(e);
			}
		}
		return ret;
		
	}
	
	/**
	 * Calls the render function of all Entitys contained within the container
	 */
	public void render() {
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).render();
		}
	}
	
	
}
