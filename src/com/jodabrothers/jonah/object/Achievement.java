/*
 * Achievement
 * 
 * Achievements are shown on every 5 meters passed
 * Simple array-list of animated Sprites 
 */

package com.jodabrothers.jonah.object;

import java.util.ArrayList;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;

public class Achievement{

	private ArrayList<AnimatedSprite> sprites;

	// Constructor - creates array-list of Animated Sprites
	public Achievement(ResourcesManager resourcesManager, VertexBufferObjectManager vbom) {
		sprites = new ArrayList<AnimatedSprite>();
		AnimatedSprite plane = new AnimatedSprite(C.CAMERAWIDTH + 200, 400, resourcesManager.plane_region, vbom);		
		AnimatedSprite balloon = new AnimatedSprite(C.CAMERAWIDTH + 200, 400, resourcesManager.balloon_region, vbom);
		AnimatedSprite ufo = new AnimatedSprite(C.CAMERAWIDTH + 200, 400, resourcesManager.ufo_region, vbom);
		sprites.add(plane);		
		sprites.add(balloon);
		sprites.add(ufo);
	}
	
	public ArrayList<AnimatedSprite> getSprites() {
		return sprites;
	}

}
