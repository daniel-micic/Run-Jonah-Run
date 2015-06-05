/*
 * Splash screen
 * 
 * Intro animation
 * Resources are loaded in background
 */

package com.jodabrothers.jonah.scene;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.sprite.Sprite;

import com.jodabrothers.jonah.base.BaseScene;
import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.SceneManager.SceneType;

public class SplashScene extends BaseScene
{
	// intro aniamtion objects
	private Sprite trademark;
	private Sprite loading;
	private Sprite alphaLayer;
	private Sprite presents;
	
	// creating scene
    @Override
    public void createScene()
    {
    	trademark = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, resourcesManager.trademark_region, vbom);    	
    	presents = new Sprite(C.CAMERAWIDTH/2,  C.CAMERAHEIGHT/4, resourcesManager.presents_region, vbom);
    	loading = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, resourcesManager.loading_region, vbom);   	
    	alphaLayer = new Sprite(C.CAMERAWIDTH/2, C.CAMERAHEIGHT/2, resourcesManager.black_background_region, vbom);
    	trademark.setAlpha(0f);
    	alphaLayer.setAlpha(0f);
    	presents.setAlpha(0f);
    	loading.setAlpha(0f);      	
      	
    	attachChild(trademark);
    	attachChild(presents);    	
    	attachChild(alphaLayer);
    	attachChild(loading); 
    	
    	
    }
    
    // animating scene : Company logo -> fade out/in -> Game intro screen 
    public void animate() {
    	trademark.registerEntityModifier(new AlphaModifier(1f, 0f, 1f) {
			@Override
			protected void onModifierFinished(IEntity pItem) {
				presents.registerEntityModifier(new AlphaModifier(1f, 0f, 1f) {
					@Override
					protected void onModifierFinished(IEntity pItem) {
						engine.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback()	    
					    {
					            public void onTimePassed(final TimerHandler pTimerHandler) 
					            {
					            	engine.unregisterUpdateHandler(pTimerHandler);	 
					            	alphaLayer.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f) {
										protected void onModifierFinished(IEntity pItem) {
											loading.registerEntityModifier(new AlphaModifier(0.5f, 0f, 1f)); 	    			   	    			
										}
									});	                
					            }
					    }));
						
						super.onModifierFinished(pItem);
				}
		    	});   	
			}
		});
    }
    
    public Sprite getAlphaLayer() {
    	return alphaLayer;
    }
    	
    @Override
    public void onBackKeyPressed()
    {
    	// void
    }

    @Override
    public SceneType getSceneType()
    {
    	return SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene()
    {
    	trademark.detachSelf();
    	trademark.dispose();
    	loading.detachSelf();
    	loading.dispose();
		this.detachSelf();
    	this.dispose();
    }
    
    
}