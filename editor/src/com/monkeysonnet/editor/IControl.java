package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IControl extends ILocation, ILabelled
{
	void onActivate();
	void onDeactivate();		
	boolean testPointInside(float x, float y);
	void drawLabel(SpriteBatch sprites);
}
