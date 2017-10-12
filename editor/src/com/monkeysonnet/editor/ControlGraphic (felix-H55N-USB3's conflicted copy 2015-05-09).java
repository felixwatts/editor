package com.monkeysonnet.editor;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Graphic;

public class ControlGraphic extends Control implements IControl, IMovable, IResizable, ICopyableControl
{
	private static final float DEFAULT_PIXELS_PER_UNIT = 128;
	
	private Graphic _data;
	private Sprite _sprite;
	private int _originalRegionWidth;
	private int _originalRegionHeight;
	
	private static final Color _workingColor = new Color();

	public ControlGraphic()
	{
		_data = new Graphic();
		_sprite = new Sprite();
		setTexture(Z.textures.get("solid"), "solid");
	}
	
	public ControlGraphic(Graphic data)
	{
		_data = data;
		_sprite = _data.toSprite(Z.textures);
		_originalRegionHeight = Z.textures.get(_data.textureName).getRegionHeight();
		_originalRegionWidth = Z.textures.get(_data.textureName).getRegionWidth();
		refreshLabelCache();
	}
	
	public Graphic data()
	{
		return _data;
	}

	@Override
	public float getX()
	{
		return _data.left;
	}
	
	@Override
	public float getY()
	{
		return _data.bottom;
	}

	@Override
	public float getLeft()
	{
		return _data.left;
	}

	@Override
	public float getRight()
	{
		return _data.right;
	}

	@Override
	public void setLeft(float left)
	{
		_data.left = left;
		
		_sprite.setSize(getRight() - left, _sprite.getHeight());
		_sprite.setX(left);		
		refreshRegion();
	}

	@Override
	public void setRight(float right)
	{
		_data.right = right;
		
		_sprite.setSize(right - getLeft(), _sprite.getHeight());
		refreshRegion();
	}

	@Override
	public float getTop()
	{
		return _data.top;
	}

	@Override
	public float getBottom()
	{
		return _data.bottom;
	}

	@Override
	public void setTop(float top)
	{
		_data.top = top;
		
		_sprite.setSize(_sprite.getWidth(), top - _sprite.getY());
		_sprite.setOrigin(0,  _sprite.getHeight());
		refreshRegion();
	}

	@Override
	public void setBottom(float bottom)
	{
		_data.bottom = bottom;
		
		_sprite.setSize(_sprite.getWidth(), getTop() - bottom);
		_sprite.setOrigin(0,  _sprite.getHeight());
		_sprite.setY(bottom);
		refreshRegion();
	}

	@Override
	public void setX(float x)
	{
		_data.right = (_data.right - _data.left) + x;
		_data.left = x;		
		_sprite.setX(x);
	}

	@Override
	public void setY(float y)
	{
		_data.top = (_data.top - _data.bottom) + y;
		_data.bottom = y;		
		_sprite.setY(y);
	}

	@Override
	public void onActivate()
	{
		_sprite.setColor(Color.YELLOW);
	}

	@Override
	public void onDeactivate()
	{
		refreshColour();
	}
	
	private void refreshColour()
	{
		Z.editor.propsToColour(_data.properties, _workingColor);
		_sprite.setColor(_workingColor);//  Color.WHITE);
	}

	@Override
	public boolean testPointInside(float x, float y)
	{
		float[] v = _sprite.getVertices();
		List<Vector2> verts = new ArrayList<Vector2>();
		verts.add(new Vector2(v[SpriteBatch.X1], v[SpriteBatch.Y1]));
		verts.add(new Vector2(v[SpriteBatch.X2], v[SpriteBatch.Y2]));
		verts.add(new Vector2(v[SpriteBatch.X3], v[SpriteBatch.Y3]));
		verts.add(new Vector2(v[SpriteBatch.X4], v[SpriteBatch.Y4]));
		return Intersector.isPointInPolygon(verts, Game.workingVector2a.set(x, y));
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
//		Color c = new Color();
//		Z.editor.propsToColour(props, c);
//		_sprite.setColor(c);
	}

	@Override
	public String properties()
	{
		return _data.properties;
	}
	
	public void setTexture(TextureRegion tex, String name)
	{
		if(tex != null)
		{
			_data.flipX = false;			
			_data.textureName = name;
			_sprite.setRegion(tex);
						
			// todo handle tiling in one dimension.
			if(canTile())
			{
				// tileable
				
				_originalRegionWidth = tex.getRegionWidth();
				_originalRegionHeight = tex.getRegionHeight();
				
				_data.tileSize = (int) (DEFAULT_PIXELS_PER_UNIT / tex.getRegionWidth());
				if(_data.tileSize > 2f)
					_data.tileSize = 2f;
				if(_data.tileSize < 1f)
					_data.tileSize = 1f;
				
				refreshRegion();
			}
			else _data.tileSize = -1;
		}
	}
	
	public void increaseTileSize()
	{
		if(canTile() && _data.tileSize > 0.125f)
		{
			if(_data.tileSize > 1f)
				_data.tileSize --;
			else
				_data.tileSize /= 2f;
			
			refreshRegion();
		}
	}
	
	public void decreaseTileSize()
	{
		if(canTile() && _data.tileSize < 64)
		{
			if(_data.tileSize < 1f)
				_data.tileSize *= 2f;
			else
				_data.tileSize ++;
			
			refreshRegion();
		}
	}
	
	public boolean canTile()
	{
		return _sprite.getTexture().getUWrap() == TextureWrap.Repeat 
				|| _sprite.getTexture().getVWrap() == TextureWrap.Repeat;
	}
	
	public void flipX()
	{
		_data.flipX = !_data.flipX;
		_sprite.flip(true, false);
	}
	
	public void flipY()
	{
		_data.flipY = !_data.flipY;
		_sprite.flip(false, true);
	}
	
	public void setTopRightCornerLoc(float x, float y)
	{
		Game.workingVector2a.set(x, y).sub(getLeft(), getTop());
		setRight(getLeft() + Game.workingVector2a.len());
		_data.rotation = Game.workingVector2a.angle();
		_sprite.setRotation(_data.rotation);
	}
	
	public Vector2 getTopRightCornerLoc()
	{
		Game.workingVector2a.set(getRight() - getLeft(), 0).rotate(_sprite.getRotation()).add(getLeft(), getTop());
		return Game.workingVector2a;
	}
	
	public Vector2 getOrigin()
	{
		return Game.workingVector2a.set(_sprite.getOriginX() + getLeft(), _sprite.getOriginY() + getBottom());
	}
	
	public void setOrigin(float x, float y)
	{
		_sprite.setOrigin(x - getLeft(), y - getBottom());
	}
	
	public void blur()
	{		
		refreshColour();
		_sprite.setColor(_sprite.getColor().r, _sprite.getColor().r, _sprite.getColor().r, 0.5f);
	}
	
	public void focus()
	{
		refreshColour();
	}
	
	private void refreshRegion()
	{
		if(canTile())
		{
			_sprite.setRegion(
					(int)_sprite.getRegionX(), 
					(int)_sprite.getRegionY(),
					(int)((_sprite.getWidth() / _data.tileSize) * _originalRegionWidth), 
					(int)((_sprite.getHeight() / _data.tileSize * _originalRegionHeight)) * (_originalRegionWidth / _originalRegionHeight));
		}
	}

	public Sprite sprite()
	{
		return _sprite;
	}

	@Override
	public IControl copy()
	{
		return new ControlGraphic(data().clone());
	}
}
