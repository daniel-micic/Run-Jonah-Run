/*
 * Sound Manager
 * 
 * Handles playing music and sound
 */
package com.jodabrothers.jonah.manager;

import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.music.MusicManager;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;

import com.jodabrothers.jonah.GameActivity;

public class SoundManager {
	// Sounds
	public Sound bubble;
    public Sound leaf;
    public Sound game_over;
    
    // Game music
    public Music game_music;
        
    // Top level elements
    private GameActivity activity;
    private MusicManager musicManager;
    
    // state variable
    private boolean game_music_playing;
    
    // Constructor with initialization and loading
    public SoundManager (GameActivity pActivity, MusicManager pMusicManager) {
    	activity = pActivity;
    	musicManager = pMusicManager;
    	    
    	// Sounds
    	try {
			this.bubble = SoundFactory.createSoundFromAsset(activity.getSoundManager(), activity.getApplicationContext(), "snd/bubble.ogg");
			this.bubble.setVolume(0.2f);
			this.leaf = SoundFactory.createSoundFromAsset(activity.getSoundManager(), activity.getApplicationContext(), "snd/leaf.ogg");
			this.leaf.setVolume(2f);
			this.game_over = SoundFactory.createSoundFromAsset(activity.getSoundManager(), activity.getApplicationContext(), "snd/game_over.ogg");
			this.game_over.setVolume(0.5f);
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	
    	// Music
    	MusicFactory.setAssetBasePath("snd/");
    	try {
    		this.game_music = MusicFactory.createMusicFromAsset(musicManager, activity, "game_music.ogg");
    		this.game_music.setLooping(true);  
    		this.game_music.setVolume(0.5f);
    	} catch (final IOException e) {
    		System.out.println("Music load failed");
    	}
    }
       
    // playing and pausing Sounds and Music
    public void playBubble() {
    	this.bubble.play();
    }
    
    public void playLeaf() {
    	this.leaf.play();
    }
    
    public void playGameOver() {
    	this.game_over.play();
    }
    
    public void playGameMusic() {
    	this.game_music.play();  
    	this.game_music_playing = true;
    }
    
    public void stopGameMusic() {
    	this.game_music.stop();   
    	this.game_music_playing = false;
    }
        
    public boolean isGameMusicPlaying() {
    	return this.game_music_playing;
    }
        
    public void pauseMusic() {
    	this.game_music.pause();
    }
    
    public void resumeMusic() {
    	this.game_music.resume();
    }
    
    public void reloadMusic() {
    	MusicFactory.setAssetBasePath("snd/");
    	try {
    		this.game_music = MusicFactory.createMusicFromAsset(musicManager, activity, "game_music.ogg");
    		this.game_music.setLooping(true); 
    		this.game_music.setVolume(0.5f);
    	} catch (final IOException e) {
    		System.out.println("Music load failed");
    	}
    }
    
    
	
}
