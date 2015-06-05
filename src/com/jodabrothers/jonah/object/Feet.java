/*
 * Feet
 * 
 * Object representing feet and simulating its movement
 * Simulation of movement is implemented using state machine
 * Feet objects are always paired in one Human object, those are held in data structure of GameScene
 * Feet object is disposed by deleting object Human from GameScene data structure when both feet are out of scene  
 */

package com.jodabrothers.jonah.object;

import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;
import com.jodabrothers.jonah.manager.ResourcesManager.LEVEL;
import com.jodabrothers.jonah.object.Human.Type;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;

public class Feet extends PixelPerfectSprite{
	
	// states of state machine
	public enum State {ROTATE, DESCEND, DESCEND_FLOORED, FLOORED, ASCEND, DELETE};
	
	// object fields
	private State mState; // actual state
	private float rotationAngle = 0; // rotation of foot
	private boolean mCourse; // course of foot - true:left/False:right
	private float left_speedX; // X axis movement speed for left foot
	private float right_speedX; // X axis movement speed for right foot
	private float speedY; // Y axis movement speed
	private float camera_speed;
	private Feet pair_feet;	
	private int maxY; // maximum Y coordinate for feet (different heights for each type of foot)
	private LEVEL level; // Difficulty is, among other ways, set also by speed of feet 
	

	public Feet(float posX, float posY, Type type, State pState, boolean pCourse, 			
			PixelPerfectTextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) 
	{
		super(posX, posY, pTextureRegion, pVertexBufferObjectManager);
		
		level = ResourcesManager.getInstance().getLevel();
		camera_speed = ResourcesManager.getInstance().getCameraSpeed();
		
		// sets max height according to the type of feet
		switch (type) {
		case SNEAKER:
		default:
			maxY = 110;			
			break;
		case HIGH_HEEL:
			maxY = 90;
			break;
		case BOOT:
			maxY = 130;			
			break;
		}
	
		// sets speed of foot according to the difficulty
		// includes background movement
		switch (level) {
		case EASY:
			// simulated speed of foot is 4.0f, real is 4.0 +-camera_speed according to course
			left_speedX = 3.3f;
			right_speedX = 4.7f;
			speedY = 1.8f;
			break;
		case MEDIUM:
			left_speedX = 3.8f;
			right_speedX = 5.2f;
			speedY = 2.33f;
			break;
		case HARD:
			left_speedX = 4f;
			right_speedX = 6f;
			speedY = 3f;		
		}
		
		// course and state initialization - initial state is either FLOORED or ROTATE, paired foot gets the other one ...
		mCourse = pCourse;
		mState = pState;  	
	}
	
	//some basic getters and setters
	public void setState(State pState) {
		mState = pState;
	}
	
	public State getState() {
		return mState;
	}	
		
	public Feet getPair_feet() {
		return pair_feet;
	}

	public void setPair_feet(Feet pair_feet) {
		this.pair_feet = pair_feet;
	}
	
	public boolean getCourse() {
		return mCourse;
	}

