package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Shape;

public class ControlChain extends ControlShape
{
	private static final float CLICK_RANGE = 0.25f;

	public ControlChain()
	{
		super(new Shape());
		_data.type = Shape.TYPE_CHAIN;

		addVertex(-1, 0);
		addVertex(1, 0);
		refreshEdges();
	}
	
	public ControlChain(Shape data)
	{
		super(data);
	}
	
	public Vector2 getFallbackVertex(Vector2 v)
	{
		int i = _verts.indexOf(v, true);
		if(i == 0)
			return _verts.get(_verts.size-1);
		else return _verts.get(i-1);
	}
	
	@Override
	protected void addVertex(float x, float y)
	{
		Vector2 v = new Vector2(x, y);
		_verts.add(v);
		
		if(_verts.size > 1)
		{
			Sprite s = new Sprite();
			s.setRegion(Z.textures.get("solid"));
			_edgeSprites.add(s);
		}
	}
	
	public Vector2 addVertex(float x, float y, Vector2 after)
	{
		Vector2 result = new Vector2(x, y);
		int i = -1;
		if(after == null)
		{
			_verts.add(result);	
			i = _verts.size - 1;
		}
		else
		{
			for(int n = 0; n < _verts.size; n++)
			{
				if(after == _verts.get(n))
				{
					if(n == _verts.size-1)
					{
						_verts.add(result);
						i = n+1;
					}
					else
					{
						_verts.insert(n, result);
						i = n;
					}
					
					break;
				}
			}
		}
		
		Sprite s = new Sprite();
		s.setRegion(Z.textures.get("solid"));
		s.setColor(Color.YELLOW);
		
		if(i == _verts.size-1)
		{
			_edgeSprites.add(s);
			refreshEdge(i-1);
		}
		else
		{
			_edgeSprites.insert(i, s);
			
			if(i > 0)
				refreshEdge(i-1);
			refreshEdge(i);
		}
	
		return result;
	}
	
	public void removeVertex(Vector2 v)
	{
		if(_verts.size > 2)
		{
			for(int n = 0; n < _verts.size; n++)
			{
				if(v == _verts.get(n))
				{
					_verts.removeIndex(n);
					
					if(n > 0)
					{
						_edgeSprites.removeIndex(n);	
						refreshEdge(n-1);	
					}
					else
					{
						_edgeSprites.removeIndex(n);
					}

					break;
				}
			}
		}
	}
	
	@Override
	protected void refreshEdge(int fromVertex)
	{
		if(fromVertex == _verts.size-1)
			return;
		
		int toVertex = fromVertex+1;
		
		refreshEdge(_edgeSprites.get(fromVertex), _verts.get(fromVertex), _verts.get(toVertex));
	}

	@Override
	public boolean testPointInside(float x, float y)
	{
		for(int n = 0; n < _verts.size - 1; n++)
		{
			float dst = Intersector.distanceLinePoint(_verts.get(n), _verts.get(n+1), Game.workingVector2a.set(x, y));
			
			if(dst < CLICK_RANGE)
				return true;
		}
		
		return false;
	}

	@Override
	public void moveVertex(Vector2 v, float newX, float newY)
	{
		for(int n = 0; n < _verts.size; n++)
		{
			if(_verts.get(n) == v)
			{
				if(v.x == newX && v.y == newY)
					return;
				
				v.set(newX, newY);
				
				if(n > 0)
					refreshEdge(n-1);
				if(n < _verts.size-1)
					refreshEdge(n);
			}
		}
	}
}
