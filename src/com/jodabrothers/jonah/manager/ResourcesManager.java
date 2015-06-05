/*
 * ResourcesManager
 * 
 * Resource handling class. Handles loading and provides access through public fields
 * Graphic resources - implemented with BitmapTextureAtlases and TextureRegions					XXX
 * Sound resources - handled by SoundManager
 * Provides access to social media through ShareManager 
 * Provides access to High Score using Android SharedPreference
 * Holds data for game count, level and theme
 */

package com.jodabrothers.jonah.manager;

import org.andengine.engine.Engine;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.engine.camera.Camera;
import org.andengine.util.debug.Debug;

import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegionFactory;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTiledTextureRegion;

import android.graphics.Color;

import android.content.Context;
import android.content.SharedPreferences;

import com.jodabrothers.jonah.GameActivity;
import com.jodabrothers.jonah.constants.C;

public class ResourcesManager
{
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------
    
	// Creating static final instance of ResourcesManager (accessed throughout the application through static getInstance() method)	 
    private static final ResourcesManager INSTANCE = new ResourcesManager();
    public static final String PREFS_NAME = "SnailPrefsFile"; // sharedPreferences key
    
    public enum LEVEL {EASY, MEDIUM, HARD}; // game difficulties
    
    // AndEngine basic variables
    public Engine engine;
    public GameActivity activity;
    public Camera camera;
    public VertexBufferObjectManager vbom;
    
    // Manager classes
    public ShareManager shareManager;
    public SoundManager soundManager;
    
    // Basic game variables access throughout all game scenes and classes
    private String shareString; // social media status  
    private float camera_speed;	// speed of background (0.7 - 1 pixel/frame)
    private LEVEL level; // chosen difficulty
    private boolean theme; // chosen theme
    public boolean isFirstRun; // tutorial is played on first run. Variable saved in sharedPreferences
    private int game_count; // game count - for ads and share window
    
    //---------------------------------------------
    // TEXTURES & TEXTURE REGIONS
    //---------------------------------------------
    
    // Loading screen
    public ITextureRegion trademark_region;
    public ITextureRegion loading_region;
    public ITextureRegion presents_region;
    
    // Menu screen
    public ITextureRegion menu_background_region;
    public ITextureRegion theme_pick_background_region;
    public ITextureRegion level_pick_background_region;
    public ITextureRegion black_background_region;
    // blank transparent picture - used for touch areas
    public ITextureRegion button_region;
    
    // Game screen
    // Player texture
    public PixelPerfectTiledTextureRegion animated_player_region;
    
    // Background texture dark 
    public ITextureRegion dark_background_region;
    
    // HUD, GameOver, Grass textures dark
    public ITextureRegion dark_grass_region;
    public ITextureRegion dark_left_button_region;
    public ITextureRegion dark_right_button_region;
    public ITextureRegion dark_left_bonus_active_region;
    public ITextureRegion dark_right_bonus_active_region;
    public ITextureRegion dark_left_bonus_inactive_region;
    public ITextureRegion dark_right_bonus_inactive_region;
    public ITextureRegion dark_game_over_region;
    public ITextureRegion dark_game_over_text1_region;
    public ITextureRegion dark_game_over_text2_region;
    public ITextureRegion dark_rate_screen_region; // after 7th game rate window is displayed
        
    // Feet textures dark
    public PixelPerfectTextureRegion dark_left_sneaker_region;
    public PixelPerfectTextureRegion dark_right_sneaker_region;
    public PixelPerfectTextureRegion dark_left_highheel_region;    
    public PixelPerfectTextureRegion dark_right_highheel_region;
    public PixelPerfectTextureRegion dark_left_boot_region;
    public PixelPerfectTextureRegion dark_right_boot_region;    
    
    // Background texture light
    public ITextureRegion light_background_region;
    
    // HUD, GameOver, Grass, Tutorial textures light
    public ITextureRegion light_grass_region;
    public ITextureRegion light_left_button_region;
    public ITextureRegion light_right_button_region;
    public ITextureRegion light_left_bonus_active_region;
    public ITextureRegion light_right_bonus_active_region;
    public ITextureRegion light_left_bonus_inactive_region;
    public ITextureRegion light_right_bonus_inactive_region;
    public ITextureRegion light_game_over_region;
    public ITextureRegion light_game_over_text1_region;
    public ITextureRegion light_game_over_text2_region;
    public ITextureRegion light_tutorial_1_region;
    public ITextureRegion light_tutorial_2_region;
    public ITextureRegion light_tutorial_3_region;
    public ITextureRegion light_tutorial_4_region;
    public ITextureRegion light_tutorial_arrow_region;
    public ITextureRegion light_sprint_arrow_region;
    public ITextureRegion light_back_arrow_region;
    