	/*
	 * onManagedUpdate contains state machine of foot
	 * movement of foot is simulated by changing its X and Y coordinates and rotating foot accordingly to its state
	 * ROTATE - X coordinate changing by +-speedX
	 * 			Y coordinate is at its maximum
	 * 			rotation by +-0.4f
	 * 			when angle reaches 0f rotation center is changed from heel to the tip of foot
	 * DESCEND - X coordinate changing by +-speedX
	 * 			Y coordinate is changed by -speedY
	 * DESCEND_FLOORED - middle state, contact with player is still game over situation
	 * 			X coordinate is changed by -camera_speed
	 * 			rotation by +-0.4f 
	 * 			rotates until +-8f (height of foot tip is approximately the height of the player sprite) -> changes state into FLOORED
	 * FLOORED - rotation by +-0.4f until the rotation angle is 0, then awaits landing of paired feet. 
	 * 			Afterwards changes rotation center from heel to tip and starts turning again until the angle is +-10f -> changes state on ASCEND
	 * 			contact with player is not game over situation
	 * ASCEND - X coordinate changing by +-speedX
	 * 			Y coordinate is changed by +speedY
	 * DELETE - void		
	 * 						   
	 */
	protected void onManagedUpdate(final float pSecondsElapsed) 
	{
		//state machine
		switch (mState) {
		case ROTATE:
			// rotation center change
			if ((rotationAngle <= 0.3f) && (rotationAngle >= -0.3f))
			{
				if(mCourse)
					setRotationCenter(0f, 0f);
				else
					setRotationCenter(1f, 0f);				
			}
			
			// rotation angle increment/decrement
			if (mCourse) 
				rotationAngle -= 0.4f;
			else 
				rotationAngle += 0.4f;
			
			// rotation simulation 
			setRotation(rotationAngle);
			
			// X coordinate movement simulation
			if (mCourse) 
				setX(getX() + left_speedX);
			else 
				setX(getX() - right_speedX);
			
			
			// state change condition ROTATE -> DESCEND
			if ((mCourse && (rotationAngle <= -10)) || (!mCourse && (rotationAngle >= 10))) 
			{
				setState(State.DESCEND);
			}
			break;
		case DESCEND:
			// X coordinate movement simulation
			if (mCourse) 
				setX(getX() + left_speedX);
			else 
				setX(getX() - right_speedX);
			
			// Y coordinate movement simulation
			setY(getY() - speedY);
			
			// state change condition DESCEND -> DESCEND_FLOORED, corrections		
			if (getY() <= C.GROUND + (C.FEETHEIGHT / 2)) {
				setY(C.GROUND + (C.FEETHEIGHT / 2));				
				setState(State.DESCEND_FLOORED);
			}
			break;
		case DESCEND_FLOORED:
		case FLOORED:		
			// X coordinate movement simulation - enviromental 
			setX(getX()-camera_speed);
			
			// state change condition DESCEND_FLOORED -> FLOORED
			if ((rotationAngle >= -8f) && (rotationAngle <= 8f))
				setState(State.FLOORED);
			
			// simulation of pivot foot, object awaits in non-active state while paired feet is descending
			if ((rotationAngle >= -0.3f) && (rotationAngle <= 0.3f)) {
				
				//corrections
				setRotation(0f);
				
				// rotation center changed
				if(mCourse)
					setRotationCenter(1f, 0f);
				else
					setRotationCenter(0f, 0f);
				
				// awaiting paired feet
				if ((pair_feet.getState() == State.DESCEND_FLOORED) || (pair_feet.getState() == State.DELETE)) {
					if (mCourse) 
						rotationAngle += 0.4f;
					else 
						rotationAngle -= 0.4f;
				}
			} else { // after paired feet touches ground 
				// rotation simulation
				setRotation(rotationAngle);
				
				// rotation angle increment/decrement
				if (mCourse) 
					rotationAngle += 0.4f;
				else 
					rotationAngle -= 0.4f;
				
				// state change condition FLOORED -> ASCEND
				if ((mCourse && (rotationAngle >= 10)) || (!mCourse && (rotationAngle <= -10))) 
				{			
					setState(State.ASCEND);
				}
			}
			break;
		case ASCEND:
			// X coordinate movement simulation
			if (mCourse) 
				setX(getX() + left_speedX);
			else 
				setX(getX() - right_speedX);
		
			// Y coordinate movement simulation
			setY(getY() + speedY);
			
			// state change condition ASCEND -> ROTATE
			if (getY() >= C.GROUND + (C.FEETHEIGHT / 2)  + maxY)
				setState(State.ROTATE);
			
			break;
		case DELETE:
			break;
		}
		
		// state change condition -> DELETE
		if (mCourse) {
			if (getX() >= C.CAMERAWIDTH + 2 * C.FEETWIDTH)
				mState = State.DELETE;			
		} else {
			if (getX() <= - C.FEETWIDTH)
				mState = State.DELETE;
		}
	}
}
