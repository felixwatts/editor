package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.editor.Shape;

public class InsertBrushButton extends ButtonActor
{
	private Shape _data;
	
	public InsertBrushButton(Shape data, TextureRegion tex)
	{
		super(0, 0, EditorScreen.BUTTON_WIDTH, EditorScreen.BUTTON_HEIGHT, tex, Z.editor);
		_data = data;
	}
	
	public Shape data()
	{
		return _data;
	}
}
