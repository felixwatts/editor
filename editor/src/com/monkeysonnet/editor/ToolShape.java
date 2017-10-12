package com.monkeysonnet.editor;

import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;

public class ToolShape implements ITool
{
	protected ButtonActor _btnNew, _btnDelete, _btnMove, _btnDeleteVertex, _btnAddVertex, _btnCopy, _btnPaste, _btnLabel, _btnProps;
	private Array<VertexButton> _vertexButtons;	
	private Vector2 _activeVertex, _vertexDragStart = new Vector2();
	private boolean _addVertexMode, _deleteVertexMode;
	protected FlowGroup _rightPanel;
	private Shape _copied;
	private int _type;	
	
	private final Hashtable<ButtonActor, Shape> _brushes = new Hashtable<ButtonActor, Shape>();
	
	public ToolShape(int type)
	{
		_type = type;
		_vertexButtons = new Array<VertexButton>();
	}
	
	@Override
	public boolean onButtonDown(Actor sender)
	{
		if(sender == _btnNew)
		{	
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			
			IShapeControl item;
			
			switch(_type)
			{
				case Shape.TYPE_CHAIN:
				default:
					item = new ControlChain();
					item.setX(Z.editor.snap(cx-1));
					item.setY(Z.editor.snap(cy));
					break;
				case Shape.TYPE_LOOP:
					item = new ControlLoop();
					item.setX(Z.editor.snap(cx-1));
					item.setY(Z.editor.snap(cy-1));
					break;
			}
			
			
//			item.setX(Z.editor.snap(cx-2));
//			item.setY(Z.editor.snap(cy-2));
//			item.setRight(item.getX()+4);
//			item.setTop(item.getY()+4);
			
			Z.map.shapes.add(item);
			Z.editor.activateItem(item);
			
			Z.map.changed();
			
			return true;
		}
		if(sender == _btnDelete)
		{
			deleteActiveItem();
			return true;
		}
//		if(sender == _btnDeleteVertex)
//		{
//			deleteActiveVertex();
//
//			return true;
//		}
		if(sender == _btnAddVertex)
		{
			setAddVertexMode(!_addVertexMode);
			return true;
		}
		if(sender == _btnCopy)
		{
			IShapeControl item = activeShape();
			_copied = item.data().clone();
			EditorScreen.enableButtons(true, _btnPaste);
			return true;
		}
		if(sender == _btnPaste)
		{
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			
			ControlLoop pasted = new ControlLoop(_copied.clone());
			
			float w = pasted.getRight() - pasted.getLeft();
			float h = pasted.getTop() - pasted.getBottom();
			
			pasted.setX(Z.editor.snap(cx-(w/2f)));
			pasted.setY(Z.editor.snap(cy-(h/2f)));
			
			Z.map.shapes.add(pasted);
			Z.editor.activateItem(pasted);
			Z.map.changed();
			return true;
		}
		else if(sender instanceof VertexButton)
		{
			VertexButton vb = (VertexButton)sender;
			setActiveVertex(vb.getVertex());
			
			if(_deleteVertexMode)
				deleteActiveVertex();
			else
				_vertexDragStart.set(vb.getVertex());
			return true;
		}
		else if(sender == _btnLabel)
			return true;
		else if(sender == _btnProps)
			return true;
		else if(_brushes.containsKey(sender))
		{
			Shape g = _brushes.get(sender);
			
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			
			ControlShape pasted = ControlShape.create(g.clone());
			
//			float w = pasted.getRight() - pasted.getLeft();
//			float h = pasted.getTop() - pasted.getBottom();
			
			pasted.setX(cx);
			pasted.setY(cy);	
			
			Z.map.shapes.add(pasted);
			
			Z.editor.activateItem(pasted);
			
			Z.map.changed();
			
			return true;
		}
		return false;
	}
	
	private IShapeControl activeShape()
	{
		return (IShapeControl)Z.editor.getActiveItem();
	}
	
	private void deleteActiveItem()
	{
		Z.map.shapes.removeValue(activeShape(), true);
		Z.editor.activateItem(null);
		Z.map.changed();
	}
	
	private void setAddVertexMode(boolean val)
	{
		if(val)
			setDeleteVertexMode(false);
		
		_addVertexMode = val;
		if(_addVertexMode)
			_btnAddVertex.color.set(1, 0, 0, 1);
		else EditorScreen.enableButtons(activeShape() != null && activeShape().canEditShape(), _btnAddVertex);
	}
	
	private void setDeleteVertexMode(boolean val)
	{
		if(_addVertexMode)
			return;
		
		_deleteVertexMode = val;
		if(_deleteVertexMode)
			_btnDeleteVertex.color.set(1, 0, 0, 1);
		else EditorScreen.enableButtons(activeShape() != null && activeShape().canEditShape(), _btnDeleteVertex);
	}
	
