package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Shape;

public abstract class ControlShape extends Control implements IShapeControl, ICopyableControl
{
	private static final float EDGE_WIDTH = 0.125f;
	
	protected Shape _data;
	protected final Color _edgeColor = new Color(Color.MAGENTA);
	protected final Array<Vector2> _verts = new Array<Vector2>();
	protected final Array<Sprite> _edgeSprites = new Array<Sprite>();
	
	public static ControlShape create(Shape data)
	{
		switch(data.type)
		{
			case Shape.TYPE_CHAIN:
				return new ControlChain(data);
			case Shape.TYPE_LOOP:
			default:
					return new ControlLoop(data);
		}
	}
	
	protected ControlShape(Shape data)
	{
		_data = data;
		
		if(_data.shape != null)
			for(Vector2 v : _data.shape)
				addVertex(v.x, v.y);
		
		Z.editor.propsToColour(_data.properties, _edgeColor);

		refreshEdges();
		label(_data.label);
		properties(_data.properties);
		
		onDeactivate();
	}
	
	@Override
	public Shape data()
	{
		return _data;
	}

	@Override
	public boolean canEditShape()
	{
		return true;
	}
	
	@Override
	public void properties(String val)
	{
		_data.properties = val;
		Z.editor.propsToColour(val, _edgeColor);
		refreshEdges();
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
	public String properties()
	{
		return _data.properties;
	}

	@Override
	public IControl copy()
	{
		updateShapeData();
		ControlShape copy = ControlShape.create(_data.clone());
		return copy;
	}
	
	@Override
	public void onActivate()
	{
		for(Sprite s : _edgeSprites)
		{
			s.setColor(Color.YELLOW);
		}
	}

	@Override
	public void onDeactivate()
	{
		for(Sprite s : _edgeSprites)
		{
			s.setColor(_edgeColor);
		}
	}
	
	@Override
	public float getX()
	{
		float x = Float.POSITIVE_INFINITY;
		for(Vector2 v : _verts)
		{
			if(v.x < x)
				x = v.x;
		}		
		return x;
	}

	@Override
	public float getY()
	{
		float y = Float.POSITIVE_INFINITY;
		for(Vector2 v : _verts)
		{
			if(v.y < y)
				y = v.y;
		}		
		return y;
	}

	@Override
	public Iterable<Vector2> getVertices()
	{
		return _verts;
	}

	@Override
	public void setX(float x)
	{
		if(x != getX())
		{
			float dx = x - getX();
			for(Vector2 v : _verts)
			{
				v.x += dx;
			}
			refreshEdges();
		}
	}

	@Override
	public void setY(float y)
	{
		if(y != getY())
		{
			float dy = y - getY();
			for(Vector2 v : _verts)
			{
				v.y += dy;
			}
			refreshEdges();
		}
	}
	
	@Override
	public Iterable<Sprite> getEdgeSprites()
	{
		return _edgeSprites;
	}
	
	public abstract Vector2 addVertex(float x, float y, Vector2 after);
	public abstract void removeVertex(Vector2 v);
	public abstract void moveVertex(Vector2 v, float newX, float newY);
	
	protected abstract void addVertex(float x, float y);
	
	protected void refreshEdges()
	{
		for(int n = 0; n < _verts.size; n++)
			refreshEdge(n);
	}
	
	protected abstract void refreshEdge(int n);
	
	protected void refreshEdge(Sprite s, Vector2 start, Vector2 end)
	{
		s.setBounds(start.x, start.y - (EDGE_WIDTH/2f), start.dst(end), EDGE_WIDTH);
		s.setOrigin(0, (EDGE_WIDTH/2f));
		s.setRotation(Game.workingVector2a.set(end).sub(start).angle());	
		s.setColor(Z.editor.getActiveItem() == this ? Color.YELLOW : _edgeColor);
	}
	
	public void updateShapeData()
	{
		_data.shape = new Vector2[_verts.size];
		for(int n = 0; n < _verts.size; n++)
			_data.shape[n] = new Vector2(_verts.get(n));
	}
}
