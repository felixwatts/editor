package com.monkeysonnet.editor;

import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IDialogResultHandler;
import com.monkeysonnet.engine.WorldButton;
import com.monkeysonnet.engine.editor.Graphic;
import com.monkeysonnet.engine.editor.Map;

public class ToolGraphic implements ITool, IDialogResultHandler
{
	private boolean _isBg;
	
	private ButtonActor 
		_btnNew, 
		_btnDelete, 
		_btnForward, 
		_btnBackward, 
		_btnTexture, 
		_btnFlipH, 
		_btnFlipV,
		_btnTileBig,
		_btnTileSmall,
		_btnCopy,
		_btnPaste, 
		_btnLabel,
		_btnProps;
	
	private TextureDialog _dlgTexture;
	private FlowGroup _rightPanel;
	private Graphic _copiedData;
	private WorldButton _btnRotate, _btnOrigin;
	private final Vector2 _vertexDragStart = new Vector2();
	
	private final Hashtable<ButtonActor, Graphic> _brushes = new Hashtable<ButtonActor, Graphic>();
	
	public ToolGraphic(boolean isBg)
	{
		_isBg = isBg;		
		_dlgTexture = new TextureDialog(this);
		_btnRotate = new WorldButton(
				0, 
				0, 
				EditorScreen.RESIZE_BUTTON_SIZE, 
				EditorScreen.RESIZE_BUTTON_SIZE, 
				false, 
				false, 
				0, 
				Z.textures.get("solid"), 
				this, 
				Z.renderer);
		_btnRotate.color.set(Color.MAGENTA);
		_btnRotate.visible = _btnRotate.touchable = false;
		
		_btnOrigin = new WorldButton(
				0, 
				0, 
				EditorScreen.RESIZE_BUTTON_SIZE*2, 
				EditorScreen.RESIZE_BUTTON_SIZE*2, 
				false, 
				false, 
				0, 
				Z.textures.get("point-marker"), 
				this, 
				Z.renderer);
		_btnOrigin.color.set(Color.MAGENTA);
		_btnOrigin.visible = _btnRotate.touchable = false;
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		if(sender == _btnForward)
		{
			moveItemForward();
			return true;
		}
		else if(sender == _btnBackward)
		{
			moveItemBackward();
			return true;
		}
		else if(sender == _btnNew)
		{
			ControlGraphic item = new ControlGraphic();
			
			Vector2 c = Z.editor.screenCentreWorld();
			item.setX(Z.editor.snap(c.x-2));
			item.setY(Z.editor.snap(c.y-2));
			item.setRight(item.getX()+4);
			item.setTop(item.getY()+4);
			
			if(_isBg)
				Z.map.gfxBg.add(item);
			else
				Z.map.gfxFg.add(item);
			
			Z.editor.activateItem(item);
			Z.map.changed();
		}
		else if(sender == _btnDelete)
		{
			ControlGraphic toDelete = (ControlGraphic)Z.editor.getActiveItem();
			if(_isBg)
				Z.map.gfxBg.removeValue(toDelete, true);
			else
				Z.map.gfxFg.removeValue(toDelete, true);
			
			Z.map.changed();
		}
		else if(sender == _btnTexture)
		{			
			Z.screens.push(_dlgTexture);
			return true;
		}
		else if(sender == _btnFlipH)
		{
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.flipX();
			Z.map.changed();
			return true;
		}
		else if(sender == _btnFlipV)
		{
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.flipY();
			Z.map.changed();
			return true;
		}
		else if(sender == _btnTileBig)
		{
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.increaseTileSize();
			Z.map.changed();
			return true;
		}
		else if(sender == _btnTileSmall)
		{
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.decreaseTileSize();
			Z.map.changed();
			return true;
		}
		else if(sender == _btnCopy)
		{
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			_copiedData = item.data().clone();
			EditorScreen.enableButtons(true, _btnPaste);
			return true;
		}
		else if(sender == _btnPaste)
		{
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			
			ControlGraphic pasted = new ControlGraphic(_copiedData.clone());	
			
			float w = pasted.getRight() - pasted.getLeft();
			float h = pasted.getTop() - pasted.getBottom();
			
			pasted.setX(cx - (w/2f));
			pasted.setY(cy - (h/2f));	
			
			if(_isBg)
				Z.map.gfxBg.add(pasted);
			else
				Z.map.gfxFg.add(pasted);
			
			Z.editor.activateItem(pasted);
			
			Z.map.changed();
			
			return true;
		}
		else if(sender == _btnRotate)
		{
			_vertexDragStart.set(((ControlGraphic)Z.editor.getActiveItem()).getTopRightCornerLoc());
			return true;
		}			
		else if(sender == _btnOrigin)
		{
			_vertexDragStart.set(((ControlGraphic)Z.editor.getActiveItem()).getOrigin());
			return true;
		}
		else if(sender == _btnLabel)
			return true;
		else if(sender == _btnProps)
		{
			return true;
		}
		else if(_brushes.containsKey(sender))
		{
			Graphic g = _brushes.get(sender);
			
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			
			ControlGraphic pasted = new ControlGraphic(g.clone());	
			
			float w = pasted.getRight() - pasted.getLeft();
			float h = pasted.getTop() - pasted.getBottom();
			
			pasted.setX(cx - (w/2f));
			pasted.setY(cy - (h/2f));	
			
			if(_isBg)
				Z.map.gfxBg.add(pasted);
			else
				Z.map.gfxFg.add(pasted);
			
			Z.editor.activateItem(pasted);
			
			Z.map.changed();
			
			return true;
		}
		return false;
	}
	