    // Feet textures light
    public PixelPerfectTextureRegion light_left_sneaker_region;
    public PixelPerfectTextureRegion light_right_sneaker_region;
    public PixelPerfectTextureRegion light_left_highheel_region;
    public PixelPerfectTextureRegion light_right_highheel_region;    
    public PixelPerfectTextureRegion light_left_boot_region;
    public PixelPerfectTextureRegion light_right_boot_region;
    
    // Bonus textures
    public PixelPerfectTextureRegion leaf_region;
    public PixelPerfectTextureRegion bubble_region;
    
    // Achievements textures
    public TiledTextureRegion plane_region;
    public TiledTextureRegion ufo_region;
    public TiledTextureRegion balloon_region;    
    
    // Fonts
    public Font dark_screen_font;
    public Font light_screen_font;
        
    

    //---------------------------------------------
    // CLASS LOGIC
    //---------------------------------------------
    
    // Manager initialization
    public static void prepareManager(Engine engine, GameActivity activity, Camera camera, VertexBufferObjectManager vbom)
    {
    	getInstance().camera = camera;
        getInstance().engine = engine;
        getInstance().activity = activity;       
        getInstance().vbom = vbom;
        getInstance().shareManager = new ShareManager(activity);
    }
      
    public void loadSplashResources()
    {
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
    	BitmapTextureAtlas splashTextureAtlasA = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
    	trademark_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlasA, activity, "trademark.png", 0, 0);
    	loading_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlasA, activity, "loading_screen.png", 0, 481);
    	
    	BitmapTextureAtlas splashTextureAtlasB = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
    	black_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlasB, activity, "black_background.png", 0, 0);
    	presents_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlasB, activity, "presents.png", 0, 481);
    	
    	splashTextureAtlasA.load();
    	splashTextureAtlasB.load();
    }
    
    public void loadMenuResources()
    {
        loadMenuGraphics();
        loadMenuFonts();
        
    }
    
    public void loadGameResources()
    {    
        loadGameGraphics();
        loadGameAudio();        
    }
    
    
    // Menu resources loading
    
    private void loadMenuGraphics()
    {
    	// setting asset path
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
    	// creating BitmapTextureAtlas
    	BitmapTextureAtlas menuTextureAtlasA = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
    	// loading textures from assets into Atlas, setting public access variables
    	menu_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlasA, activity, "play_screen.png", 0, 0);
    	theme_pick_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlasA, activity, "theme_pick.png", 0, 481);
    	
    	BitmapTextureAtlas menuTextureAtlasB = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
    	level_pick_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlasB, activity, "level_pick.png", 0, 0);
    	button_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlasB, activity, "button.png", 0, 481);    	
    	
    	// Atlas loading
    	menuTextureAtlasA.load();
    	menuTextureAtlasB.load();     
    }
    
    private void loadMenuFonts()
    {    	
    	FontFactory.setAssetBasePath("font/");
        final ITexture darkScreenFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);        
        dark_screen_font = FontFactory.createStrokeFromAsset(activity.getFontManager(), darkScreenFontTexture, activity.getAssets(), "font.ttf", 50f, true, Color.parseColor("#BDAB80"), 0f, Color.parseColor("#BDAB80"));        
        dark_screen_font.load();
        
        final ITexture lightScreenFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        light_screen_font = FontFactory.createStrokeFromAsset(activity.getFontManager(), lightScreenFontTexture, activity.getAssets(), "font.ttf", 50f, true, Color.parseColor("#190020"), 0f, Color.parseColor("#190020"));// 835857
        light_screen_font.load();
    }
    
    // Game resources loading
    
    /*
     * Game textures
     * using BuildableBitmapTextureAtlas - maximum size 1024*1024 for older devices support
     * textures are builded into BitmapTextureAtlas canvas, access is provided through ResourcesManager public fields 
     */
    private void loadGameGraphics()
    {    	
    	final TextureManager textureManager = activity.getTextureManager();
    	
	    /////
		/////LIGHT/////
		/////
    	
    	// setting asset path
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/dark/");
    	
    	// Creating buildable atlas
    	BuildableBitmapTextureAtlas backgroundDarkTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
    	// loading textures from assets into Atlas, setting public access variables
    	dark_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundDarkTextureAtlas, activity, "background.png");    	
    	dark_grass_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundDarkTextureAtlas, activity, "grass.png");
    	// building and loading atlas
    	try 
    	{
    		backgroundDarkTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
    		backgroundDarkTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}  
    	    	
    	BuildableBitmapTextureAtlas gameDarkTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);    	
    	dark_rate_screen_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameDarkTextureAtlas, activity, "rate_screen.png");
    	try 
    	{
    		gameDarkTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
    		gameDarkTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}  

    	BuildableBitmapTextureAtlas GameOverDarkAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
    	dark_game_over_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(GameOverDarkAtlas, activity, "game_over.png");
        dark_game_over_text2_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(GameOverDarkAtlas, activity, "game_over_text2.png");
        try 
    	{
        	GameOverDarkAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        	GameOverDarkAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}  
           
        BuildableBitmapTextureAtlas darkHudTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
        dark_game_over_text1_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "game_over_text1.png");
        dark_left_button_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "left.png");
        dark_right_button_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "right.png");
        dark_left_bonus_active_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "bonus_left_active.png");
        dark_right_bonus_active_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "bonus_right_active.png");
        dark_left_bonus_inactive_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "bonus_left_inactive.png");
        dark_right_bonus_inactive_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(darkHudTextureAtlas, activity, "bonus_right_inactive.png");        
        try 
    	{
        	darkHudTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        	darkHudTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}   
        
        PixelPerfectTextureRegionFactory.setAssetBasePath("gfx/game/dark/");
        BuildableBitmapTextureAtlas pixelPerfectBitmapDarkTextureAtlas = new BuildableBitmapTextureAtlas(textureManager,1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        dark_right_sneaker_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "right_feet.png", false, 50);
		dark_left_sneaker_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "left_feet.png", false, 50);
		dark_right_highheel_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "high_heel_right.png", false, 50);		
		dark_left_highheel_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "high_heel_left.png", false, 50);
		dark_right_boot_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "boot_right.png", false, 50);
		dark_left_boot_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "boot_left.png", false, 50);
		
		leaf_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "leaf.png", false, 50);
		bubble_region  = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapDarkTextureAtlas, this.activity.getAssets(), "bubble_shield.png", false, 50);
		try 
    	{
			pixelPerfectBitmapDarkTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
			pixelPerfectBitmapDarkTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}   
		        
		/////
		/////LIGHT/////
		/////		
		
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/light/");
        BuildableBitmapTextureAtlas backgroundLightTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
    	light_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundLightTextureAtlas, activity, "background.png");    	
    	light_grass_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundLightTextureAtlas, activity, "grass.png");
    	try 
    	{
    		backgroundLightTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
    		backgroundLightTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}       

    	BuildableBitmapTextureAtlas GameOverLightAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
    	light_game_over_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(GameOverLightAtlas, activity, "game_over.png");
        light_game_over_text2_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(GameOverLightAtlas, activity, "game_over_text2.png");
        try 
    	{
        	GameOverLightAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        	GameOverLightAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}   
        
        
        BuildableBitmapTextureAtlas fuckingStubbornGameOverLightTextAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
        light_game_over_text1_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "game_over_text1.png");
        light_left_button_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "left.png");
        light_right_button_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "right.png");
        light_left_bonus_active_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "boost_left_active.png");
        light_right_bonus_active_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "boost_right_active.png");
        light_left_bonus_inactive_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "boost_left_inactive.png");
        light_right_bonus_inactive_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fuckingStubbornGameOverLightTextAtlas, activity, "boost_right_inactive.png");
        try 
    	{
        	fuckingStubbornGameOverLightTextAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        	fuckingStubbornGameOverLightTextAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}        
        
        BuildableBitmapTextureAtlas tutorialLightTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
        light_tutorial_1_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "tutorial_1.png");
        light_tutorial_2_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "tutorial_2.png");
        light_tutorial_3_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "tutorial_3.png");
        light_tutorial_4_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "tutorial_4.png");
        light_tutorial_arrow_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "arrow.png");        
        light_back_arrow_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "go_back_arrow.png");
        light_sprint_arrow_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialLightTextureAtlas, activity, "sprint_arrow.png");
        try 
    	{
        	tutorialLightTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        	tutorialLightTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}
        
        PixelPerfectTextureRegionFactory.setAssetBasePath("gfx/game/light/");
        BuildableBitmapTextureAtlas pixelPerfectBitmapLightTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        light_right_sneaker_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "right_feet.png", false, 50);
        light_left_sneaker_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "left_feet.png", false, 50);
        light_right_highheel_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "high_heel_right.png", false, 50);              
        light_left_highheel_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "high_heel_left.png", false, 50);
        light_right_boot_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "boot_right.png", false, 50);
		light_left_boot_region = PixelPerfectTextureRegionFactory.createFromAsset(pixelPerfectBitmapLightTextureAtlas, this.activity.getAssets(), "boot_left.png", false, 50);                				
		try 
    	{
			pixelPerfectBitmapLightTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
			pixelPerfectBitmapLightTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}
    	
    	PixelPerfectTextureRegionFactory.setAssetBasePath("gfx/game/");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");		
		BuildableBitmapTextureAtlas pixelPerfectAnimatedBitmapTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1000, 1000, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		animated_player_region = PixelPerfectTextureRegionFactory.createTiledFromAsset(pixelPerfectAnimatedBitmapTextureAtlas, this.activity.getAssets(), "animated_player.png", 8, 1, false, 50);
    	try 
    	{
    	    pixelPerfectAnimatedBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
    	    pixelPerfectAnimatedBitmapTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	    Debug.e(e);
    	}
    	
    	BuildableBitmapTextureAtlas	achievementTextureAtlas = new BuildableBitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR);
    	plane_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(achievementTextureAtlas, activity, "animated_plane.png", 4 , 1);
    	ufo_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(achievementTextureAtlas, activity, "animated_ufo.png", 4 , 1);
    	balloon_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(achievementTextureAtlas, activity, "animated_balloon.png", 4 , 1);
    	try 
    	{
    	    achievementTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
    	    achievementTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	        Debug.e(e);
    	}    
    }
    
    private void loadGameAudio()
    {
    	// creating sound manager
        this.soundManager = new SoundManager(activity, engine.getMusicManager());        
    }          
        
    // Setters, Getters, other methods
    // accessing static instance of Resources Manager
    public static ResourcesManager getInstance()
    {
        return INSTANCE;
    }

    // Theme getter and setter
	public boolean getTheme() {
		return theme;
	}

	public void setTheme(boolean theme) {
		this.theme = theme;
	}

	// Difficulty and camera speed getter and setter
	public LEVEL getLevel() {
		return level;
	}

	public void setLevel(LEVEL level) {
		this.level = level;
		// camera speed varies with difficulty 
		this.camera_speed = (level == LEVEL.HARD) ? C.CAMERASPPEDFAST : C.CAMERASPPEDSLOW;
	}
	
	public float getCameraSpeed() {
		return camera_speed;
	}
	
	// Game count getter and setter
    public int getGameCount() {
    	return game_count;
    }
    
    public void setGameCount(int p_game_count) {
    	this.game_count = p_game_count;
    }
    
    public void gameCountIncrement() {
    	this.game_count++;
    }

    // Share string getter and setter
	public String getShareString() {
		
		return shareString;
	}

	public void setShareString(float distance) {
		String level = null;
		switch(this.level) {
		case EASY:
			level = "Easy";
			break;
		case MEDIUM:
			level = "Normal";
			break;
		case HARD:
			level = "Poor Snail";
			break;
		}
		this.shareString = "I've just run " + String.format("%.2f", distance) + " meters on " + level + " level";		
	}
	
	// High score. Saved in shared preferences - one field for each difficulty
    public float loadHighScore() {
    	SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	switch (level) {
    	default:
    	case EASY:    	
    		return prefs.getFloat("top_score_easy", 0.0f);
    	case MEDIUM:
    		return prefs.getFloat("top_score_medium", 0.0f);
    	case HARD:    	
    		return prefs.getFloat("top_score_hard", 0.0f);    		
    	}
        
    }
    
    public void setHighScore(float score) {
    	SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (level) {
    	default:
    	case EASY:    	
    		editor.putFloat("top_score_easy", score); 
    		break;
    	case MEDIUM:
    		editor.putFloat("top_score_medium", score);
    		break;
    	case HARD:    	
    		editor.putFloat("top_score_hard", score);
    		break;
    	}           
        editor.commit();
    }
    
    //First run. Saved in shared preferences - used for tutorial display
    public boolean loadFirstRun() {
    	SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("firstRun", true);
    }
    
    public void setFirstRun(boolean install) {
    	SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("firstRun", install);        
        editor.commit();
    }
        
    //Sound Manager
	public void playBubble() {
		this.soundManager.playBubble();
    }
    
    public void playLeaf() {
    	this.soundManager.playLeaf();
    }
    
    public void playGameOver() {
    	this.soundManager.playGameOver();
    }
    
    public void playGameMusic() {
    	this.soundManager.playGameMusic();
    }
    
    public void stopGameMusic() {
    	this.soundManager.stopGameMusic();
    }
    
    public boolean isGameMusicPlaying() {
    	return this.soundManager.isGameMusicPlaying();
    }
    
    public void pauseMusic() {
    	this.soundManager.pauseMusic();
    }
    
    public void resumeMusic() {
    	this.soundManager.resumeMusic();
    }
    
    public void reloadMusic() {
    	this.soundManager.reloadMusic();
    }
    
    
}


