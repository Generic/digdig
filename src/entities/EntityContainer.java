package entities;

import java.util.ArrayList;
import java.util.List;

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
	
	public void update(float deltaTime) {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.update(deltaTime);
		}
	}
	
	public void render() {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.render();
		}
	}
	
}
