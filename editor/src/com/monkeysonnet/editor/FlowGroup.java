package com.monkeysonnet.editor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

public class FlowGroup extends Group
{
	private float _padding, _nextX, _nextY, _rowY, _paddingRight;	
	
	public FlowGroup(float width, float padding, float paddingRight)
	{
		this.width = width;
		this.height = _padding;
		_padding = padding;
		_nextX = _padding;
		_nextY = _padding;
		_paddingRight = paddingRight;
	}
	
	@Override 
	public void addActor(Actor a)
	{
		a.x = _nextX;
		a.y = _nextY;
		super.addActor(a);
		
		_rowY = Math.max(_rowY, a.height);
		this.height = _nextY + _rowY + _padding;
		
		_nextX += a.width + _padding;
		if(_nextX >= (this.width-_paddingRight))
		{
			_nextX = _padding;
			_nextY += _rowY + _padding;
			_rowY = 0;
		}
		
//		a.x = _nextX;
//		a.y = _padding;
//		super.addActor(a);
//		
//		_rowY = Math.max(_rowY, a.height);
//		this.height = _nextY + _rowY + _padding;
//		
//		_nextX += a.width + _padding;
//		if(_nextX >= (this.width-_paddingRight))
//		{
//			_nextX = _padding;
//			_nextY += _rowY + _padding;
//			
//			for(Actor b : getActors())
//			{
//				if(b != a)
//					b.y += _rowY + _padding;
//			}
//			
//			_rowY = 0;
//		}
	}
	
	public void addBreak()
	{
		_nextX = _padding;
		_nextY += _rowY + _padding;
		_rowY = 0;
	}
}
