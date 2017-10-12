package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;

public class NinePatchGroup extends Group
{
	private NinePatch _patch;
	
	public NinePatchGroup(NinePatch patch)
	{
		_patch = patch;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		_patch.draw(batch, x, y, width, height);
		
		super.draw(batch, parentAlpha);
	}
}
