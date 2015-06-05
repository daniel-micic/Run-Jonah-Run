/*
 * Player
 * 
 * Element representing controlled sprite
 * Simulation of player sprite - snail is implemented using state-machine.
 * Player input managed in GameScene, communication handled by setMove() method
 * Holds score value - traveled distance
 * Player sprite is animated (8 tiles), animation speed changes according to speed of movement
 */

package com.jodabrothers.jonah.object;

import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;
import com.jodabrothers.jonah.manager.ResourcesManager.LEVEL;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectAnimatedSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTiledTextureRegion;

public class Player extends PixelPerfectAnimatedSprite {

	// state machine states
	public enum Move {LEFT, RIGHT, STOP};
	
	// boost bonus
    private boolean boost = false;
    
    // position limitations, score value, environmental speed
    private float minX;
    private float maxX;
    private float distance;
    private float camera_speed;
    
    // move state initialization
    private Move mMove = Move.STOP;

    
	public Player(PixelPerfectTiledTextureRegion pTextureRegion, VertexBufferObjectManager vertexBufferObjectManager) 
	{
		super(C.CAMERAWIDTH / 2, C.GROUND + (C.PLAYERHEIGHT / 2) - 0.5f , pTextureRegion, vertexBufferObjectManager);
		// position limits initialization
		minX = 0 + C.PLAYERWIDTH/2;
		maxX = C.CAMERAWIDTH - C.PLAYERWIDTH/2;
		
		// animation speed (Move.STOP)
		animate(70);
		
		//score initialization
		distance = 0.0f;
				
		camera_speed = ResourcesManager.getInstance().getCameraSpeed();
	}
	
	// boost bonus activation
	public void setBoost(boolean pBoost) 
	{
		this.boost = pBoost;
	}
	
	/*
	 *  external state (move) setting called by GameScene
	 *  sets animation speed
	 */	
	public void setMove(Move pMove) 
	{
		switch (pMove) {
		case LEFT:
			// simulates stopping of snail
			if (mMove != Move.LEFT) {
				setCurrentTileIndex(0);
				stopAnimation();
			}
			break;
		case RIGHT:
			// simulates faster movement 
			if (mMove != Move.RIGHT)
				animate(50);
			break;
		case STOP:
			// simulates normal movement
			if (mMove != Move.STOP) 
				animate(70);
			boost = false;
			break;
				
		}
		this.mMove = pMove;	
	}
	
	public float getDistance() {
		return distance;
	}	

	/*
	 * onManagedUpdate contains state machine of player sprite
	 * movement of snail is simulated by changing its X coordinate
	 * LEFT - state representing stopping or backing up (according to difficulty)  
	 * 		X coordinate changed by 
	 * 			HARD : -camera speed (-0.7f px) - stops
	 * 			NORMAL, EASY: -1.5f px - backs up
	 * 			BOOST BONUS: -2.5f px 			
	 * RIGHT - state representing sprinting to the right  
	 * 		X coordinate changed by 
	 * 			NO BOOST: 2f px 
	 * 			BOOST: 4f px
	 * STOP - default state that represents player movement at normal speed	 
	 * 		X coordinate is not changed, 
	 * 						   
	 */
	protected void onManagedUpdate(final float pSecondsElapsed)
    {
		// score addition in actual frame
		float distance_change = 0.0f;
		
		// state machine
		switch (mMove) {
		case LEFT :
			// movement simulation
			// bonus detection
			if (boost) {
				setX(getX() - 2.5f);
				distance_change = -0.0015f;
			}
			else {
				// difficulty detection
				if (ResourcesManager.getInstance().getLevel() == LEVEL.HARD)
					setX(getX() - camera_speed);
				else {
					setX(getX() - 1.5f);
					distance_change = -0.0005f;
				}
			}			
			break;
		case RIGHT :
			// movement simulation
			// bonus detection
			if (boost) {
				setX(getX() + 4f);
				distance_change = 0.005f; 
			}
			else {
				setX(getX() + 2f);
				distance_change = 0.003f;
			}
			break;
		case STOP :
			// movement simulation
			distance_change = 0.001f;
			break;
		} 
		
		// limits control, corrections
		if (this.getX() >= maxX)  {
			this.setX(maxX);
			this.boost = false;
			distance_change = 0.001f;
		}

		if (this.getX() <= minX) {
			this.setX(minX);
			distance_change = 0.001f;
		}
		 
		// score update
		distance += distance_change;
        super.onManagedUpdate(pSecondsElapsed);
    }
}
