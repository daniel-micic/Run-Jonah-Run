/*
 * Bubble
 * 
 * implementation of collectible shield bonus - bubble
 * shows up on random X coordinate 
 * GameScene checks contact of this sprite with player - activates bonus
 */

package com.jodabrothers.jonah.object;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.manager.ResourcesManager;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;

public class Bubble extends PixelPerfectSprite {
	// state variable active/inactive
	private boolean active;	
	
	// X axis movement
	private float camera_speed;

	public Bubble(float pX, float pY, 
			PixelPerfectTextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		active = true;	
		camera_speed = ResourcesManager.getInstance().getCameraSpeed();
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void setInactive() {
		registerEntityModifier(new ScaleAtModifier(0.2f, 0.5f, 0.0f, 0.5f, 0.5f) {
			@Override
			protected void onModifierFinished(IEntity pItem) {
				setVisible(false);
				super.onModifierFinished(pItem);
			}		
		});	
		active = false;
	}
	
	/*
	 * called by engine on every frame
	 * simulates movement of bubble with ground
	 */
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		setX(getX()-camera_speed);
		super.onManagedUpdate(pSecondsElapsed);
	}
	
}
