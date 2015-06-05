/*
 * GameScene
 * 
 * Main scene of application. Game simulator is realized and managed in this class
 * Initializes all game objects upon creation
 * OnManagedUpdate handles all important situations and objects of game
 * Handles user input 
 */

package com.jodabrothers.jonah.scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TickerText;
import org.andengine.entity.text.TickerText.TickerTextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.adt.align.HorizontalAlign;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.jodabrothers.jonah.GameActivity;
import com.jodabrothers.jonah.base.BaseScene;
import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.SceneManager;
import com.jodabrothers.jonah.manager.SceneManager.SceneType;
import com.jodabrothers.jonah.object.Achievement;
import com.jodabrothers.jonah.object.Bubble;
import com.jodabrothers.jonah.object.Feet;
import com.jodabrothers.jonah.object.Grass;
import com.jodabrothers.jonah.object.Human;
import com.jodabrothers.jonah.object.Leaf;
import com.jodabrothers.jonah.object.Player;
import com.jodabrothers.jonah.object.Feet.State;
import com.jodabrothers.jonah.object.Player.Move;
import com.jodabrothers.jonah.util.ParallaxLayer;
import com.jodabrothers.jonah.util.ParallaxLayer.ParallaxEntity;

public class GameScene extends BaseScene
{
	
	// basic game fields
	private boolean game_over = false; // helper field for Game Over situation
	private boolean theme; //false = dark, true = light
	private float level; // EASY, NORMAL, HARD
	
	// Movement buttons constants
	private enum Button {LEFT_BOOST, LEFT, RIGHT, RIGHT_BOOST, NONE};
		
	// Player instance
	private Player player;
	
	// Human objects array and helper fields
	private ArrayList<Human> human_array;
	private Human last_left_human;	
	private boolean last_course;  
	private int same_course_counter; 
	
	// Leaf objects array and helper fields - sprint bonus
	private ArrayList<Leaf> leaves_array;	
	private int leaves_counter;
	private boolean boost_bonus;
	
	// Bubble object and helper fields - shield bonus
	private Bubble bubble;	
	private Sprite shield_button;
	private Sprite shield_sprite;
	private boolean shield_bonus;
	private float last_shield_bonus;	
	private boolean shield;
	
	// background and fade in/out layer	
	private ParallaxLayer background;
	private Sprite alphaLayer;
	
	// Achievements
	private Achievement achievements;
	private boolean firstBase;
	private boolean secondBase;
	private boolean thirdBase;
	
	// HUD - movement buttons, bonus buttons
	private Sprite left_button;
	private Sprite right_button;
	private Sprite left_boost_button_active;
	private Sprite left_boost_button_inactive;
	private Sprite right_boost_button_active;
	private Sprite right_boost_button_inactive;
	
	// Informative HUD objects
	private ArrayList<Sprite> leaf_icon;
	
	private Text distance_sprite;	
	private float distance;
	private float last_distance;
		
	// helper fields for movement decision
	private boolean leftTouchDown;
	private boolean rightTouchDown;
	private boolean leftBoostTouchDown;
	private boolean rightBoostTouchDown;
	private Button lastButton = Button.NONE;
	
	// Rate Application window (shown after 7th game)
	private Sprite rate_screen_sprite;
	private Sprite ok_sprite;
	private Sprite no_sprite;
	
	// Scene initialization
    @Override
    public void createScene()
    {
    	// satting game over boolean
    	game_over = false;
    	
    	// theme set up
    	theme = resourcesManager.getTheme();
    	
    	// Setting distance between Human objects according to level
    	switch (resourcesManager.getLevel()) {
    	case EASY:
    		level = 0.6f;
    		break;
    	case MEDIUM:
    		level = 0.4f;
    		break;
    	case HARD:
    		level = 0.27f;
    		break;
    		
    	}
    	
    	// Game music start
    	if (!resourcesManager.isGameMusicPlaying()) {
    		resourcesManager.playGameMusic();
    	}
    	
    	// Game scene objects creation
        createBackground();
        createAchievement();
        createPlayer();
        createHUD();        
        createHuman();
        createLeaves();
        createBubble();
        createAlpha();
    }
    
    // Sprint bonus objects initialziation
    private void createLeaves() {
    	// Data structure holding leaf objects
    	leaves_array = new ArrayList<Leaf>();
    	
    	// Coutner for collected leaves
    	leaves_counter = 0;
    	
    	// Boost bonus boolean
    	boost_bonus = false;
    	
    	// First leaf creation
    	newLeaf();    	
    }
            