	private void deleteActiveVertex()
	{
		if(_activeVertex != null)
		{
			activeShape().removeVertex(_activeVertex);			
			initVertexButtons();
			Z.editor.initResizeButtons();					
			setActiveVertex(activeShape().getVertices().iterator().next());
			Z.map.changed();
		}
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender instanceof VertexButton)
		{
			initVertexButtons();
			Z.editor.initResizeButtons();
		}
		else if(sender == _btnLabel)
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
		if(sender instanceof VertexButton)
		{
			VertexButton vb = (VertexButton)sender;
			if(_activeVertex == vb.getVertex())
			{
				Game.workingVector2a.set(delta.x, -delta.y);
				Z.renderer.screenToWorld(Game.workingVector2a);
				Z.renderer.screenToWorld(Game.workingVector2b.set(0, 0));				
				
				Game.workingVector2a.sub(Game.workingVector2b).add(_vertexDragStart);

				activeShape().moveVertex(vb.getVertex(), Z.editor.snap(Game.workingVector2a.x), Z.editor.snap(Game.workingVector2a.y));
				
				Z.map.changed();
			}
		}
	}

	@Override
	public void init()
	{
		_rightPanel = new FlowGroup(88, 8, 0);
		
		_btnNew = new ButtonActor(
				0, 
				0, 
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
		_btnAddVertex = new ButtonActor(
				0, 
				0,
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-vertex-add"),
				this);
		_btnDeleteVertex = new ButtonActor(
				0, 
				0,
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-vertex-delete"),
				this);
		_btnCopy = new ButtonActor(
				0, 
				0,
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-copy"),
				this);
		_btnPaste = new ButtonActor(
				0, 
				0,
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-paste"),
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
		
		addBrushButtons();
		
		_rightPanel.addBreak();
		_rightPanel.addBreak();
		
		_rightPanel.addActor(_btnLabel);
		_rightPanel.addActor(_btnProps);
		_rightPanel.addActor(_btnAddVertex);
		_rightPanel.addActor(_btnDeleteVertex);
		_rightPanel.addActor(_btnCopy);
		_rightPanel.addActor(_btnPaste);
		_rightPanel.addActor(_btnNew);
		_rightPanel.addActor(_btnDelete);
		
		_rightPanel.x = Z.editor.getStage().width() - _rightPanel.width;
		_rightPanel.y = Z.editor.getStage().height() - _rightPanel.height;
		
		Z.editor.getStage().addActor(_rightPanel);
	}

	@Override
	public void activate()
	{
		_rightPanel.visible = _rightPanel.touchable = true;
		
		for(IShapeControl item : Z.map.shapes)
		{
			if(item.data().type == _type)
			{
				for(Sprite s : item.getEdgeSprites())
				{
					Color c = s.getColor();
					s.setColor(c.r, c.g, c.b, 1);
				}
			}
		}
		
		_copied = null;
		EditorScreen.enableButtons(false, _btnPaste);
		
		onActiveItemChanged();
	}

	@Override
	public void deactivate()
	{
		Z.editor.activateItem(null);
		
		for(IShapeControl item : Z.map.shapes)
		{
			if(item.data().type == _type)
			{
				for(Sprite s : item.getEdgeSprites())
				{
					Color c = s.getColor();
					s.setColor(c.r, c.g, c.b, 0.3f);
				}
			}
		}
		
		_rightPanel.visible = _rightPanel.touchable = false;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Keys.FORWARD_DEL:
				deleteActiveItem();
				return true;
			case Keys.CONTROL_LEFT:
			case Keys.CONTROL_RIGHT:
				setAddVertexMode(true);
				return true;
			case Keys.ALT_LEFT:
			case Keys.ALT_RIGHT:
				setDeleteVertexMode(true);
				return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Keys.CONTROL_LEFT:
			case Keys.CONTROL_RIGHT:
				setAddVertexMode(false);
				return true;
			case Keys.ALT_LEFT:
			case Keys.ALT_RIGHT:
				setDeleteVertexMode(false);
				return true;
		}
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
		if(_addVertexMode)
		{					
			Z.renderer.screenToWorld(Game.workingVector2a.set(x, y));
			Vector2 v = activeShape().addVertex(Z.editor.snap(Game.workingVector2a.x), Z.editor.snap(Game.workingVector2a.y), _activeVertex);
			
			initVertexButtons();
			Z.editor.initResizeButtons();
			
			setActiveVertex(v);
			Z.map.changed();
			
			return true;
		}
		else
		{
			IShapeControl item = getItemAtScreenLocation(x, y);
			
			if(item != Z.editor.getActiveItem())
				Z.editor.activateItem(null);
			return false;
		}
	}
	
	protected IShapeControl getItemAtScreenLocation(float x, float y)
	{
		return getItemAtScreenLocation(_type, x, y);
		
//		Z.renderer.screenToWorld(Game.workingVector2a.set(x, y));
//		
//		for(IShapeControl item : Z.map.shapes)
//		{
//			if(
//					((item.data().type == _type) || (_type == Shape.TYPE_LOOP && item.data().type == 8)) 
//					&& item.testPointInside(Game.workingVector2a.x, Game.workingVector2a.y))
//			{
//				return item;
//			}
//		}
//
//		return null;
	}
	
	protected ControlShape getItemAtScreenLocation(int type, float x, float y)
	{
		Z.renderer.screenToWorld(Game.workingVector2a.set(x, y));
		
		Array<ControlShape> candidates = new Array<ControlShape>();
		
		for(IShapeControl item : Z.map.shapes)
		{
			if(item.data().type == _type && item.testPointInside(Game.workingVector2a.x, Game.workingVector2a.y))
			{
				candidates.add((ControlShape)item);
			}
		}
		
		if(candidates.size == 0)
			return null;
		else if(candidates.size == 1)
			return candidates.get(0);
		else
		{
			ControlShape best = null;
			float minDst2 = Float.POSITIVE_INFINITY;
			
			for(ControlShape item : candidates)
			{
				for(Vector2 p : item.getVertices())
				{
					float dst2 = p.dst2(Game.workingVector2a);
					
					if(dst2 < minDst2)
					{
						minDst2 = dst2;
						best = item;
						
						break;
					}
				}
			}
			
			return best;
		}
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(!_addVertexMode)
		{
			IShapeControl item = getItemAtScreenLocation(x, y);
			Z.editor.activateItem(item);
		}
		return _addVertexMode;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		return _addVertexMode;
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
	public void onActiveItemChanged()
	{
		setActiveVertex(null);
		
		EditorScreen.enableButtons(activeShape() != null, _btnDelete, _btnCopy, _btnLabel, _btnProps);
		EditorScreen.enableButtons(activeShape() != null && activeShape().canEditShape(), _btnAddVertex, _btnDeleteVertex);
		
		initVertexButtons();
		
		if(activeShape() != null)
			setActiveVertex(activeShape().getVertices().iterator().next());
	}
	
	private void setActiveVertex(Vector2 v)
	{
		if(v != _activeVertex)
		{		
			if(_activeVertex != null)
			{
				for(VertexButton btn : _vertexButtons)
				{
					if(btn.getVertex() == _activeVertex)
					{
						btn.color.set(Color.MAGENTA);
						break;
					}
				}
			}
			
			_activeVertex = v;
			for(VertexButton btn : _vertexButtons)
			{
				if(btn.getVertex() == _activeVertex)
				{
					btn.color.set(Color.RED);
					break;
				}
			}
						
			if(_activeVertex == null)
				setAddVertexMode(false);
			
			if(_addVertexMode)
				_btnAddVertex.color.set(1, 0, 0, 1);
		}
	}

	private void initVertexButtons()
	{
		for(Actor a : _vertexButtons)
			Z.editor.getStage().removeActor(a);
		_vertexButtons.clear();							
				
		if(activeShape() != null && activeShape().canEditShape())
		{
			for(Vector2 v : activeShape().getVertices())
			{
				VertexButton btn = new VertexButton(v, this, Z.renderer);
				_vertexButtons.add(btn);
				Z.editor.getStage().addActor(btn);
				if(v == _activeVertex)
					btn.color.set(Color.RED);
			}
		}
	}
	
	private void addBrushButtons()
	{
		FileHandle dir = Gdx.files.internal("brushes");
		if(dir.exists() && dir.isDirectory())
		{
			for(FileHandle brushFile  : dir.list("brush"))
			{
				Map map = new Map(brushFile.path());
				
				if(map.shapes() != null)
				{
					for(Shape s : map.shapes())
					{
						if(s.type == _type)
						{
							addBrush(s, brushFile.nameWithoutExtension());						
							break;
						}
					}
				}
			}
		}
	}

	public void addBrush(Shape g, String name)
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
		
		_brushes.put(btn, g);
		
		_rightPanel.addActor(btn);
	}

	@Override
	public IControl copyActiveItem()
	{
		if(activeShape() != null)
		{
			ControlShape copy = (ControlShape)activeShape().copy();
			Z.map.shapes.add(copy);
			return copy;
		}
		else return null;
	}
}
