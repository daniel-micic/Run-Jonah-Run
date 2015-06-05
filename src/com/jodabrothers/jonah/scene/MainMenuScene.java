/*
 * MainMenuScene
 * 
 * Menu scene providing game starter and theme and level pick
 * Consists of AndEngine MenuScene and MenuItems (Buttons) - handled by IOnMenuItemClickListener 
 */

package com.jodabrothers.jonah.scene;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;

import com.jodabrothers.jonah.base.BaseScene;
import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.SceneManager;
import com.jodabrothers.jonah.manager.ResourcesManager.LEVEL;
import com.jodabrothers.jonah.manager.SceneManager.SceneType;

public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener{

	// MenuScene holding all MenuItems, backgrounds 
	private MenuScene menuChildScene;
	private Sprite menu_background; // play screen
	private Sprite level_pick_background; // level pick screen
	private Sprite theme_pick_background; // theme pick screen
	
	// Menu Items	
	// Play screen
	private IMenuItem playMenuItem;
	// Level pick screen
    private IMenuItem easyMenuItem;
    private IMenuItem mediumMenuItem;
    private IMenuItem hardMenuItem;
    // Theme pick screen
	private IMenuItem darkMenuItem;
    private IMenuItem lightMenuItem;
    
    // MenuItem constants
	private final int MENU_PLAY = 0;
	private final int MENU_DARK = 1;
	private final int MENU_LIGHT = 2;
	private final int MENU_EASY = 3;
	private final int MENU_MEDIUM = 4;
	private final int MENU_HARD = 5;
    
    // Helping variables - for BackKey
    private boolean isThemePick;
    private boolean isLevelPick;
	
	// Scene creation
	@Override
	public void createScene() 
	{
		createBackground();
	    createMenuChildScene();
	    isThemePick = false;
	    isLevelPick = false;
	}
	
	// Create backgrounds for all screens 
	private void createBackground()
	{
		// Background SPrite creation
		menu_background = new Sprite(C.CAMERAWIDTH/2 ,C.CAMERAHEIGHT/2 , resourcesManager.menu_background_region, vbom);		
		level_pick_background = new Sprite(C.CAMERAWIDTH/2 ,C.CAMERAHEIGHT/2 , resourcesManager.level_pick_background_region, vbom);
		theme_pick_background = new Sprite(C.CAMERAWIDTH/2 ,C.CAMERAHEIGHT/2 , resourcesManager.theme_pick_background_region, vbom);
		
		// Setting alpha for fade in/out animations between screens
		menu_background.setAlpha(0f);
		level_pick_background.setAlpha(0f);
		theme_pick_background.setAlpha(0f);
		
		//attaching backgrounds 
	    attachChild(menu_background);
	    attachChild(theme_pick_background);
	    attachChild(level_pick_background);
	}
	
	// MenuScene and MenuItems creation
	private void createMenuChildScene()
	{
		// MenuScene
	    menuChildScene = new MenuScene(camera);
	    menuChildScene.setPosition(0, 0);
	    menuChildScene.setBackgroundEnabled(false);
	    
	    // Play screen button
	    playMenuItem = new SpriteMenuItem(MENU_PLAY, resourcesManager.button_region, vbom);
	    
	    playMenuItem.setPosition(C.CAMERAWIDTH/2, 319);
	    
	    menuChildScene.addMenuItem(playMenuItem);           
	    	    
	    // Theme pick screen buttons
	    darkMenuItem = new SpriteMenuItem(MENU_DARK, resourcesManager.button_region, vbom);
	    lightMenuItem = new SpriteMenuItem(MENU_LIGHT, resourcesManager.button_region, vbom);
	    
	    darkMenuItem.setPosition(212, 96);
	    lightMenuItem.setPosition(611 ,96);
	    
	    // Level pick screen buttons	    
	    easyMenuItem = new SpriteMenuItem(MENU_EASY, resourcesManager.button_region, vbom);
	    mediumMenuItem = new SpriteMenuItem(MENU_MEDIUM, resourcesManager.button_region, vbom);
	    hardMenuItem = new SpriteMenuItem(MENU_HARD, resourcesManager.button_region, vbom);
	    	   	   	    
	    easyMenuItem.setPosition(C.CAMERAWIDTH/2 ,334);
	    mediumMenuItem.setPosition(C.CAMERAWIDTH/2 ,272);
	    hardMenuItem.setPosition(C.CAMERAWIDTH/2 ,205);
	    
	    easyMenuItem.setScaleY(0.7f);
	    mediumMenuItem.setScaleY(0.7f);
	    hardMenuItem.setScaleY(0.7f);
	
	    // Textures of all MenuItems are transparent blank images - texts are part of background
	    playMenuItem.setVisible(false);
	    darkMenuItem.setVisible(false);
	    lightMenuItem.setVisible(false);
	    easyMenuItem.setVisible(false);
	    mediumMenuItem.setVisible(false);
	    hardMenuItem.setVisible(false);
	    
	    // Registering menu item listener
	    menuChildScene.setOnMenuItemClickListener(this);
	    
	    setChildScene(menuChildScene);
	}

	// Back key handling
	@Override
	public void onBackKeyPressed() 
	{
		// return to Play screen from theme/level pick screen
		if (isThemePick || isLevelPick) {
			// Fade out/in animation
			theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f) {
    	    	@Override
    	    	protected void onModifierFinished(IEntity pItem) {
    	    		menu_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
    	    		super.onModifierFinished(pItem);
    	    	}
    	    });   
			
			// deactivating/activating buttons
			menuChildScene.clearMenuItems();
			menuChildScene.addMenuItem(playMenuItem);
						
			isThemePick = false;
			isLevelPick = false;
		} else {
			// exiting application in Play screen
			System.exit(0);
		}
	}

	@Override
	public SceneType getSceneType() 
	{
		return SceneType.SCENE_MENU;
	}

	/*
	 * Menu click handling
	 * Play screen -> Level pick screen     -> Theme pick screen
	 * MENU_PLAY   -> MENU_EASY,MEDIUM,HARD -> MENU_LIGHT,DARK
	 */
	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
	{
		
	        switch(pMenuItem.getID())
	        {
	        case MENU_PLAY:	       
	        	// Play screen -> Level pick screen
	        	isLevelPick = true;
	        	
	        	// activating/deactivating menu items
	        	menuChildScene.clearMenuItems();
	        	menuChildScene.addMenuItem(easyMenuItem);
	    	    menuChildScene.addMenuItem(mediumMenuItem);
	    	    menuChildScene.addMenuItem(hardMenuItem);
	    	    
	    	    // screen change, Fade out/in animation 
	    	    menu_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f) {
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		level_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });   	        	
	            return true;
	        case MENU_EASY:
	        	// Easy difficulty picked
	        	resourcesManager.setLevel(LEVEL.EASY);
	        	
	        	// Level pick screen -> Theme pick screen
	        	isLevelPick = false;
	        	isThemePick = true;
	        	
	        	// activating/deactivating menu items
	        	menuChildScene.clearMenuItems();
	        	menuChildScene.addMenuItem(darkMenuItem);
	    	    menuChildScene.addMenuItem(lightMenuItem);
	    	    
	    	    // screen change, Fade out/in animation 
	    	    level_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f) {
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });  
	    	    return true;
	        case MENU_MEDIUM:
	        	// Normal(Medium) difficulty picked
	        	resourcesManager.setLevel(LEVEL.MEDIUM);
	        	
	        	// Level pick screen -> Theme pick screen
	        	isLevelPick = false;
	        	isThemePick = true;

	        	// activating/deactivating menu items
	        	menuChildScene.clearMenuItems();
	        	menuChildScene.addMenuItem(darkMenuItem);
	    	    menuChildScene.addMenuItem(lightMenuItem);
	    	    
	    	    // screen change, Fade out/in animation
	    	    level_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f) {
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });  
	    	    return true;
	        case MENU_HARD:
	        	// Poor Snail(Hard) difficulty picked
	        	resourcesManager.setLevel(LEVEL.HARD);
	        	
	        	// Level pick screen -> Theme pick screen	        	
	        	isLevelPick = false;
	        	isThemePick = true;	    
	        	
	        	// activating/deactivating menu items	        	
	        	menuChildScene.clearMenuItems();
	        	menuChildScene.addMenuItem(darkMenuItem);
	    	    menuChildScene.addMenuItem(lightMenuItem);
	    	    
	    	    // screen change, Fade out/in animation
	    	    level_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f) {
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });  
	        	return true;	        	
	        case MENU_DARK:
	        	// Dark theme picked
	        	resourcesManager.setTheme(true);
	        	
	        	// MenuScene (Theme pick screen) -> GameScene
	        	isThemePick = false;	          	
	        	
	        	// Scene change, Fade out/in animation
	        	theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f){
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		SceneManager.getInstance().createGameScene();
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });   
	        	return true;
	        case MENU_LIGHT:
	        	// Light theme picked
	        	resourcesManager.setTheme(false);
	        	
	        	// MenuScene (Theme pick screen) -> GameScene
	        	isThemePick = false;
	        	
	        	// Scene change, Fade out/in animation	        	
	        	theme_pick_background.registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 1f, 0f){
	    	    	@Override
	    	    	protected void onModifierFinished(IEntity pItem) {
	    	    		SceneManager.getInstance().createGameScene();
	    	    		super.onModifierFinished(pItem);
	    	    	}
	    	    });    	    		
	        	return true;
	        default:	        	
	            return false;
	    }
	}

	public Sprite getMenu_background() {
		return menu_background;
	}
	
	@Override
	public void disposeScene() {
		detachSelf();
        dispose();
	}
	
	

}