	@Override
	public void onActiveItemChanged()
	{
		EditorScreen.enableButtons(Z.editor.getActiveItem() != null,
				_btnDelete,
				_btnForward,
				_btnBackward,
				_btnTexture,
				_btnTileBig,
				_btnTileSmall,
				_btnFlipH,
				_btnFlipV,
				_btnCopy,
				_btnLabel,
				_btnProps);
		
		ControlGraphic g = (ControlGraphic)Z.editor.getActiveItem();
		if(g != null)
		{
			EditorScreen.enableButtons(g.canTile(), _btnTileBig, _btnTileSmall);
		}
		
		refreshRotateButton();
		refreshOriginButton();
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender == _btnLabel)
		{
			Z.editor.showLabelDialog();
		}
		if(sender == _btnProps)
		{
			Z.editor.showPropsDialog();
		}
		else if(sender == _btnRotate || sender == _btnOrigin)
		{
			refreshRotateButton();
			refreshOriginButton();
			Z.editor.initResizeButtons();
		}
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
		if(sender == _btnRotate)
		{
			Game.workingVector2a.set(delta.x, -delta.y);
			Z.renderer.screenToWorld(Game.workingVector2a);
			Z.renderer.screenToWorld(Game.workingVector2b.set(0, 0));				
			Game.workingVector2a.sub(Game.workingVector2b).add(_vertexDragStart);

			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.setTopRightCornerLoc(Z.editor.snap(Game.workingVector2a.x), Z.editor.snap(Game.workingVector2a.y));
			Z.map.changed();
		}
		else if(sender == _btnOrigin)
		{
			Game.workingVector2a.set(delta.x, -delta.y);
			Z.renderer.screenToWorld(Game.workingVector2a);
			Z.renderer.screenToWorld(Game.workingVector2b.set(0, 0));				
			Game.workingVector2a.sub(Game.workingVector2b).add(_vertexDragStart);

			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.setOrigin(Z.editor.snap(Game.workingVector2a.x), Z.editor.snap(Game.workingVector2a.y));
			Z.map.changed();
		}
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
		Array<ControlGraphic> gfx = _isBg ? Z.map.gfxBg : Z.map.gfxFg;
		
