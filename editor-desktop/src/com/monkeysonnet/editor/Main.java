package com.monkeysonnet.editor;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) 
	{
//		Settings settings = new Settings();
//		settings.padding = 2;
//		settings.maxWidth = 512;
//		settings.maxHeight = 512;
//		settings.minWidth = 1;
//		settings.minHeight = 1;
//		settings.incremental = false;
//		settings.pot = true;
//		settings.edgePadding = false;
//		TexturePacker.process(settings, "../images", "./assets");
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "MonkeySonnet Map Editor";
		cfg.useGL20 = false;
		cfg.width = 480;
		cfg.height = 320;
		
		new LwjglApplication(new Editor(), cfg);
	}
}
