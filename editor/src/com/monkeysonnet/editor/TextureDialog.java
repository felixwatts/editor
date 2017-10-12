package com.monkeysonnet.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.IDialogResultHandler;
import com.monkeysonnet.engine.IScreen;

public class TextureDialog implements IScreen, IButtonEventHandler
{
	private static final float PADDING = 8;
	private static final float BUTTON_SCALE = 0.5f;
	private static final String TEXTURE_FILTER_PREFIX = "env-";
	private static final float MIN_BUTTON_SIZE = 64f;
	
	private Stage _stage;
	private AtlasRegion _selectedTex;
	private IDialogResultHandler _handler;
	private String _filter;	
	
	public TextureDialog(IDialogResultHandler handler)
	{
		_handler = handler;
	}
	
	@Override
	public void pause()
	{
	}
	
	public void setFilter(String filter)
	{
		if(_filter == filter)
			return;
		
		if(_stage != null)
			_stage.dispose();
		
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		new TextureDialogBackgroundActor(_stage);
		
		FlickScrollPane pane = new FlickScrollPane();
		pane.x = 0;
		pane.y = 0;
		pane.width = _stage.width();
		pane.height = _stage.height();
		pane.color.set(Color.MAGENTA);
		_stage.addActor(pane);	
		
		FlowGroup group = new FlowGroup(_stage.width(), PADDING, 128);
		
		for(AtlasRegion tex : Z.textures.getAll())
		{
			if(tex.name.startsWith(filter))
			{
				ButtonActor btn = new ButtonActor(0, 0, tex.getRegionWidth() * BUTTON_SCALE, tex.getRegionHeight() * BUTTON_SCALE, tex, this);
				
				if(btn.width < MIN_BUTTON_SIZE)
					btn.width = MIN_BUTTON_SIZE;
				
				if(btn.height < MIN_BUTTON_SIZE)
					btn.height = MIN_BUTTON_SIZE;
				
				group.addActor(btn);
			}
		}
		
		pane.setWidget(group);
	}

	@Override
	public void show()
	{
		if(_stage == null)
		{
			setFilter(TEXTURE_FILTER_PREFIX);
		}
	}
	
	public AtlasRegion getSelectedTexture()
	{
		return _selectedTex;
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(_stage);
	}

	@Override
	public void render()
	{		
		_stage.draw();
	}

	@Override
	public void blur()
	{
		// no op
	}

	@Override
	public void hide()
	{
		// no op
	}

	@Override
	public boolean isFullScreen()
	{
		return true;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		_selectedTex = (AtlasRegion)((ButtonActor)sender).getTexture();
		if(_handler != null)
			_handler.onDialogResult(this);
		Z.screens.pop();
		return true;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}
}