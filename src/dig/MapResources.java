package dig;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class MapResources {
	private static MapResources instance;
	
	public static MapResources getInstance() {
		if (instance == null) {
			instance = new MapResources();
		}
		return instance;
	}
	
	private TiledMap map;
	
	public void create() {
		map.
	}
	
	public void dispose() {
		MapResources.instance = null;
		map.dispose();
	}
}
