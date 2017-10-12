package com.monkeysonnet.editor;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.engine.WorldButton;
import com.monkeysonnet.engine.editor.PropertyParser;
import com.monkeysonnet.engine.editor.Shape;

public class EditorScreen implements IScreen, IButtonEventHandler, InputProcessor //, IDialogResultHandler
{
	public static final float RESIZE_BUTTON_SIZE = 16;
	public static final float BUTTON_WIDTH = 32;
	public static final float BUTTON_HEIGHT = 32;
	public static final float BUTTON_PADDING = 8;
	private static final int INITIAL_GRID = 1;
	
	private Stage _stage;
	private IControl _activeItem;	
	private String _filename;
	private final JFileChooser _fc = new JFileChooser();
	
	public WorldButton _btnResizeLeft, _btnResizeRight, _btnResizeTop, _btnResizeBottom;
	private ButtonActor 
		_btnNew,
		_btnSave,
		_btnSaveAs,
		_btnOpen, 
		_btnFg, 
		_btnBg, 
		_btnZoomIn, 
		_btnZoomOut, 
		_btnGridSmall,
		_btnGridBig,
		_btnLoop,
		_btnChain,
		_btnPoint;
	
	private final Vector2 
		_dragStartWorld = new Vector2(), 
		_dragStartCamLocation = new Vector2(), 
		_dragStartScreen = new Vector2();
	
	private float _resizeStartVal;
	
	private ITool 
		_activeTool, 
		_toolBg, 
		_toolFg, 
		_toolLoop,
		_toolChain,
		_toolPoint;
	
	private FlowGroup _leftPanel;

	private StringDialog _dlgLabel;
	private StringDialog _dlgProps;
	private float _grid = INITIAL_GRID;
	private boolean _copyMode;
	
	@Override
	public void pause()
	{
	}

	@Override
	public void show()
	{
		Gdx.graphics.setContinuousRendering(false);

		_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		_fc.setAcceptAllFileFilterUsed(false);
		_fc.addChoosableFileFilter(new FileNameExtensionFilter("MonkeySonnet Map Files", "map", "v", "brush"));
		
		if(Z.map == null)
		{
			_dlgLabel = new StringDialog();
			_dlgProps = new StringDialog();
			
			Z.map = new EditableMap("test-map");
			
			_toolFg = new ToolGraphic(false);
			_toolBg = new ToolGraphic(true);
			_toolLoop = new ToolShape(Shape.TYPE_LOOP);
			_toolChain = new ToolShape(Shape.TYPE_CHAIN);
			_toolPoint = new ToolPoint();
			
			initStage();
		}	
		
		Gdx.graphics.requestRendering();
	}	
	
	public void resize(int w, int h)
	{		
		_dlgLabel.resize();
		_dlgProps.resize();
		
		initStage();
		if(_activeTool != null)
			activateTool(_activeTool);
		
		Gdx.graphics.requestRendering();
	}
	
	private void initStage()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		_leftPanel = new FlowGroup(80, 8, 0);	
		
		_toolFg.init();
		_toolBg.init();
		_toolLoop.init();
		_toolChain.init();
		_toolPoint.init();
		
		_btnResizeLeft = new WorldButton(0, 0, RESIZE_BUTTON_SIZE, RESIZE_BUTTON_SIZE, false, false, -1, Z.textures.get("solid"), this, Z.renderer);
		_btnResizeLeft.visible = _btnResizeLeft.touchable = false;
		_btnResizeLeft.color.set(Color.BLUE);
		_stage.addActor(_btnResizeLeft);
		
		_btnResizeRight = new WorldButton(0, 0, RESIZE_BUTTON_SIZE, RESIZE_BUTTON_SIZE, true, false, -1, Z.textures.get("solid"), this, Z.renderer);
		_btnResizeRight.visible = _btnResizeRight.touchable = false;
		_btnResizeRight.color.set(Color.BLUE);
		_stage.addActor(_btnResizeRight);
		