    // Leaf object creation
    private void newLeaf() {
    	
    	Leaf leaf = new Leaf(0, 0, resourcesManager.leaf_region, vbom);
    	Random r = new Random();
    	
    	//X coordinate is set with random offset
    	leaf.setX(C.CAMERAWIDTH + leaf.getWidth() + r.nextInt(15));
    	leaf.setY(C.GROUND + (leaf.getHeight() / 2));
    	
    	leaves_array.add(leaf);
    	
    	// attaching the object to the scene
    	attachChild(leaf);
    }
    
    // Shield bonus objects initialization
    private void createBubble() {
    	
    	// Bubbles are created in intervals
    	last_shield_bonus = distance;
    	
    	// Shield bonus boolean
    	shield_bonus = false;
    	
    	// First bubble creation
    	newBubble();
    }
    
    // Bubble creation
    private void newBubble() {
    	
    	bubble = new Bubble(0, 0, resourcesManager.bubble_region, vbom);
    	
    	// X Coordiante of bubble is computed depending on player position
    	Random r = new Random();
    	if (player.getX() < 120) { // bubble cannot be created to the left of player
    		bubble.setX(player.getX() + 65 + r.nextInt((int)(C.CAMERAWIDTH - player.getX() - 110)));
    	} else if (player.getX() > 680) { // bubble cannot be created to the right of player
    		bubble.setX(r.nextInt((int)(player.getX() - 110)) + 45);
    	} else { // computing the X coordinate to the left or right of player
    		if (r.nextBoolean())
    			bubble.setX(player.getX()  + 65 + r.nextInt((int)(C.CAMERAWIDTH - player.getX() - 110)));
    		else 
    			bubble.setX(r.nextInt((int)(player.getX() - 110)) + 45);
    	}
    	
    	// Setting the Y coordinates so the bubble sits on ground
    	bubble.setY(C.GROUND + 18); 
    	
    	//Scaling bubble sprite
    	bubble.setScale(0.1f);
    	
    	// attaching the object to the scene
    	attachChild(bubble);
    	
    	// Bubble creation animation
    	bubble.registerEntityModifier(new ScaleAtModifier(0.5f, 0.1f, 0.5f, 0.5f, 0.5f));
    }
    
    // Human objects initialization
    private void createHuman() 
    {
    	human_array = new ArrayList<Human>();
    	last_left_human = null;    	
    	same_course_counter = 0;
    	last_course = true;
    	newHuman();
    }

    // Human objects 
    private void newHuman() 
    {    	
    	Random r = new Random();
    	
    	// generating direction of Human object
    	boolean course = r.nextBoolean();  
    	
    	// checking and correcting the direction in case last three Human object came from same direction
    	if (course == last_course) {
    		same_course_counter++;
    		if (same_course_counter == 3) {
    			course = !course;
    			same_course_counter = 0;    		
    		}    			
    	}    		
    	else 
    		same_course_counter = 0;
    	
    	last_course = course;
    	
    	// Human object creation
    	Human human;
 
    	// X coordinate corrections to maintain distance between Human objects coming from left 
		float posX;
		if ((!course) || (last_left_human == null)) {
			posX = 0;    			
		} else { // getting the most left X coordinate of last Human object that came from left
			if (last_left_human.getSecond_feet().getX() < last_left_human.getFirst_feet().getX()) {  
				posX = last_left_human.getSecond_feet().getX();
			}
			else {
				posX = last_left_human.getFirst_feet().getX();
			}
		}
    	human = new Human(course, posX, theme, resourcesManager, vbom);
    	
    	// setting last Human object that came from left
    	if (course) {
    		last_left_human = human;
    	}
    	
    	
    	human_array.add(human);
    	
    	// attaching the objects to the scene
    	attachChild(human.getFirst_feet());
    	attachChild(human.getSecond_feet());
    }
    
    // Background creation
    private void createBackground()
	{
    	
    	// for continuously moving Background I used external class Parallax Layer
    	Sprite background_sprite = new Sprite(C.BACKGROUNDWIDTH/2, C.CAMERAHEIGHT/2, theme ? resourcesManager.dark_background_region : resourcesManager.light_background_region, vbom);
    	this.background = new ParallaxLayer(camera, true, 1024);   
    	this.background.setParallaxChangePerSecond(-0.3f);
    	this.background.setParallaxScrollFactor(1);
    	this.background.attachParallaxEntity(new ParallaxEntity(15, background_sprite, false, 1));
    	
    	// creating grass object - movement referencing point
    	Grass grass = new Grass(theme, vbom);
    	
    	// attaching the objects to the scene
    	this.attachChild(background);
    	this.attachChild(grass.getGrass1());
    	this.attachChild(grass.getGrass2());
    }
    
