package com.monkeysonnet.editor;

import com.badlogic.gdx.math.Vector2;

public interface IShape
{
	Iterable<Vector2> getVertices();
	Vector2 addVertex(float x, float y, Vector2 after);
	void removeVertex(Vector2 v);
	void moveVertex(Vector2 v, float newX, float newY);
	boolean canEditShape();
}
