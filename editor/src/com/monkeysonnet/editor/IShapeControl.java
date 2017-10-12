package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.monkeysonnet.engine.editor.Shape;

public interface IShapeControl extends IMovable, IControl, IShape, ICopyableControl
{
	Shape data();
	Iterable<Sprite> getEdgeSprites();
	void updateShapeData();
}