		_btnResizeTop = new WorldButton(0, 0, RESIZE_BUTTON_SIZE, RESIZE_BUTTON_SIZE, false, false, -1, Z.textures.get("solid"), this, Z.renderer);
		_btnResizeTop.visible = _btnResizeTop.touchable = false;
		_btnResizeTop.color.set(Color.BLUE);
		_stage.addActor(_btnResizeTop);
		
		_btnResizeBottom = new WorldButton(0, 0, RESIZE_BUTTON_SIZE, RESIZE_BUTTON_SIZE, false, true, -1, Z.textures.get("solid"), this, Z.renderer);
		_btnResizeBottom.visible = _btnResizeLeft.touchable = false;
		_btnResizeBottom.color.set(Color.BLUE);
		_stage.addActor(_btnResizeBottom);
		
		_btnNew = new ButtonActor((_stage.width()/2f) - (2*BUTTON_WIDTH) - (1.5f*BUTTON_PADDING), _stage.height() - BUTTON_HEIGHT - BUTTON_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-new"), this);
		_btnOpen = new ButtonActor((_stage.width()/2f) - (1*BUTTON_WIDTH) - (0.5f*BUTTON_PADDING), _stage.height() - BUTTON_HEIGHT - BUTTON_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-open"), this);
		_btnSave = new ButtonActor((_stage.width()/2f) + (0.5f*BUTTON_PADDING), _stage.height() - BUTTON_HEIGHT - BUTTON_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-save"), this);
		_btnSaveAs = new ButtonActor((_stage.width()/2f) + (1*BUTTON_WIDTH) + (1.5f*BUTTON_PADDING), _stage.height() - BUTTON_HEIGHT - BUTTON_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-save-as"), this);
		
		_btnFg = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-tool-foreground"), this);
		_btnBg = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-tool-background"), this);
		_btnZoomIn = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-zoom-in"), this);
		_btnZoomOut = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-zoom-out"), this);

		_btnGridSmall = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-grid-small"), this);
		_btnGridBig = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-grid-big"), this);
		_btnLoop = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-tool-loop"), this);
		_btnChain = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-tool-chain"), this);
		_btnPoint = new ButtonActor(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Z.textures.get("button-editor-tool-point"), this);
		
		_leftPanel.addActor(_btnGridSmall);
		_leftPanel.addActor(_btnGridBig);
		_leftPanel.addActor(_btnZoomOut);
		_leftPanel.addActor(_btnZoomIn);
		_leftPanel.addActor(_btnPoint);
		_leftPanel.addBreak();
		_leftPanel.addActor(_btnLoop);	
		_leftPanel.addActor(_btnChain);	
		_leftPanel.addActor(_btnFg);
		_leftPanel.addActor(_btnBg);
		
		_stage.addActor(_btnNew);
		_stage.addActor(_btnOpen);
		_stage.addActor(_btnSave);
		_stage.addActor(_btnSaveAs);
		
//		_leftPanel.addActor(_btnSave);
//		_leftPanel.addActor(_btnSaveAs);
//		_leftPanel.addActor(_btnNew);
//		_leftPanel.addActor(_btnOpen);

		_leftPanel.y = _stage.height() - _leftPanel.height;
		
		_stage.addActor(_leftPanel);
		
		ITool activeTool = _activeTool;
		
		_toolBg.deactivate();
		_toolFg.deactivate();
		_toolLoop.deactivate();
		_toolChain.deactivate();
		_toolPoint.deactivate();
		
		if(activeTool == null)
			activateTool(_toolLoop);
		else activateTool(activeTool);
		
		for(ControlPoint p : Z.map.points)
			p.initStage();
	}
	
	private void newMap()
	{
		if(!ensureNoUnsavedChanges())
			return;
		
		closeMap();

		Z.map = new EditableMap();
	}
	
	private void closeMap()
	{		
		for(ControlPoint p : Z.map.points)
			p.onDelete();
		Z.renderer.cam.position.set(0, 0, 0);
		activateItem(null);
		_filename = null;
		Z.map = null;		
	}
	
	private void open()
	{
		switch(_fc.showOpenDialog(null))
		{
			case JFileChooser.APPROVE_OPTION:
				try
				{
					if(!ensureNoUnsavedChanges())		
						return;
					
					closeMap();
					
					_filename = _fc.getSelectedFile().getCanonicalPath();
					Z.map = new EditableMap(_filename);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
		}
	}
	
	private void saveAs()
	{				
		switch(_fc.showSaveDialog(null))
		{
			case JFileChooser.APPROVE_OPTION:
				try
				{
					_filename = _fc.getSelectedFile().getCanonicalPath();
					Z.map.serialize(_filename);		
					JOptionPane.showMessageDialog(null, _filename + " saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
		}
	}
	
	private void save()
	{
		if(_filename == null)
			saveAs();
		else 
		{
			try
			{
				Z.map.serialize(_filename);
				JOptionPane.showMessageDialog(null, _filename + " saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean ensureNoUnsavedChanges()
	{			
		if(Z.map == null || !Z.map.hasChanges())
			return true;
		
		else 
		{
			Object[] options = {"Save", "Discard", "Cancel" };
			int n = JOptionPane.showOptionDialog(null, 
					"There are unsaved in the current map. Would you like to save the changes?",
					"Save Changes?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[0]); //default button title
			
			switch(n)
			{
				case 0:
					save();
					return true;
				case 1:
					return true;
				case 2:
				default:
					return false;
			}
		}
	}

	public void propsToColour(String props, Color colour)
	{
		if(PropertyParser.hasProperty("colour", props))
		{
			Color c = ColorTools.parse(PropertyParser.propertyValue("colour", props));
			if(c != null)
				colour.set(c);
			else hashToColor(props, colour);
		}
		else if(props == null)
			colour.set(Color.MAGENTA);
		else hashToColor(props, colour);	
	}
	
	private void hashToColor(String props, Color colour)
	{
		int hash = props.hashCode();
		float r = ((float)(hash & 255))/255f;
		float g = ((float)((hash >> 8) & 255))/255f;
		float b = ((float)((hash >> 16) & 255))/255f;
		
		colour.set(r, g, b, 1f);
	}
	
	public void showLabelDialog()
	{
		IControl item = getActiveItem();
		String s = (String)JOptionPane.showInputDialog(
                "Set item label:",
                item.label());

		if ((s != null) && (s.length() > 0)) 
		{
			item.label(s);
			Z.map.changed();
		}
	}
	
	public void showPropsDialog()
	{
		IControl item = getActiveItem();
		String s = (String)JOptionPane.showInputDialog(
                "Edit item properties:",
                item.properties());

		if ((s != null) && (s.length() > 0)) 
		{
			item.properties(s);
			Z.map.changed();
		}
	}
	
//	private void prepareForTest()
//	{
//		_inTest = true;
//		
//		for(EditorItemGraphical g : _map.getBackgroundItems())
//			g.sprite.setColor(Color.WHITE);
//				
//		for(EditorItemGraphical g : _map.getForegroundItems())
//			g.sprite.setColor(Color.WHITE);
//	}
	
//	private void restoreAfterTest()
//	{
//		Array<IEditorTool> tools = new Array<IEditorTool>();
//		
//		tools.add(_toolBalloon);
//		tools.add(_toolBg);
////		tools.add(_toolBox);
//		tools.add(_toolFg);
//		tools.add(_toolGoal);
//		tools.add(_toolSpikes);
//		tools.add(_toolWall);
//		
//		for(IEditorTool t : tools)
//		{
//			if(t == _activeTool)
//			{
//				t.activate();
//			}
//			else
//			{
//				t.deactivate();
//			}
//		}
//		
//		_inTest = false;
//	}
	
	public static void highlightButton(Actor btn, boolean highlight)
	{
		float v = highlight ? 0 : 1f;
		btn.color.set(1, v, v, 1);
	}
	
	public IControl getActiveItem()
	{
		return _activeItem;
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(this);	
		
//		if(_inTest)
//			restoreAfterTest();
	}

	@Override
	public void render()
	{
		Z.renderer.render();		
	}
	
	public void activateTool(ITool tool)
	{
		if(_activeTool != null)
		{
			_activeTool.deactivate();
		}
		
		activateItem(null);
		
		_activeTool = tool;
		_activeTool.activate();	
		_activeTool.onActiveItemChanged();
		
		for(Actor a : _leftPanel.getActors())
		{
			highlightButton(a, false);
		}
		
		if(tool == _toolBg)
			highlightButton(_btnBg, true);
		else if(tool == _toolFg)
			highlightButton(_btnFg, true);
		else if(tool == _toolLoop)
			highlightButton(_btnLoop, true);
		else if(tool == _toolChain)
			highlightButton(_btnChain, true);
		else if(tool == _toolPoint)
			highlightButton(_btnPoint, true);
	}
	
	public void activateItem(IControl item)
	{
		if(_activeItem != item)
		{
			if(_activeItem != null)
			{
				_activeItem.onDeactivate();
			}
			
			_activeItem = item;

			if(_activeItem != null)
			{
				_activeItem.onActivate();
			}
			
			initResizeButtons();			
			
			_activeTool.onActiveItemChanged();
		}
	}
	
	public static void enableButtons(boolean enabled, Actor... actors)
	{
		for(Actor a : actors)
		{
			a.touchable = enabled;
			float b = enabled ? 1f : 0.3f;
			a.color.set(b, b, b, 1);
		}		
	}
	
	public void enableResizer(boolean enable)
	{
		_btnResizeBottom.touchable = _btnResizeBottom.visible =
				_btnResizeTop.touchable = _btnResizeTop.visible =
				_btnResizeLeft.touchable = _btnResizeLeft.visible =
				_btnResizeRight.touchable = _btnResizeRight.visible = enable;
	}

	public void initResizeButtons()
	{
		if(_activeItem == null)
		{
			_btnResizeLeft.visible = _btnResizeLeft.touchable = false;
			_btnResizeRight.visible = _btnResizeRight.touchable = false;
			_btnResizeTop.visible = _btnResizeTop.touchable = false;
			_btnResizeBottom.visible = _btnResizeLeft.touchable = false;
		}
		else if(_activeItem instanceof IResizable)
		{
			IResizable rs = (IResizable) _activeItem;
			_btnResizeLeft.setWorldLocation(rs.getLeft(), (rs.getTop() + rs.getBottom()) / 2f);
			_btnResizeLeft.visible = _btnResizeLeft.touchable = true;
			
			_btnResizeRight.setWorldLocation(rs.getRight(), (rs.getTop() + rs.getBottom()) / 2f);
			_btnResizeRight.visible = _btnResizeRight.touchable = true;

			_btnResizeTop.setWorldLocation((rs.getLeft() + rs.getRight()) / 2f, rs.getTop());
			_btnResizeTop.visible = _btnResizeTop.touchable = true;

			_btnResizeBottom.setWorldLocation((rs.getLeft() + rs.getRight()) / 2f, rs.getBottom());
			_btnResizeBottom.visible = _btnResizeBottom.touchable = true;
		}
	}
	
//	private void test()
//	{
//		prepareForTest();
//		_map.serialize("test-map");	
//		LiftGame.ScreenManager.push(LiftGame.PlayScreen);
//		LiftGame.PlayScreen.init(new Map(_map));
//	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
		//_map.serialize("test-map");
		
		//_renderer.dispose();
		//_stage.dispose();
	}

	@Override
	public boolean isFullScreen()
	{
		return true;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
	
	public Stage getStage()
	{
		return _stage;
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		//Gdx.graphics.requestRendering();
		
		if(_activeTool.onButtonDown(sender))
			return true;
		else if(sender instanceof InsertBrushButton)
		{
			InsertBrushButton btn = (InsertBrushButton)sender;
			
			ControlLoop item = new ControlLoop(btn.data().clone());
			Vector2 c = Z.editor.screenCentreWorld();
			float cx = c.x;
			float cy = c.y;
			item.setX(Z.editor.snap(cx));
			item.setY(Z.editor.snap(cy));
			
			Z.map.shapes.add(item);
			
			switch(item.data().type)
			{
				case Shape.TYPE_CHAIN:
					activateTool(_toolChain);
					break;
				case Shape.TYPE_LOOP:
					activateTool(_toolLoop);
					break;
			}
						
			Z.editor.activateItem(item);
			
			Z.map.changed();
			
			return true;
		}
		else if(sender == _btnResizeLeft)
		{
			_resizeStartVal = ((IResizable)_activeItem).getLeft();
		}
		else if(sender == _btnResizeRight)
		{
			_resizeStartVal = ((IResizable)_activeItem).getRight();
		}
		else if(sender == _btnResizeTop)
		{
			_resizeStartVal = ((IResizable)_activeItem).getTop();
		}
		else if(sender == _btnResizeBottom)
		{
			_resizeStartVal = ((IResizable)_activeItem).getBottom();
		}
		else if(sender == _btnFg)
		{
			activateTool(_toolFg);
		}
		else if(sender == _btnBg)
		{
			activateTool(_toolBg);
		}
		else if(sender == _btnZoomIn)
		{			
			Z.renderer.zoom(0.75f);			
		}
		else if(sender == _btnZoomOut)
		{
			Z.renderer.zoom(1.25f);	
		}
		else if(sender == _btnGridBig)
		{
			growGrid();
		}
		else if(sender == _btnGridSmall)
		{
			shrinkGrid();
		}
		else if(sender == _btnLoop)
		{
			activateTool(_toolLoop);
		}
		else if(sender == _btnChain)
		{
			activateTool(_toolChain);
		}
		else if(sender == _btnPoint)
		{
			activateTool(_toolPoint);
		}
		else if(sender == _btnNew)
		{
			newMap();
		}
		else if(sender == _btnOpen)
		{
			open();
		}
		else if(sender == _btnSave)
		{
			save();
		}
		else if(sender == _btnSaveAs)
		{
			saveAs();
		}
		return true;
	}
	
	public void growGrid()
	{
		//if(_grid < 8)
			_grid *= 2;
	}
	
	public void shrinkGrid()
	{
		//if(_grid > 0.125f)
			_grid /= 2f;
	}
	
	public float getGrid()
	{
		return _grid;
	}
	
	public Vector2 screenCentreWorld()
	{
		Game.workingVector2a.set(_stage.centerX(), _stage.centerY());
		Z.renderer.screenToWorld(Game.workingVector2a);
		return Game.workingVector2a;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		//Gdx.graphics.requestRendering();
		
		if(sender == _btnResizeLeft
				|| sender == _btnResizeRight
				|| sender == _btnResizeTop
				|| sender == _btnResizeBottom)
		{
			initResizeButtons();
		}
//		else if(sender == _btnTest)
//		{
//			test();
//		}
	}
	
	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{		
		//Gdx.graphics.requestRendering();
		
		if(sender == _btnResizeLeft
				|| sender == _btnResizeRight
				|| sender == _btnResizeTop
				|| sender == _btnResizeBottom)
		{
			Z.renderer.screenToWorld(delta);
			Z.renderer.screenToWorld(Game.workingVector2b.set(0, 0));
			delta.sub(Game.workingVector2b);
			IResizable rs = (IResizable)_activeItem;
			
			if(sender == _btnResizeLeft)	
				rs.setLeft(snap(_resizeStartVal + delta.x));
			else if(sender == _btnResizeRight)
				rs.setRight(snap(_resizeStartVal + delta.x));			
			else if(sender == _btnResizeTop)
				rs.setTop(snap(_resizeStartVal - delta.y));
			else if(sender == _btnResizeBottom)
				rs.setBottom(snap(_resizeStartVal - delta.y));
			
			_activeTool.onActiveItemChanged();
			Z.map.changed();
		}		
	}
	
	public float snap(float val)
	{
		float rm = val % _grid;
		if(rm > _grid/2f)
			return val + (_grid-rm);
		else if(rm < -(_grid/2f))
			return val - (rm + _grid);
		else return
				val - rm;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
//			case Keys.BACK:
//			case Keys.ESCAPE:
//				Game.ScreenManager.pop();
//				break;
			case Keys.SHIFT_LEFT:
			case Keys.SHIFT_RIGHT:
				_copyMode = true;
				break;
			default:
				return _activeTool.keyDown(keycode);
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Keys.SHIFT_LEFT:
			case Keys.SHIFT_RIGHT:
				_copyMode = false;
				return true;
			default: 
				return _activeTool.keyUp(keycode);				
		}		
	}

	@Override
	public boolean keyTyped(char character)
	{
		return _activeTool.keyTyped(character);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{		
		//Gdx.graphics.requestRendering();
		
		if(_stage.touchDown(x, y, pointer, button))
			return true;
		else if(_activeTool.touchDown(x, y, pointer, button))
			return true;
		else
		{
			if(_activeItem == null || _activeItem instanceof IMovable)
			{			
				Z.renderer.screenToWorld(_dragStartWorld.set(x, y));
				_dragStartScreen.set(x, y);

				if(_activeItem == null)
					_dragStartCamLocation.set(Z.renderer.cam.position.x, Z.renderer.cam.position.y);
				else
				{
					IMovable m = (IMovable)_activeItem;
					_dragStartCamLocation.set(m.getX(), m.getY());
					
					if(_copyMode && _activeItem instanceof ICopyableControl)
					{
						activateItem(_activeTool.copyActiveItem());
					}
				}
			}
			
			return true;
		}
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		//Gdx.graphics.requestRendering();
		
		if(_stage.touchUp(x, y, pointer, button))
			return true;
		else if(_activeTool.touchUp(x, y, pointer, button))
			return true;
		else
		{
			if(_activeItem instanceof IMovable)
			{
				initResizeButtons();	
				_activeTool.onActiveItemChanged();
			}
			return true;
		}
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		//Gdx.graphics.requestRendering();
		
		if(_stage.touchDragged(x, y, pointer))
			return true;
		else if(_activeTool.touchDragged(x, y, pointer))
			return true;
		else
		{
			if(_activeItem == null || _activeItem instanceof IMovable)
			{
				Game.workingVector2a.set(x, y).sub(_dragStartScreen);
				Z.renderer.screenToWorld(Game.workingVector2a);
				Z.renderer.screenToWorld(Game.workingVector2b.set(0, 0));				
				
				if(_activeItem == null)		
				{
					Game.workingVector2a.sub(Game.workingVector2b).mul(-1).add(_dragStartCamLocation);
					Z.renderer.cam.position.set(Game.workingVector2a.x, Game.workingVector2a.y, 0);
				}
				else 
				{
					Game.workingVector2a.sub(Game.workingVector2b).add(_dragStartCamLocation);
					IMovable m = (IMovable)_activeItem;
					float px = Game.workingVector2a.x;
					float py = Game.workingVector2a.y;
					m.setX(snap(px));
					m.setY(snap(py));
					Z.map.changed();
				}
					
				return true;
			}
			else return false;
		}
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		if(amount > 0)
			Z.renderer.zoom(0.75f);
		else if(amount < 0)
			Z.renderer.zoom(1.25f);		
		return true;
	}

//	@Override
//	public void onDialogResult(Object sender)
//	{
//		//Gdx.graphics.requestRendering();a
//		
//		if(sender == _dlgLabel)
//		{
//			if(_dlgLabel.getResult())
//			{
//				ILabelled active = (ILabelled)getActiveItem();
//				active.label(_dlgLabel.getValue());
//				Z.map.changed();
//			}
//		}
//		else if(sender == _dlgProps)
//		{
//			if(_dlgProps.getResult())
//			{
//				ILabelled active = (ILabelled)getActiveItem();
//				active.properties(_dlgProps.getValue());
//				Z.map.changed();
//			}
//		}
//	}
}
