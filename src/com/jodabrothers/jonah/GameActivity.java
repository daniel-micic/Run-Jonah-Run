/* Run Jonah Run 
 * 
 * GameActivity
 * 
 * main class of application - Android entry point of whole software. In first place, the Engine is created. 
 * Then Resources Manager is instantiated and resources for splash screen are loaded. Next step is creating and populating 
 * splash scene. While the game shows SplashScene (Logo and loading) the Resources Manager is loading MenuScene and GameScene 
 * resources in background. Ad object are initialized. After the loading is done, control is handed to MenuScene.
 * 
 * Daniel Micic
 * 2015 
 */ 

package com.jodabrothers.jonah;

import java.io.IOException;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

import android.app.Activity;
import android.view.KeyEvent;

import com.jodabrothers.jonah.constants.C;
import com.jodabrothers.jonah.manager.ResourcesManager;
import com.jodabrothers.jonah.manager.SceneManager;
import com.jodabrothers.jonah.manager.SceneManager.SceneType;
import com.jodabrothers.jonah.scene.SplashScene;

/*import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.purplebrain.adbuddiz.sdk.AdBuddizDelegate;
import com.purplebrain.adbuddiz.sdk.AdBuddizError;*/

public class GameActivity extends BaseGameActivity {
	
	// helping fields
	private Camera camera; // camera for internal use of AndEngine LimitedFPSEngine
	private boolean game_loaded = false; // checks whether the games is loaded for first time - pausing/playing music
	private SceneType adScene; // decides which scene to show after Ad	
	final Activity activity = this;	// used for access in Time Handler onTimePassed method

	@Override
	/*
	 * Sets basic Engine Options
	 * Camera Width : 800
	 * Camera Height : 480
	 * Screen Orientation : Landscape_Fixed
	 * Needs Sound & Music : True
	 * Needs MultiTouch : True
	 * Dithering ; True 
	 */
	