		gfx.removeValue((ControlGraphic)Z.editor.getActiveItem(), true);
		Z.editor.activateItem(null);
		Z.map.changed();
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
		ControlGraphic item = getItemAtScreenLocation(x, y);
		if(item != Z.editor.getActiveItem())
			Z.editor.activateItem(null);
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		ControlGraphic item = getItemAtScreenLocation(x, y);
		Z.editor.activateItem(item);
		return false;
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
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-delete"), 
				this);
		_btnForward = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-move-fwd"), 
				this);
		_btnBackward = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-move-back"), 
				this);	
		_btnTexture = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-texture"), 
				this);		
		_btnFlipH = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-flip-h"), 
				this);	
		_btnFlipV = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-flip-v"), 
				this);	
		_btnTileBig = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-tile-big"), 
				this);	
		_btnTileSmall = new ButtonActor(
				0, 0, 
				EditorScreen.BUTTON_WIDTH, 
				EditorScreen.BUTTON_HEIGHT, 
				Z.textures.get("button-editor-tile-small"), 
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
		
		_rightPanel.addActor(_btnForward);
		_rightPanel.addActor(_btnBackward);
		_rightPanel.addActor(_btnFlipH);
		_rightPanel.addActor(_btnFlipV);		
		_rightPanel.addActor(_btnTileBig);
		_rightPanel.addActor(_btnTileSmall);		
		_rightPanel.addActor(_btnTexture);
		_rightPanel.addBreak();
		_rightPanel.addActor(_btnLabel);
		_rightPanel.addActor(_btnProps);
		_rightPanel.addActor(_btnCopy);
		_rightPanel.addActor(_btnPaste);
		_rightPanel.addActor(_btnNew);
		_rightPanel.addActor(_btnDelete);		
		
		_rightPanel.x = Z.editor.getStage().width() - _rightPanel.width;
		_rightPanel.y = Z.editor.getStage().height() - _rightPanel.height;
		Z.editor.getStage().addActor(_rightPanel);
		
		Z.editor.getStage().addActor(_btnRotate);
		Z.editor.getStage().addActor(_btnOrigin);
	}
	
	private ControlGraphic getItemAtScreenLocation(float x, float y)
	{
		Z.renderer.screenToWorld(Game.workingVector2a.set(x, y));
		
		Array<ControlGraphic> items = getItems();
		for(int n = items.size-1; n >= 0; n--)
		{
			ControlGraphic item = items.get(n);
			if(item.testPointInside(Game.workingVector2a.x, Game.workingVector2a.y))
			{
				return item;
			}
		}

		return null;
	}

	@Override
	public void activate()
	{
		for(ControlGraphic p : getItems())
		{
			p.focus();
		}
		
		_rightPanel.visible = _rightPanel.touchable = true;
		
		_copiedData = null;
		EditorScreen.enableButtons(false, _btnPaste);
		
		onActiveItemChanged();
	}

	@Override
	public void deactivate()
	{
		Z.editor.activateItem(null);
		
		for(ControlGraphic p : getItems())
		{
			p.blur();
		}
		
		_rightPanel.visible = _rightPanel.touchable = false;
	}
	
	private Array<ControlGraphic> getItems()
	{
		return _isBg ? Z.map.gfxBg : Z.map.gfxFg;
	}
	
	private void moveItemForward()
	{
		ControlGraphic activeItem = (ControlGraphic)Z.editor.getActiveItem();
		if(activeItem == null)
			return;
		
		Z.map.shiftGraphic(activeItem, !_isBg, true);
		Z.map.changed();
	}
	
	private void moveItemBackward()
	{
		ControlGraphic activeItem = (ControlGraphic)Z.editor.getActiveItem();
		if(activeItem == null)
			return;
		
		Z.map.shiftGraphic(activeItem, !_isBg, false);
		Z.map.changed();
	}

	@Override
	public void onDialogResult(Object sender)
	{
		if(sender == _dlgTexture && _dlgTexture.getSelectedTexture() != null)
		{			
			ControlGraphic item = (ControlGraphic)Z.editor.getActiveItem();
			item.setTexture(_dlgTexture.getSelectedTexture(), _dlgTexture.getSelectedTexture().name);
			EditorScreen.enableButtons(item.canTile(), _btnTileBig, _btnTileSmall);
			Z.map.changed();
		}
	}
	
	private void refreshRotateButton()
	{
		if(Z.editor.getActiveItem() == null)
		{
			_btnRotate.visible = _btnRotate.touchable = false;
		}
		else
		{
			Vector2 c = ((ControlGraphic)Z.editor.getActiveItem()).getTopRightCornerLoc();
			_btnRotate.setWorldLocation(c.x, c.y);			
			_btnRotate.visible = _btnRotate.touchable = true;
		}
	}
	
	private void refreshOriginButton()
	{
		if(Z.editor.getActiveItem() == null)
		{
			_btnOrigin.visible = _btnOrigin.touchable = false;
		}
		else
		{
			Vector2 c = ((ControlGraphic)Z.editor.getActiveItem()).getOrigin();
			_btnOrigin.setWorldLocation(c.x, c.y);			
			_btnOrigin.visible = _btnOrigin.touchable = true;
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
				
				if(_isBg)
				{
					if(map.gfxBg() != null && map.gfxBg().length > 0)
					{
						addBrush(map.gfxBg()[0], brushFile.nameWithoutExtension());
					}
				}
				else
				{
					if(map.gfxFg() != null && map.gfxFg().length > 0)
					{
						addBrush(map.gfxFg()[0], brushFile.nameWithoutExtension());
					}
				}
			}
		}
	}

	public void addBrush(Graphic g, String name)
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
		if(Z.editor.getActiveItem() != null)
		{
			ControlGraphic copy = (ControlGraphic)((ControlGraphic)Z.editor.getActiveItem()).copy();
			if(_isBg)
				Z.map.gfxBg.add(copy);
			else
				Z.map.gfxFg.add(copy);
			
			return copy;
		}
		else return null;
	}
}
