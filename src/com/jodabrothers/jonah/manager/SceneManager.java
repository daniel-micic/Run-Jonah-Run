/*
 * Scene Manager
 * 
 * Handles creation of scene and communication between them
 * Contains private fields for each logic scene - Splask(Loading), Menu, Game and Tutorial  
 */

package com.jodabrothers.jonah.manager;

import org.andengine.engine.Engine;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.scene.Scene;

import com.jodabrothers.jonah.base.BaseScene;
import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.scene.GameScene;
import com.jodabrothers.jonah.scene.MainMenuScene;
import com.jodabrothers.jonah.scene.SplashScene;


public class SceneManager
{
    //---------------------------------------------
    // SCENES
    //---------------------------------------------
        
    private BaseScene splashScene;
    private BaseScene menuScene;
    private BaseScene gameScene;
    
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------
    
    // creates static instance of SceneManager 
    private static final SceneManager INSTANCE = new SceneManager();
    
    private SceneType currentSceneType = SceneType.SCENE_SPLASH;
    private BaseScene currentScene;  
    private Engine engine = ResourcesManager.getInstance().engine;
    
    public enum SceneType
    {
        SCENE_SPLASH,
        SCENE_MENU,
        SCENE_GAME,
    }
    
    //---------------------------------------------
    // CLASS LOGIC
    //---------------------------------------------
    
    public void setScene(BaseScene scene)
    {
        engine.setScene(scene);
        currentScene = scene;
        currentSceneType = scene.getSceneType();
    }
    
    public void setScene(SceneType sceneType)
    {
        switch (sceneType)
        {
            case SCENE_MENU:
                setScene(menuScene);
                break;
            case SCENE_GAME:
                setScene(gameScene);
                break;
            case SCENE_SPLASH:
                setScene(splashScene);
                break;
            default:
                break;
        }
    }
    
    public Scene createSplashScene()
    {
    	splashScene = new SplashScene();
    	currentScene = splashScene;
    	setScene(splashScene);
    	return splashScene;
    }
       
    public void createMenuScene()
    {
    	ResourcesManager.getInstance().reloadMusic();
    	if (getCurrentSceneType() == SceneType.SCENE_SPLASH) {
	    	((SplashScene)splashScene).getAlphaLayer().registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f) {
	    		@Override
	    		protected void onModifierFinished(IEntity pItem) {
	    			menuScene = new MainMenuScene();
	    	    	currentScene = menuScene;
	    	    	setScene(menuScene);
	    	    	((MainMenuScene)menuScene).getMenu_background().registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
	    			super.onModifierFinished(pItem);
	    		}
	    	});
    	} else if (getCurrentSceneType() == SceneType.SCENE_GAME) {
    		menuScene = new MainMenuScene();
	    	currentScene = menuScene;
	    	setScene(menuScene);
	    	((MainMenuScene)menuScene).getMenu_background().registerEntityModifier(new AlphaModifier(C.SCREENSWITCHDUR, 0f, 1f));
    	}  	
    }
    
    public void createGameScene()
    {
    	gameScene = new GameScene();
    	currentScene = gameScene;
    	setScene(gameScene);
    }
    

    
    //---------------------------------------------
    // GETTERS AND SETTERS
    //---------------------------------------------
    
    public static SceneManager getInstance()
    {
        return INSTANCE;
    }
    
    public SceneType getCurrentSceneType()
    {
        return currentSceneType;
    }
    
    public BaseScene getCurrentScene()
    {
        return currentScene;
    }
    

    
    
}