	public EngineOptions onCreateEngineOptions() {
	    camera = new Camera(0, 0, C.CAMERAWIDTH, C.CAMERAHEIGHT);
	    EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(C.CAMERAWIDTH, C.CAMERAHEIGHT), this.camera);
	    engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
	    engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
	    engineOptions.getTouchOptions().setNeedsMultiTouch(true);	    
	    engineOptions.getRenderOptions().setDithering(true);
	    return engineOptions;
	}
	
	@Override
	/*
	 * Creates Game Engine 
	 * This application uses LimitedFPSEngine of great open-source AndEngine
	 * This engine tries to update the game 60 times a second
	 */
	public Engine onCreateEngine(EngineOptions pEngineOptions) 
	{
	    return new LimitedFPSEngine(pEngineOptions, 60);
	}

	@Override
	/*
	 * Initial resources loading
	 * Prepares static instance of Resources Manager
	 * Indicates first run of application - tutorial is shown
	 */
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException
	{		
	    ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
	    ResourcesManager.getInstance().loadSplashResources();
	    ResourcesManager.getInstance().isFirstRun = ResourcesManager.getInstance().loadFirstRun();  
	    pOnCreateResourcesCallback.onCreateResourcesFinished();	        	    
	}

	@Override
	/*
	 * Static instance of SceneManager creates SplashScene 
	 */
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException
	{	
		pOnCreateSceneCallback.onCreateSceneFinished(SceneManager.getInstance().createSplashScene());
	}
	
	@Override
	/*
	 * Animation of SplashScene is started
	 * TimeHandler is set for 3.5 seconds
	 * 		ResourcesManager loads Menu and Game Scene
	 * 		Ads are initialized
	 * 		GameCount set for 1 (Ads are shown after every third game)
	 * 		Splash screen is blackened and SceneMnager creates Menu Scene 
	 */
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException
	{
		((SplashScene)SceneManager.getInstance().getCurrentScene()).animate();	

	    mEngine.registerUpdateHandler(new TimerHandler(3.5f, new ITimerCallback()	    
	    {
	            public void onTimePassed(final TimerHandler pTimerHandler) 
	            {
	            	game_loaded = true;
	            	
	                mEngine.unregisterUpdateHandler(pTimerHandler);        
	                //resources loading
	                ResourcesManager.getInstance().loadMenuResources();
	        	    ResourcesManager.getInstance().loadGameResources();	 
	        	    
	        	    //Ad initialization
	        		AdBuddiz.setPublisherKey("4c2e8de3-75a4-4bec-a86f-73327cb644da");  
	        		AdBuddiz.setTestModeActive();
	        		AdBuddiz.cacheAds(activity);                         
	        		
	        		AdBuddiz.setDelegate(new AdBuddizDelegate() {
	        			@Override
	        			public void didCacheAd() {	        				
	        			}
	        			@Override
	        			public void didShowAd() {

	        			}
	        			@Override
	        			public void didFailToShowAd(AdBuddizError error) {
	        				
	        			}
	        			@Override
	        			public void didClick() {

	        			}
	        			@Override
	        			// On Ad closed
	        			public void didHideAd() {
	        				// adScene is set when player touches button on Game Over Screen  
	        				if (adScene == SceneType.SCENE_GAME)  
	        					SceneManager.getInstance().createGameScene();
	        				else
	        					SceneManager.getInstance().createMenuScene();
	        			}
	        		});
	        	            	    
	        	    ResourcesManager.getInstance().setGameCount(1);
	        	    ((SplashScene)SceneManager.getInstance().getCurrentScene()).getAlphaLayer().registerEntityModifier(new AlphaModifier(0.2f, 0f, 1f) {
	        	    	@Override
	        	    	protected void onModifierFinished(IEntity pItem) {	        	    	
	        	    		SceneManager.getInstance().createMenuScene(); 
	        	    		super.onModifierFinished(pItem);
	        	    	}
	        	    });       	    		              	               	             
	            }
	    }));	    
	    pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	@Override
	/*
	 * Handles back Button of Smartphone
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{  
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	        SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
	    }
	    return false; 
	}
	
	@Override
	/*
	 * Android Activity handling
	 * onDestroy - exit application
	 * onResume - play music
	 * onPause - pause music
	 */
	protected void onDestroy()
	{
		super.onDestroy();
	    System.exit(0);	
	}
	

	public void onResume() {
		super.onResume();
		if (game_loaded)
			ResourcesManager.getInstance().resumeMusic();
				
	}
	
	public void onPause() {
		if (game_loaded)
			ResourcesManager.getInstance().pauseMusic();
		super.onPause();		
	}
	
	public void showAd(SceneType sceneType) {
		adScene = sceneType;
		if (AdBuddiz.isReadyToShowAd(this))
			AdBuddiz.showAd(this);
		if (adScene == SceneType.SCENE_GAME)  
			SceneManager.getInstance().createGameScene();
		else
			SceneManager.getInstance().createMenuScene();
		
	}
	

	
	/*@Override
	protected void onSetContentView() {
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

        final FrameLayout frameLayout = new FrameLayout(this);
        final FrameLayout.LayoutParams frameLayoutLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                             FrameLayout.LayoutParams.MATCH_PARENT);
 
        final AdView adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        
        DisplayMetrics dm = getResources().getDisplayMetrics();
        
        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y);

        if (screenInches > 8) { // > 728 X 90
        	adView.setAdSize(AdSize.LEADERBOARD);
        } else if (screenInches > 6) { // > 468 X 60
        	adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        } else { // > 320 X 50
        	adView.setAdSize(AdSize.BANNER);
        }
        
        adView.refreshDrawableState();
        adView.setVisibility(AdView.VISIBLE);
       
        final FrameLayout.LayoutParams adViewLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                             FrameLayout.LayoutParams.WRAP_CONTENT,
                                             Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        
        adViewLayoutParams.setMargins(0, 0, 0, 5);
 
        AdRequest adRequest = new AdRequest.Builder().addKeyword("Game Chat Internet").build();
        adView.loadAd(adRequest);
 
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        this.mRenderSurfaceView.setRenderer(this.mEngine, this);
        
        final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams =
                new FrameLayout.LayoutParams(size.x, size.y);
        surfaceViewLayoutParams.gravity = Gravity.CENTER;
 
        frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
        frameLayout.addView(adView, adViewLayoutParams);
 
        this.setContentView(frameLayout, frameLayoutLayoutParams);
	}*/
	


}
