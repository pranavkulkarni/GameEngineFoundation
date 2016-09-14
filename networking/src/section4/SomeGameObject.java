package section4;

import java.io.*;
public class SomeGameObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int x = 20, y = 20;
	String playerName = "default game object";
	
	public SomeGameObject() {
		
	}
	
	public SomeGameObject(int x, int y, String playerName) {
		this.x = x;
		this.y = y;
		this.playerName = playerName;
	}
	
}
