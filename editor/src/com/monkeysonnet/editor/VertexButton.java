package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.IProjection;
import com.monkeysonnet.engine.WorldButton;

public class VertexButton extends WorldButton
{
	private Vector2 _v;
	
	public VertexButton(Vector2 v, IButtonEventHandler handler, IProjection renderer)
	{
		super(
				v.x, 
				v.y, 
				EditorScreen.RESIZE_BUTTON_SIZE, 
				EditorScreen.RESIZE_BUTTON_SIZE, 
				false, 
				false, 
				-1, 
				Z.textures.get("solid"), 
				handler, 
				renderer);
		
		_v = v;
		
		this.color.set(Color.MAGENTA);
	}
	
	public Vector2 getVertex()
	{
		return _v;
	}
}
