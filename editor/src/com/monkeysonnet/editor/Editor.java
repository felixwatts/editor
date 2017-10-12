package com.monkeysonnet.editor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;

public class Editor implements ApplicationListener 
{
	@Override
	public void create() 
	{
		Z.textures = new MultiTextureSource();
		Z.renderer = new Renderer();
		Z.font = new BitmapFont(Gdx.files.internal("font.fnt"), Z.textures.get("font"), false);
		Z.ninePatch = new NinePatch(Z.textures.get("nine-patch"), 8, 8, 8, 8);
		Z.editor = new EditorScreen();		
		Z.screens.push(Z.editor);
	}

	@Override
	public void dispose() 
	{
		Z.editor.ensureNoUnsavedChanges();
	}

	@Override
	public void render() 
	{
		Z.screens.render();
	}

	@Override
	public void resize(int width, int height) 
	{
		Z.renderer.resize((float)width, (float)height);
		Z.editor.resize(width, height);
	}

	@Override
	public void pause() 
	{
	}

	@Override
	public void resume() 
	{
	}
}