    // Achievemnt creation
    public void createAchievement() {
    	// Creating achievements objects and attaching them to scene
    	this.achievements = new Achievement(resourcesManager, vbom);
    	for (TiledSprite sprite : achievements.getSprites()) {
    		attachChild(sprite);
    	}
    	
    	// setting of helper fields
    	this.firstBase = false;
    	this.secondBase = false;
    	this.thirdBase = false;
    	
    }
    
    // Player sprite creation
    private void createPlayer() 
    {
    	this.player = new Player(resourcesManager.animated_player_region, vbom);
    	attachChild(this.player);
    }
    
    // Creation of HUD
    // Input handling is declared upon creation of buttons 
    private void createHUD() 
    {
    	// for tracking of touch movement (binding Touch Down with corresponding Touch Up)
    	this.setTouchAreaBindingOnActionDownEnabled(true);
    	this.setTouchAreaBindingOnActionMoveEnabled(true);
    	
    	leftTouchDown = false;
    	rightTouchDown = false;
    	leftBoostTouchDown = false;
    	rightBoostTouchDown = false;
    	
    	// initialization of fields holding and showing traveled distance 
    	distance = 0.0f;
    	last_distance = 0.0f; // for keeping intervals between bubble creation
    	this.distance_sprite = new Text(C.CAMERAWIDTH/2, 140, theme ? resourcesManager.dark_screen_font : resourcesManager.light_screen_font, String.format("%.2f m", distance), vbom);
    	this.distance_sprite.setScale(0.7f);
    	
    	// Handling movement button to the left
    	this.left_button = new Sprite(50, 70, theme ? resourcesManager.dark_left_button_region : resourcesManager.light_left_button_region, vbom)
    	{
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown())
    	        {  	
    	        	lastButton = Button.LEFT;
    	        	player.setBoost(false);
    	        	leftTouchDown = true;    	        	
    	        }    
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {
    	        	if ((!rightTouchDown) && (!rightBoostTouchDown) && (!leftBoostTouchDown))
    	        		lastButton = Button.NONE;
    	        	leftTouchDown = false;
    	        }   	        
    	        return true;
    	    };
    	};
    	
    	// Handling movement button to the right
    	this.right_button = new Sprite(750, 70, theme ? resourcesManager.dark_right_button_region : resourcesManager.light_right_button_region, vbom)
    	{
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown())
    	        {  	        		
        	       	lastButton = Button.RIGHT;
        	       	player.setBoost(false);
        	       	rightTouchDown = true;
        	       	
    	        }
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {
    	        	if ((!leftTouchDown) && (!rightBoostTouchDown) && (!leftBoostTouchDown))
    	        		lastButton = Button.NONE;
    	        	rightTouchDown = false;
    	        }  
    	        return true;
    	    };
    	};
    	
    	// Handling sprint bonus button (to the left)
    	this.left_boost_button_active = new Sprite(150, 70, theme ? resourcesManager.dark_left_bonus_active_region : resourcesManager.light_left_bonus_active_region, vbom) 
    	{
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown())
    	        {  	
    	        	if (boost_bonus) {    	        		
	    	        	player.setBoost(true);
	    	        	lastButton = Button.LEFT_BOOST;
	    	        	leftBoostTouchDown = true;	
	    	        	leaves_counter = 0;
	    	        	for(Sprite leaf: leaf_icon) {
	    	        		leaf.registerEntityModifier(new ScaleAtModifier(0.2f, 0.7f, 0.0f, 0.5f, 0.5f));
	    	        	}
    	        	}
    	        }    
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {
    	        	if ((!rightTouchDown) && (!leftTouchDown) && (!rightBoostTouchDown))
    	        		lastButton = Button.NONE;
    	        	//leaves_counter = 0;
    	        	player.setBoost(false);
    	        	leftBoostTouchDown = false;	    	        		    	        	
    	        }   	            	    	
    	        return true;
    	    };
    	};    	
    	// active/inactive bonus buttons
    	this.left_boost_button_active.setVisible(false);
    	
    	this.left_boost_button_inactive = new Sprite(150, 70, theme ? resourcesManager.dark_left_bonus_inactive_region : resourcesManager.light_left_bonus_inactive_region, vbom);
    	this.left_boost_button_inactive.setVisible(true);

    	// Handling sprint bonus button (to the right)
    	this.right_boost_button_active = new Sprite(650, 70, theme ? resourcesManager.dark_right_bonus_active_region : resourcesManager.light_right_bonus_active_region, vbom) 
    	{
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {    	    	
    	        if (pSceneTouchEvent.isActionDown())
    	        {  	
    	        	if (boost_bonus) {
	    	        	player.setBoost(true);
	    	        	lastButton = Button.RIGHT_BOOST;
	        	       	rightBoostTouchDown = true;	        
	        	       	leaves_counter = 0;
	    	        	for(Sprite leaf: leaf_icon) {
	    	        		leaf.registerEntityModifier(new ScaleAtModifier(0.2f, 0.7f, 0.0f, 0.5f, 0.5f));
	    	        	}
    	        	}
    	        }    
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {
    	        	if ((!rightTouchDown) && (!leftTouchDown) && (!leftBoostTouchDown))
    	        		lastButton = Button.NONE;
    	        	//leaves_counter = 0;
    	        	player.setBoost(false);    
    	        	rightBoostTouchDown = false;  
    	        }   	          	    	
    	        return true;
    	    };
    	};
    	// active/inactive bonus buttons
    	this.right_boost_button_active.setVisible(false);
    	
    	this.right_boost_button_inactive = new Sprite(650, 70, theme ? resourcesManager.dark_right_bonus_inactive_region : resourcesManager.light_right_bonus_inactive_region, vbom);
    	this.right_boost_button_inactive.setVisible(true);
    	
    	// Handling shield bonus button    	
    	this.shield_button = new Sprite(C.CAMERAWIDTH/2, 80, resourcesManager.bubble_region, vbom) {
    		@Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	    	if (shield_bonus) {
	    	        if (pSceneTouchEvent.isActionDown()) 
	    	        {
	    	        	//shield_sprite.setVisible(true);
	    	        	shield = true;
	    	        	shield_sprite.setVisible(true);
	    	        	//shield_sprite.registerEntityModifier(new AlphaModifier(0.2f, 0.0f, 1.0f));
	    	        	shield_bonus = false;
	    	        	last_shield_bonus = distance;
	    	        	shield_button.setVisible(false);
	    	        	shield_button.setScale(0.0f);
	    	        }   	      
    	    	}
    	        return true;
    	    };
    	};
    	this.shield_button.setScale(0.0f);
    	this.shield_button.setVisible(false);
    	
    	// shield sprite (bubble around player sprite)
    	this.shield_sprite = new Sprite(player.getX(), player.getY(), resourcesManager.bubble_region, vbom) {
    		protected void onManagedUpdate(float pSecondsElapsed) {
    			setX(player.getX());
    		}
    	};
    	this.shield_sprite.setVisible(false);  
    	
    	// leaf counter sprite
    	leaf_icon = new ArrayList<Sprite>();
    	
    	for (int i = 0; i < 3; i++) {
    		Sprite leaf = new Sprite(C.CAMERAWIDTH/2 + (i-1)*30, 25, resourcesManager.leaf_region, vbom);
    		leaf.setScale(0.0f);
    		attachChild(leaf);
    		this.leaf_icon.add(leaf);
    	}
    	    	
    	// registering buttons touch areas and attaching HUD objects to the scene
    	this.registerTouchArea(left_button);
    	this.attachChild(left_button);
    	this.registerTouchArea(right_button);
    	this.attachChild(right_button);
    	
    	this.registerTouchArea(left_boost_button_active);
    	this.attachChild(left_boost_button_active);
    	this.registerTouchArea(right_boost_button_active);
    	this.attachChild(right_boost_button_active);
    	this.attachChild(left_boost_button_inactive);
    	this.attachChild(right_boost_button_inactive);
    	    	
    	this.attachChild(distance_sprite);
    	
    	this.attachChild(shield_button);
    	this.registerTouchArea(shield_button);
    	this.attachChild(shield_sprite);   	
    }
    
    // setting Boost Bonus active and inactive
    public void setBoostInactive() {
    	boost_bonus = false;
    	right_boost_button_active.setVisible(false);    	        	
    	right_boost_button_inactive.setVisible(true);
    	left_boost_button_active.setVisible(false);    	        	
    	left_boost_button_inactive.setVisible(true);	
    }
    
    public void setBoostActive() {
   		boost_bonus = true;
    	right_boost_button_active.setVisible(true);    	        	
    	right_boost_button_inactive.setVisible(false);
    	left_boost_button_active.setVisible(true);    	        	
    	left_boost_button_inactive.setVisible(false);
    }
       
    // Layer handling fade in/out animations
    private void createAlpha() {   	
    	alphaLayer = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, resourcesManager.black_background_region, vbom);
    	this.attachChild(alphaLayer);	
    	alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f));
    } 
    
    // reattaching Alpha layer (to cover all objects)
    private void reattachAlpha() {
    	detachChild(alphaLayer);
    	attachChild(alphaLayer);
    }
    
    // Main simulator method - handles all important situations
    protected void onManagedUpdate(final float pSecondsElapsed) 
    {    	    	
    	// checking game over situation on every enigne update
    	if (!game_over) {
    		    		
    		// Environmental movement
    		distance += 0.001f;     
    		
    		//levels of difficulty - game gets harder on 5.0f milestones
    		switch (resourcesManager.getLevel()) {
    		case EASY:
    			if (distance == 5f)       			
        			level = 0.5f;
    			else if (distance == 10f)
        			level = 0.4f;
    			else if (distance == 15f)
        			level = 0.5f;
    			else if (distance == 20f)
        			level = 0.4f;       		        		
    			break;
    		case MEDIUM:
    			if (distance == 5f)       			
        			level = 0.33f;
    			else if (distance == 10f)
        			level = 0.27f;
    			else if (distance == 15f)
        			level = 0.40f;
    			else if (distance == 20f)
        			level = 0.33f;       		        		
    			break;
    		default:
    			break;
    		}
    		
    		//achievements, shown every 5.0f distance
    		if ((player.getDistance() >= 5f) && !firstBase) {
    			AnimatedSprite plane = achievements.getSprites().get(0);
    			plane.registerEntityModifier(new MoveModifier(15f, plane.getX(), 400, -plane.getX(), 400));    	
    			plane.animate(75);
    			firstBase = true;
    		}
    		if ((player.getDistance() >= 10f) && !secondBase) {
    			AnimatedSprite balloon = achievements.getSprites().get(1);
    			balloon.registerEntityModifier(new MoveModifier(15f, balloon.getX(), 400, -balloon.getX(), 400));
    			balloon.animate(75);
    			secondBase = true;    			    			
    		}
    		if ((player.getDistance() >= 15f) && !thirdBase) {
    			AnimatedSprite ufo = achievements.getSprites().get(2);
    			ufo.registerEntityModifier(new MoveModifier(15f, ufo.getX(), 400, -ufo.getX(), 400));
    			ufo.animate(75);
    			thirdBase = true;
    		}
    		    		
    		// moving player according to the touched buttons    	
    		switch (lastButton) {
    		case LEFT:
    		case LEFT_BOOST:
    			player.setMove(Move.LEFT);
    			break;
    		case RIGHT:
    		case RIGHT_BOOST:
    			player.setMove(Move.RIGHT);
    			break;
    		case NONE:
    		default:
    			player.setMove(Move.STOP);
    		}
	    	
    		// creating new Human object
	    	if (((distance - last_distance) >= level) && (!game_over)) {
	    		newHuman();
	    		last_distance = distance;
	    	}
	    	
	    	// shield bonus objects handling
	    	if (bubble != null) {	
	    		// collision with player - activating bonus
		    	if (bubble.collidesWith(player) && bubble.isActive()){
			    	bubble.setInactive();
			    	resourcesManager.playBubble(); // bubble collection sound
			    	shield_button.setVisible(true);
			    	shield_button.registerEntityModifier(new ScaleAtModifier(0.2f, shield_button.getScaleX(), 0.75f, 0.5f, 0.5f));
			    	shield_bonus = true;
		    	} 
		    	
		    	// removing uncollected bubble object
	    		if (bubble.getX() <= -100) {
	    			detachChild(bubble);
	    			bubble = null;
	    		}	    			    	
	    	} else {
	    		// creating new Bubble object
	    		if (!shield_bonus && ((distance - last_shield_bonus) >= 0.5f)) {	    			
	    			newBubble(); 
	    			last_shield_bonus = distance;
	    		}
	    	}
	    	
	    	// Removing shield sprite
	    	if (shield && (distance - last_shield_bonus > 0.2f)) {	    			    	
	    		shield_sprite.setVisible(false);
	    		shield = false;
	    	}
	 	    	
	    	// Sprint bonus objects handling
	    	if (leaves_array.get(leaves_array.size() - 1).getX() <= 3*C.CAMERAWIDTH/4) {	    		
	    		newLeaf();	    		
	    	}
	    	
	    	// Collision with Player or Feet object, removing uncollected leaves 
	    	Iterator<Leaf> leaves_iterator = leaves_array.iterator();
	    	while (leaves_iterator.hasNext()) 
	    	{	    		
	    		Leaf leaf = leaves_iterator.next();
	    		
	    		// collision with Player sprite increments leaves counter
	    		if (leaf.collidesWith(player) && leaf.isActive()) {
	    			if (leaves_counter < 3) {
	    				leaves_counter++;
	    				leaf_icon.get(leaves_counter - 1).registerEntityModifier(new ScaleAtModifier(0.2f, 0.0f, 0.7f ,0.5f, 0.5f));
	    			}
	    			resourcesManager.playLeaf(); // leaf collection sound
	    			leaf.setInactive();	    				    		
	    		}
	    		
	    		// collision with Human (Feet) object sets leaf inactive (not shown, uncollectable)
	    		for (Human human: human_array) {
	    			Feet first_feet = human.getFirst_feet();
		    		Feet second_feet = human.getSecond_feet();
	    			if ((first_feet.collidesWith(leaf) && ((first_feet.getState() == State.DESCEND) || (first_feet.getState() == State.DESCEND_FLOORED))) || 
	    				(second_feet.collidesWith(leaf) && ((second_feet.getState() == State.DESCEND) || (second_feet.getState() == State.DESCEND_FLOORED)))) {	    				
	    				leaf.setInactive();
	    			}
	    		}
	    		
	    		// removing uncollected leaf
	    		if (leaf.getX() <= - leaf.getWidth()/2) {
	    			leaves_iterator.remove();
	    			detachChild(leaf);
	    		}	    		
	    	} 
	    	
	    	// activating/deactivating boost bonus
	    	if (leaves_counter == 3)
	    		setBoostActive();
	    	else 
	    		setBoostInactive();
	    	
	    	// checking contact with feet, evaluating game_over or corrects player movement
	    	for (Human human: human_array) {
	    		Feet firstFeet = human.getFirst_feet();
	    		Feet secondFeet = human.getSecond_feet();
	    		
	    		if (player.collidesWith(firstFeet)) {
	    			leftTouchDown = false;
	    			rightTouchDown = false;
	    			leftBoostTouchDown = false;
	    			rightBoostTouchDown = false;
	    			
	    			
	    			switch (firstFeet.getState()) {
	    			case DESCEND: // Feet coming down - Game Over situation    
	    				if (!shield)
	    					gameOver();
	    				break;
	    			case DESCEND_FLOORED: 
	    				// IF the player sprite is next to feet, it cannot go behind it
	    				// If the player sprite is under feet object - Game Over situation
	    				if (firstFeet.getCourse()) {
	    					if (player.getX() <= firstFeet.getX())
	    						player.setMove(Move.LEFT);
	    					else
	    						if (!shield)
	    	    					gameOver();
	    				} else {
	    					if (player.getX() >= firstFeet.getX())
	    						player.setMove(Move.STOP);
	    					else
	    						if (!shield)
	    							gameOver();
	    				}
	    				break;
	    			case FLOORED: // Player sprite X coordinate corrections (can't go behind feet)
	    				if (player.getX() <= firstFeet.getX())
	    					player.setMove(Move.LEFT);
	    				else
	    					player.setMove(Move.STOP);
	    			}
	    		}
	    		
	    		// Analogical to the First feet
	    		if (player.collidesWith(secondFeet)) {
	    			switch (secondFeet.getState()) {
	    			case DESCEND:  
	    				if (!shield)
	    					gameOver();
	    				break;
	    			case DESCEND_FLOORED:
	    				if (firstFeet.getCourse()) {
	    					if (player.getX() <= firstFeet.getX())
	    						player.setMove(Move.LEFT);
	    					else
	    						if (!shield)
	    	    					gameOver();
	    				} else {
	    					if (player.getX() >= firstFeet.getX())
	    						player.setMove(Move.STOP);
	    					else
	    						if (!shield)
	    							gameOver();
	    				}
	    				break;
	    			case FLOORED:
	    				if (player.getX() <= secondFeet.getX())
	    					player.setMove(Move.LEFT);
	    				else
	    					player.setMove(Move.STOP);
	    			}
	    		}
	    	}	    		    
	    	
	    	// removing Human Object that optically left the Scene
	    	Iterator<Human> human_iterator = human_array.iterator();
	    	while (human_iterator.hasNext()) 
	    	{
	    		Human human = human_iterator.next();   	
	    		if (human.getSecond_feet().getState() == State.DELETE) {
	    			human_iterator.remove();
	    		}
	    	} 
	    	    	
	    	// updating score text sprite
	    	distance_sprite.setText(String.format("%.2f m", player.getDistance()));
    	}
    	super.onManagedUpdate(pSecondsElapsed);
    	
    }
    
    // Hnadling Game Over situation
    private void gameOver() 
    {    	
    	this.game_over = true;
    	
    	// Updating High Score
    	float player_dist = player.getDistance();
    	
    	float record = resourcesManager.loadHighScore();
    	if (record < player_dist) {
    		resourcesManager.setHighScore(player_dist);
    		record = player_dist;
    	}

    	// incrementing Game Count
    	resourcesManager.gameCountIncrement();
    	
    	// Share string composition
    	resourcesManager.setShareString(player_dist);
    	
    	// playing Game Over sound
    	resourcesManager.playGameOver();
    	
    	// creating Game Over screen objects    	
    	final Sprite game_over_sprite = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, theme ? resourcesManager.dark_game_over_region : resourcesManager.light_game_over_region, vbom);
    	final Sprite game_over_text_sprite1 =  new Sprite(C.CAMERAWIDTH/2, 3*C.CAMERAHEIGHT/4, theme ? resourcesManager.dark_game_over_text1_region : resourcesManager.light_game_over_text1_region, vbom);
    	final Sprite game_over_text_sprite2 =  new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, theme ? resourcesManager.dark_game_over_text2_region : resourcesManager.light_game_over_text2_region, vbom);
    	
    	// Button for return to menu
    	final Sprite menu_button_sprite = new Sprite(240, 80, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	    	
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {
    	        	resourcesManager.stopGameMusic();
    	        	// showing/not showing ads
    	        	if (resourcesManager.getGameCount() % 3 == 0) {
    	        		reattachAlpha();
    	        		alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR ,0f , 1f) {
	    	        		@Override
	    	        		protected void onModifierFinished(IEntity pItem) {
	    	        			((GameActivity)activity).showAd(SceneType.SCENE_MENU);
	    	        		}		    	        		
    	        		});
    	        	} else {
	    	        	reattachAlpha();
	    	        	alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR ,0f , 1f) {
	    	        		@Override
	    	        		protected void onModifierFinished(IEntity pItem) {
	    	        			engine.getCamera().setHUD(null);	    	        			
	    	    	        	SceneManager.getInstance().createMenuScene();
	    	        			super.onModifierFinished(pItem);
	    	        		}
	    	        	});
    	        	}
    	        }    	        	
    	        return true;
    	    };
    	};
    	
    	// Play Again button
    	final Sprite again_button_sprite = new Sprite(517, 80, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionUp()) 
    	        {    	    
    	        	// showing/not showing ads
    	        	if (resourcesManager.getGameCount() % 3 == 0) {
    	        		reattachAlpha();
    	        		alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR ,0f , 1f) {
	    	        		@Override
	    	        		protected void onModifierFinished(IEntity pItem) {
	    	        			((GameActivity)activity).showAd(SceneType.SCENE_GAME);
	    	        			System.out.println("kurva");
	    	        			//resourcesManager.setGameCount(0);
	    	        		}		    	        		
    	        		});
    	        	} else {    	        		
	    	        	reattachAlpha();
	    	        	alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR ,0f , 1f) {
	    	        		@Override
	    	        		protected void onModifierFinished(IEntity pItem) {
	    	        			engine.getCamera().setHUD(null);
	    	    	        	SceneManager.getInstance().createGameScene();
	    	        			super.onModifierFinished(pItem);
	    	        		}
	    	        	});
    	        	}
    	        }  
    	        return true;
    	    };
    	};
    	
    	// Button redirecting to the Ad Free version on Google Play (in Google Store app or web)
    	final Sprite ad_free_sprite = new Sprite(355, 30, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown()) 
    	        {
    	        	Uri uri = Uri.parse("market://details?id=com.jodabrothers.jonah.adfree");
    	        	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
    	        	try {
    	        	  activity.startActivity(goToMarket);
    	        	} catch (ActivityNotFoundException e) {
    	        	  activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jodabrothers.jonah.adfree")));
    	        	}    	                    	  	        
    	        }  
    	        return true;
    	    };
    	};
    	ad_free_sprite.setScaleY(0.4f);  
    	
    	// SHaring buttons
    	final Sprite facebook_sprite = new Sprite(600, 285, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown()) 
    	        {
    	        	resourcesManager.shareManager.shareFacebook();  	                    	  	        
    	        }  
    	        return true;
    	    };
    	}; 
    	
    	final Sprite twitter_sprite = new Sprite(600, 335, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown()) 
    	        {
    	        	resourcesManager.shareManager.shareTwitter();  	                    	  	        
    	        }  
    	        return true;
    	    };
    	};    	
    	  
    	// Actual and High score texts
    	TickerTextOptions top_time_text_options = new TickerTextOptions(HorizontalAlign.CENTER, 10);
    	TickerTextOptions your_time_text_options = new TickerTextOptions(HorizontalAlign.CENTER, 10);
    	final Text top_time = new TickerText(0, 371, resourcesManager.light_screen_font, String.format("%.2f m", record), top_time_text_options, vbom);
    	final Text your_time = new TickerText(0, 417, resourcesManager.light_screen_font, String.format("%.2f m", player_dist), your_time_text_options, vbom);
    	top_time.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    	your_time.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    	
    	top_time.setX(426 + top_time.getWidth()/2);
    	your_time.setX(426 + your_time.getWidth()/2);
    	
    	top_time.setScaleCenterX(0f);
    	your_time.setScaleCenterX(0f);
    	
    	top_time.setScale(0.69f);
    	your_time.setScale(0.87f);
    	
    	// initial state of Game Over animation
    	game_over_sprite.setAlpha(0f);
    	game_over_text_sprite1.setAlpha(0f);    	
    	game_over_text_sprite2.setAlpha(0f);
    	top_time.setAlpha(0f);
    	your_time.setAlpha(0f);
    	
    	reattachAlpha();
    	
    	// attaching game over objects to the scene
    	attachChild(game_over_sprite);
    	attachChild(game_over_text_sprite1);
    	attachChild(game_over_text_sprite2);
    	attachChild(top_time);
    	attachChild(your_time);
    	
    	// Game Over animation - text Game Over -> fade out/in animation -> score and buttons texts
    	alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f) {
    		@Override
    		protected void onModifierFinished(IEntity pItem) {    	
    			game_over_text_sprite1.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
		    	game_over_sprite.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f) {
		    		protected void onModifierFinished(IEntity pItem) {  
		    			alphaLayer.setAlpha(0f);		    			
    					engine.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback()	    
    				    {
    				            public void onTimePassed(final TimerHandler pTimerHandler) 
    				            {
    				            	game_over_text_sprite1.registerEntityModifier(new AlphaModifier(0.5f, 1f, 0f) {
    				    				@Override
    				    				protected void onModifierFinished(IEntity pItem) {
		    				            	game_over_text_sprite2.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f));		    				            	
		    				            	top_time.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f));
		    				            	your_time.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f));
		    		    					registerTouchArea(menu_button_sprite);
		    		    			    	registerTouchArea(again_button_sprite);
		    		    			    	registerTouchArea(ad_free_sprite);     
		    		    			    	registerTouchArea(facebook_sprite);
		    		    			    	//attachChild(facebook_sprite);
		    		    			    	registerTouchArea(twitter_sprite);
		    		    			    	//attachChild(twitter_sprite);
		    		    			    	if (resourcesManager.getGameCount() == 8)
		    		    			    		createRateScreen();
		    		    			    	super.onModifierFinished(pItem);
    				    				}
    				            	});
    				            }
    				    }));
		    		}
		    	});
    		}
    	});

    	// stopping game objects
    	for(Human human: human_array) {
    		human.getFirst_feet().setState(State.DELETE);
    		human.getSecond_feet().setState(State.DELETE);    			
    	}
    	
    	player.setMove(Move.STOP);
    	player.stopAnimation();
    	
    	background.setParallaxChangePerSecond(0);
    	
    	this.unregisterTouchArea(left_button);
    	this.unregisterTouchArea(right_button);    	
    }
    
    // creating Rate Window
    public void createRateScreen() {
    	rate_screen_sprite = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, resourcesManager.dark_rate_screen_region, vbom);    	
    	rate_screen_sprite.setScale(0f);
    	attachChild(rate_screen_sprite);
    	rate_screen_sprite.registerEntityModifier(new ScaleModifier(0.2f, 0.0f, 1.0f));
    	
    	// redirecting to Google Play page of application
    	ok_sprite = new Sprite(285, 206, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown()) 
    	        {
    	        	hideRateScreen();
    	        	Uri uri = Uri.parse("market://details?id=com.jodabrothers.jonah");
    	        	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
    	        	try {
    	        	  activity.startActivity(goToMarket);
    	        	} catch (ActivityNotFoundException e) {
    	        	  activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jodabrothers.jonah")));
    	        	}    	                    	  	        
    	        }  
    	        return true;
    	    };
    	};
    	attachChild(ok_sprite);
    	registerTouchArea(ok_sprite);
    	ok_sprite.setVisible(false);
    	ok_sprite.setWidth(43);
    	
    	// return to game
    	no_sprite = new Sprite(490, 206, resourcesManager.button_region, vbom) {
    	    @Override
    	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	    {
    	        if (pSceneTouchEvent.isActionDown()) 
    	        {
    	        	hideRateScreen();   	                    	  	        
    	        }  
    	        return true;
    	    };
    	};
    	attachChild(no_sprite);
    	registerTouchArea(no_sprite);
    	no_sprite.setVisible(false);
    	ok_sprite.setWidth(84);   	   	
    }
    
    public void hideRateScreen() {
    	rate_screen_sprite.registerEntityModifier(new ScaleModifier(0.2f, 1.0f, 0.0f));   
    	detachChild(no_sprite);
    	detachChild(ok_sprite);
    	unregisterTouchArea(ok_sprite);
    	unregisterTouchArea(no_sprite);
    }
    
    // return to menu
    @Override
    public void onBackKeyPressed()
    {
    	resourcesManager.stopGameMusic();
    	reattachAlpha();
    	alphaLayer.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f) {
    		@Override
    		protected void onModifierFinished(IEntity pItem) {
    			engine.getCamera().setHUD(null);
    	    	SceneManager.getInstance().createMenuScene();
    			super.onModifierFinished(pItem);
    		}
    	});    	
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        detachSelf();
        dispose();
    }
}