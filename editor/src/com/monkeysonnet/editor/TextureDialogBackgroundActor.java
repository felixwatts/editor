package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class TextureDialogBackgroundActor extends Actor
{
	private static final float TILE_SIZE = 32f;
	private TextureRegion _tex = Z.textures.get("grid");
	
	public TextureDialogBackgroundActor(Stage stage)
	{
		_tex.setRegion(0, 0, stage.width()/TILE_SIZE, stage.height()/TILE_SIZE);	
		stage.addActor(this);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(Color.WHITE);
		batch.draw(_tex, 0, 0, 0, 0, stage.width(), stage.height(), 1f, 1f, 0);
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}
}
