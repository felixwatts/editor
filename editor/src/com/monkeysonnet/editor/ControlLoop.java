package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Shape;

public class ControlLoop extends ControlShape implements IResizable
{		
	private static final float MIN_SIZE = 0.25f;
	
	public ControlLoop()
	{
		super(new Shape());
		_data.type = Shape.TYPE_LOOP;

		addVertex(-1, -1);
		addVertex(1, -1);
		addVertex(1, 1);
		addVertex(-1, 1);
		refreshEdges();
	}
	
	public ControlLoop(Shape data)
	{
		super(data);
	}
	
//	public Sprite getIcon()
//	{
//		return _icon;
//	}
	
//	
//	public void setType(int type)
//	{
//		_type = type;
//		
//		if(_map.isEditMode())
//		{
//			if(_icon != null)
//			{
//				_map.removePhysicalPatch(_icon);
//				_icon = null;
//			}
//			
//			switch(type)
//			{
//				case TYPE_GOAL_BLUE:
//					_icon = new Sprite(Textures.get("button-editor-blue"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);				
//					break;
//				case TYPE_GOAL_RED:
//					_icon = new Sprite(Textures.get("button-editor-red"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);
//					break;
//				case TYPE_GOAL_YELLOW:
//					_icon = new Sprite(Textures.get("button-editor-yellow"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);
//					break;
//				case TYPE_GOAL_GREEN:
//					_icon = new Sprite(Textures.get("button-editor-green"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);
//					break;
//				case TYPE_GOAL_CYAN:
//					_icon = new Sprite(Textures.get("button-editor-cyan"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);
//					break;
//				case TYPE_GOAL_MAGENTA:
//					_icon = new Sprite(Textures.get("button-editor-magenta"));
//					_icon.setSize(ICON_SIZE, ICON_SIZE);
//					refreshIcon();
//					_map.addPhysicalPatch(_icon);
//					break;
//			}
//		}
//	}
	
	public Vector2 getCentre()
	{
		return Game.workingVector2a.set(
				getX() + ((getRight() - getLeft()) / 2f),
				getY() + ((getTop() - getBottom()) / 2f));
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
		
		Sprite s = new Sprite();
		s.setRegion(Z.textures.get("solid"));
		_edgeSprites.add(s);
		
		//refreshIcon();
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
					_verts.insert(n, result);
					i = n;
					break;
				}
			}
		}
		
		Sprite s = new Sprite();
		s.setRegion(Z.textures.get("solid"));
		s.setColor(Color.YELLOW);

		_edgeSprites.insert(i, s);
		
		refreshEdge(wrap(i-1, _verts.size));
		refreshEdge(wrap(i, _verts.size));
		refreshEdge(wrap(i+1, _verts.size));

		//refreshIcon();

		return result;
	}
	
	private int wrap(int n, int wrap)
	{
		n = n % wrap;
		if(n < 0)
			n+= wrap;
		return n;
	}
	
	public void removeVertex(Vector2 v)
	{
		if(_verts.size > 3)
		{
			for(int n = 0; n < _verts.size; n++)
			{
				if(v == _verts.get(n))
				{
					_verts.removeIndex(n);
					
					_edgeSprites.removeIndex(n);	
					refreshEdge(n-1);	

					break;
				}
			}
			
			//refreshIcon();
		}
	}
	
//	private void refreshIcon()
//	{
//		if(_map.isEditMode())
//		{
//			if(_icon != null)
//			{
//				Vector2 centre = getCentre();
//				_icon.setPosition(centre.x - (_icon.getWidth()/2f), centre.y - (_icon.getHeight()/2f));
//			}
//		}
//	}
	
	@Override
	protected void refreshEdge(int fromVertex)
	{
		fromVertex = wrap(fromVertex, _verts.size);
		int toVertex = wrap(fromVertex+1, _verts.size);
		
		refreshEdge(_edgeSprites.get(fromVertex), _verts.get(fromVertex), _verts.get(toVertex));
	}

	@Override
	public void setLeft(float left)
	{		
		float oldLeft = getLeft();
		if(oldLeft != left)
		{	
			if(left == getRight())
				left = getRight() - MIN_SIZE;
			
			float right = getRight();		
			float oldWidth = right - oldLeft;
			float newWidth = right - left;

			for(Vector2 v : _verts)
			{
				float relPos = 1 - ((v.x-oldLeft) / oldWidth);
				v.x = right - (newWidth * relPos);
			}
			
			refreshEdges();
			//refreshIcon();
		}
	}

	@Override
	public void setRight(float right)
	{
		float oldRight = getRight();
		if(oldRight != right)
		{
			if(right == getLeft())
				right = getLeft() + MIN_SIZE;
			
			float left = getLeft();		
			float oldWidth = oldRight - left;
			float newWidth = right - left;

			for(Vector2 v : _verts)
			{
				float relPos = (v.x-left) / oldWidth;
				v.x = left + (newWidth * relPos);
			}
			
			refreshEdges();
			//refreshIcon();
		}
	}

	@Override
	public float getTop()
	{
		float y = Float.NEGATIVE_INFINITY;
		for(Vector2 v : _verts)
		{
			if(v.y > y)
				y = v.y;
		}	
		return y;
	}
	
	@Override
	public float getRight()
	{
		float x = Float.NEGATIVE_INFINITY;
		for(Vector2 v : _verts)
		{
			if(v.x > x)
				x = v.x;
		}		
		return x;
	}

	@Override
	public float getBottom()
	{
		return getY();
	}
	
	@Override
	public float getLeft()
	{
		return getX();
	}

	@Override
	public void setTop(float top)
	{
		float oldTop = getTop();
		if(oldTop != top)
		{
			if(top == getBottom())
				top = getBottom() + MIN_SIZE;
			
			float bottom = getBottom();		
			float oldHeight = oldTop - bottom;
			float newHeight = top - bottom;

			for(Vector2 v : _verts)
			{
				float relPos = (v.y-bottom) / oldHeight;
				v.y = bottom + (newHeight * relPos);
			}
			
			refreshEdges();
			//refreshIcon();
		}
	}

	@Override
	public void setBottom(float bottom)
	{
		float oldBottom = getBottom();
		if(oldBottom != bottom)
		{
			if(bottom == getTop())
				bottom = getTop() - MIN_SIZE;
			
			float top = getTop();		
			float oldHeight = top - oldBottom;
			float newHeight = top - bottom;

			for(Vector2 v : _verts)
			{
				float relPos = 1 - ((v.y-oldBottom) / oldHeight);
				v.y = top - (newHeight * relPos);
			}
			
			refreshEdges();
			//refreshIcon();
		}
	}

	@Override
	public boolean testPointInside(float x, float y)
	{
		return isPointInPolygon(_verts, Game.workingVector2a.set(x,y));
	}
	
	private static boolean isPointInPolygon (Array<Vector2> polygon, Vector2 point) 
	{
		int j = polygon.size - 1;
		boolean oddNodes = false;
		for (int i = 0; i < polygon.size; i++) {
			if ((polygon.get(i).y < point.y && polygon.get(j).y >= point.y)
				|| (polygon.get(j).y < point.y && polygon.get(i).y >= point.y)) {
				if (polygon.get(i).x + (point.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y)
					* (polygon.get(j).x - polygon.get(i).x) < point.x) {
					oddNodes = !oddNodes;
				}
			}
			j = i;
		}

		return oddNodes;
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
				refreshEdge(n-1);
				refreshEdge(n);
			}
		}

		//refreshIcon();
	}
}
