/*
 * Grass
 * 
 * Class representing grass, grass is referencing point of camera movement by perpetual movement to left  
 */

package com.jodabrothers.jonah.object;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;

public class Grass {
	
	// Two sprites next to each other. When one leaves camera view , its reset onto initial X coordinate
	private Sprite grass1;
	private Sprite grass2;
	private float camera_speed;
	
	// constructor, initialization of both sprites
	public Grass(boolean theme, VertexBufferObjectManager vbom) {

		camera_speed = ResourcesManager.getInstance().getCameraSpeed();
		//sprite creation
		grass1 = new Sprite(C.BACKGROUNDWIDTH/2, 0, theme ? ResourcesManager.getInstance().dark_grass_region : ResourcesManager.getInstance().light_grass_region, vbom) {
			@Override
			protected void onManagedUpdate(float pSecondsElapsed) {
				// perpetual movement to left
				if (getX() <= -C.BACKGROUNDWIDTH/2)
					setX(C.BACKGROUNDWIDTH/2 + C.BACKGROUNDWIDTH);
				else
					setX(getX() - camera_speed);
					
				super.onManagedUpdate(pSecondsElapsed);
			}
		};
		grass1.setY(C.GROUND + (grass1.getHeight()/2));
		
		//sprite creation
		grass2 = new Sprite(C.BACKGROUNDWIDTH/2 + C.BACKGROUNDWIDTH, 0, theme ? ResourcesManager.getInstance().dark_grass_region : ResourcesManager.getInstance().light_grass_region, vbom){
			@Override
			protected void onManagedUpdate(float pSecondsElapsed) {
				// perpetual movement to left
				if (getX() <= -C.BACKGROUNDWIDTH/2)
					setX(C.BACKGROUNDWIDTH/2 + C.BACKGROUNDWIDTH);
				else
					setX(getX() - camera_speed);
				super.onManagedUpdate(pSecondsElapsed);
			}
		};
		grass2.setY(C.GROUND + (grass2.getHeight()/2));
	}

	public Sprite getGrass1() {
		return grass1;
	}

	public Sprite getGrass2() {
		return grass2;
	}
	
	

}
