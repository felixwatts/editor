package com.monkeysonnet.editor;

import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;

public class ToolPoint implements ITool
{
	private ButtonActor _btnNew, _btnDelete, _btnLabel, _btnProps;
	private FlowGroup _rightPanel;
	
	private final Hashtable<ButtonActor, Point> _brushes = new Hashtable<ButtonActor, Point>();

	@Override
	public boolean onButtonDown(Actor sender)
	{

		if(sender == _btnNew)
		{
			ControlPoint item = new ControlPoint();
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			item.setX(Z.editor.snap(cx));
			item.setY(Z.editor.snap(cy));
			Z.map.points.add(item);
			Z.editor.activateItem(item);
			Z.map.changed();
			
			return true;
		}
		else if(sender == _btnDelete)
		{
			deleteActiveItem();
			return true;
		}
		else if(sender == _btnLabel)
		{
			return true;
		}
		else if(sender == _btnProps)
		{
			return true;
		}
		else if(_brushes.containsKey(sender))
		{
			Point g = _brushes.get(sender);
			
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
	
			ControlPoint pasted = new ControlPoint(g.clone());	

			pasted.setX(cx);
			pasted.setY(cy);	
			
			Z.map.points.add(pasted);
			
			Z.editor.activateItem(pasted);
			
			Z.map.changed();
			
			return true;
		}
		else return false;
	}
	
	private ControlPoint activePoint()
	{
		return (ControlPoint)Z.editor.getActiveItem();
	}
	
	private ControlPoint getItemAtScreenLocation(float x, float y)
	{
		Z.renderer.screenToWorld(Game.workingVector2a.set(x, y));
		
		for(ControlPoint item : Z.map.points)
		{
			if(item.testPointInside(Game.workingVector2a.x, Game.workingVector2a.y))
			{
				return item;
			}
		}

		return null;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender == _btnLabel)
		{
			Z.editor.showLabelDialog();
		}
		else if(sender == _btnProps)
		{
			Z.editor.showPropsDialog();
		}
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Keys.FORWARD_DEL:
				deleteActiveItem();
				return true;
		}
		return false;
	}
	
	private void deleteActiveItem()
	{
		if(activePoint() != null)
		{
			activePoint().onDelete();
			Z.map.points.removeValue(activePoint(), true);
			Z.editor.activateItem(null);
			Z.map.changed();
		}
	}

	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		ControlPoint item = getItemAtScreenLocation(x, y);
		
		if(item != Z.editor.getActiveItem())
			Z.editor.activateItem(null);
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		ControlPoint item = getItemAtScreenLocation(x, y);
		Z.editor.activateItem(item);
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

	@Override
	public void init()
	{
		_rightPanel = new FlowGroup(88, 8, 0);
		
		_btnNew = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-new"), 
				this);
		_btnDelete = new ButtonActor(
				0, 
				0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-delete"),
				this);	
		_btnLabel = new ButtonActor(
				0, 
				0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-label"),
				this);
		_btnProps = new ButtonActor(
				0, 
				0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-props"),
				this);
		
		_rightPanel.clear();
		
		addBrushButtons();
		
		_rightPanel.addBreak();
		_rightPanel.addBreak();

		_rightPanel.addActor(_btnLabel);
		_rightPanel.addActor(_btnProps);
		_rightPanel.addActor(_btnNew);
		_rightPanel.addActor(_btnDelete);			
		
		_rightPanel.x = Z.editor.getStage().width() - _rightPanel.width;
		_rightPanel.y = Z.editor.getStage().height() - _rightPanel.height;
		Z.editor.getStage().addActor(_rightPanel);
		
		onActiveItemChanged();
		deactivate();
	}

	@Override
	public void activate()
	{
		_rightPanel.visible = _rightPanel.touchable = true;
	}

	@Override
	public void deactivate()
	{
		_rightPanel.visible = _rightPanel.touchable = false;	
	}

	@Override
	public void onActiveItemChanged()
	{
		EditorScreen.enableButtons(Z.editor.getActiveItem() != null, _btnDelete, _btnLabel, _btnProps);
	}
	
	private void addBrushButtons()
	{
		FileHandle dir = Gdx.files.internal("brushes");
		if(dir.exists() && dir.isDirectory())
		{
			for(FileHandle brushFile  : dir.list("brush"))
			{
				Map map = new Map(brushFile.path());
				
				if(map.points() != null)
				{
					if(map.points().length > 0)
					{
						addBrush(map, brushFile.nameWithoutExtension());
					}
				}
			}
		}
	}

	public void addBrush(Map g, String name)
	{
		Texture tex = new Texture("brushes/" + name + ".png");
		TextureRegion reg = new TextureRegion(tex);
		
		ButtonActor btn = new ButtonActor(
				0, 
				0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				reg,
				this);
		
		_brushes.put(btn, g.point(0));
		
		_rightPanel.addActor(btn);
	}

	@Override
	public IControl copyActiveItem()
	{
		if(activePoint() != null)
		{
			ControlPoint copy = (ControlPoint)activePoint().copy();
			Z.map.points.add(copy);
			return copy;
		}
		else return null;
	}
}
