package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.WorldButton;
import com.monkeysonnet.engine.editor.Point;

public class ControlPoint extends Control implements IControl, IMovable, ICopyableControl
{
	private static final float BUTTON_SIZE = 32;

	private Point _data;
	
	private WorldButton _button = new WorldButton(0, 0, BUTTON_SIZE, BUTTON_SIZE, false, false, -1, Z.textures.get("point-marker"), null, Z.renderer);
	
	public ControlPoint()
	{
		_data = new Point();
		_data.point = new Vector2();
		_button.setVisible(true);
		_button.setTouchable(Touchable.disabled);
		Z.editor.getStage().addActor(_button);
		Z.editor.propsToColour(_data.properties, _button.getColor());
	}
	
	public ControlPoint(Point data)
	{
		_data = data;
        _button.setVisible(true);
        _button.setTouchable(Touchable.disabled);
		_button.setWorldLocation(_data.point.x, _data.point.y);
		Z.editor.getStage().addActor(_button);
		Z.editor.propsToColour(_data.properties, _button.getColor());
		refreshLabelCache();
	}
	
	public Point data()
	{
		return _data;
	}
	
	public void initStage()
	{
        _button.remove();
		Z.editor.getStage().addActor(_button);
	}

	@Override
	public void label(String label)
	{
		_data.label = label;
		refreshLabelCache();
	}

	@Override
	public String label()
	{
		return _data.label;
	}

	@Override
	public void properties(String props)
	{
		_data.properties = props;
		Z.editor.propsToColour(_data.properties, _button.getColor());
	}

	@Override
	public String properties()
	{
		return _data.properties;
	}

	@Override
	public float getX()
	{
		return _data.point.x;
	}

	@Override
	public float getY()
	{
		return _data.point.y;
	}

	@Override
	public void setX(float x)
	{
		_data.point.x = x;
		_button.setWorldLocation(_data.point.x, _data.point.y);
	}

	@Override
	public void setY(float y)
	{
		_data.point.y = y;
		_button.setWorldLocation(_data.point.x, _data.point.y);
	}

	@Override
	public void onActivate()
	{
		_button.getColor().set(Color.YELLOW);
	}

	@Override
	public void onDeactivate()
	{
		Z.editor.propsToColour(_data.properties, _button.getColor());
	}

	@Override
	public boolean testPointInside(float x, float y)
	{
		Game.workingVector2b.set(x, y);
		Z.renderer.worldToScreen(Game.workingVector2b);
		return _button.hit(Game.workingVector2b.x - _button.getX(), Game.workingVector2b.y - _button.getY(), true) != null;
	}
	
	public void onDelete()
	{
        _button.remove();
	}

	@Override
	public IControl copy()
	{
		return new ControlPoint(_data.clone());
	}

}
