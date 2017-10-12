package com.monkeysonnet.editor;

import com.badlogic.gdx.InputProcessor;
import com.monkeysonnet.engine.IButtonEventHandler;

public interface ITool extends IButtonEventHandler, InputProcessor
{
	void init();
	void activate();
	void deactivate();
	void onActiveItemChanged();
	IControl copyActiveItem();
}
