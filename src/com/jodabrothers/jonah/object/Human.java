/*
 * Human
 * 
 * Simulates human - holds two paired feet objects
 * Human object constructor decides starting position and type of feet
 * Initial states of feet
 * 		Both feet have same random generated X coordinate
 * 		Course is random - generated by GameScene
 * 		first - FLOORED, rotation angle = 0, Y coordinate = ground + (feet height / 2) 
 * 		second - ROTATE, rotation angle = 0, Y coordinate = ground + (feet height / 2) + maximum Y coordinate of step  		
 */

package com.jodabrothers.jonah.object;

import java.util.Random;

import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;
import com.jodabrothers.jonah.object.Feet.State;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;

public class Human {
	// Types of foot
	public enum Type {SNEAKER, HIGH_HEEL, BOOT};
	
	// feet of human this object represent
	private Feet first_feet;
	private Feet second_feet;
	
	//course and type of feet
	private boolean mCourse;
	private Type type;
	
	/*
	 * Constructor
	 * 
	 * randomize offset of X coordinate
	 * randomly chooses type of feet
	 * sets Y coordinates and states of both feet
	 * assigns texture region of both feet
	 * creates both feet
	 */
	public Human(boolean pCourse, float posX, boolean theme,  ResourcesManager resourcesManager, VertexBufferObjectManager vbom) {
		
		mCourse = pCourse; 
		float firstPosX;
		float firstPosY;
		float secondPosX;
		float secondPosY;
		Random r = new Random();
		int offset = r.nextInt(100);
		
		// X Coordinate 
		if (mCourse) { 
			// Feet coming from left
			// for keeping minimal distance between Human objects, X coordinate is corrected   
			if (posX - 450 < -(C.FEETWIDTH / 2)) { // minimal distance
				firstPosX = posX - 450 - offset;
				secondPosX = posX - 450 - offset;
			} else {
				firstPosX = - C.FEETWIDTH / 2 - offset;				
				secondPosX = - C.FEETWIDTH / 2 - offset;
			}			
		} else {
			// feet coming from right
			firstPosX = C.CAMERAWIDTH + C.FEETWIDTH / 2 + offset;
			secondPosX = C.CAMERAWIDTH + C.FEETWIDTH / 2 + offset;
		}		
		
		/*
		 * Each type of foot has different chance to be created
		 * 		SNEAKER - 7/12
		 * 		HIGH HEEL - 1/3
		 * 		BOOT - 1/12
		 * 
		 * Each type of foot has different step height - changes length of step
		 * 		SNEAKER - ground + 110
		 * 		HIGH HEEL - ground + 90
		 * 		BOOT - ground + 90
		 */
		
		int typeRandom = r.nextInt(60);
		if (typeRandom < 35) {
			type = Type.SNEAKER;
			firstPosY = C.GROUND + C.FEETHEIGHT / 2 + 110;
			secondPosY = C.GROUND + C.FEETHEIGHT / 2;	
		} else if (typeRandom < 55) {
			type = Type.HIGH_HEEL;
			firstPosY = C.GROUND + C.FEETHEIGHT / 2 + 90;
			secondPosY = C.GROUND + C.FEETHEIGHT / 2;	
		} else {		
			type = Type.BOOT;
			firstPosY = C.GROUND + C.FEETHEIGHT / 2 + 130;
			secondPosY = C.GROUND + C.FEETHEIGHT / 2;	
		}
		
		// assigning texture region according to type, direction and theme
		PixelPerfectTextureRegion pTextureRegion;
		switch (type) {
		case SNEAKER: // type - SNEAKER
		default:
			if (mCourse) { // direction - from left
				if (theme) // theme - dark
					pTextureRegion = resourcesManager.dark_left_sneaker_region; 
				else // theme - light
					pTextureRegion = resourcesManager.light_left_sneaker_region;
			} else { // direction - from right
				if (theme) // theme - dark
					pTextureRegion = resourcesManager.dark_right_sneaker_region;
				else // theme - light
					pTextureRegion = resourcesManager.light_right_sneaker_region;
				break;
			}
			break;
		case HIGH_HEEL: 
			if (mCourse) { 
				if (theme)
					pTextureRegion =  resourcesManager.dark_left_highheel_region;
				else
					pTextureRegion = resourcesManager.light_left_highheel_region;
				break;
			} else {
				if (theme)
					pTextureRegion =  resourcesManager.dark_right_highheel_region;
				else
					pTextureRegion = resourcesManager.light_right_highheel_region;
				break;
			}
		case BOOT:
			if (mCourse) { 
				if (theme)
					pTextureRegion = resourcesManager.dark_left_boot_region;
				else
					pTextureRegion = resourcesManager.light_left_boot_region;
				break;
			} else {
				if (theme)
					pTextureRegion = resourcesManager.dark_right_boot_region;
				else
					pTextureRegion = resourcesManager.light_right_boot_region;
				break;
			}
		}
		
		// creating feet
		first_feet = new Feet(firstPosX, firstPosY, type, State.ROTATE, mCourse, pTextureRegion, vbom);
		second_feet = new Feet(secondPosX, secondPosY, type, State.FLOORED, mCourse, pTextureRegion, vbom);
		
		// pairing feet
		first_feet.setPair_feet(second_feet);
		second_feet.setPair_feet(first_feet);		
	}
	
	
	// getters
	public Feet getFirst_feet() {
		return first_feet;
	}
	public Feet getSecond_feet() {
		return second_feet;
	}
	
	
}
