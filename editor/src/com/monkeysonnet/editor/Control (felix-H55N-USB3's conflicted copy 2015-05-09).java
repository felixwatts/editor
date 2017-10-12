package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.monkeysonnet.engine.Game;

public abstract class Control implements IControl
{
	private BitmapFontCache _labelCache;
	
	public abstract void label(String label);
	public abstract String label();
	public abstract void properties(String props);
	public abstract String properties();
	
	protected void refreshLabelCache()
	{
		if(label() == null)
		{
			if(_labelCache != null)
			{
				_labelCache.dispose();
				_labelCache = null;
			}
		}
		else
		{
			if(_labelCache == null)
			{
				_labelCache = new BitmapFontCache(Z.font);
				_labelCache.setColor(Color.BLACK);			
			}
			
			_labelCache.setText(label(), 0, 0);
		}
	}
	
	@Override
	public void drawLabel(SpriteBatch sprites)
	{
		if(_labelCache != null)
		{
			Z.renderer.worldToScreen(Game.workingVector2a.set(getX(), getY()));
			_labelCache.setPosition(Game.workingVector2a.x, Game.workingVector2a.y);
			_labelCache.draw(sprites);
		}
	}
}
