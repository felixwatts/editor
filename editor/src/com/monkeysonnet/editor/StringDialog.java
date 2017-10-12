package com.monkeysonnet.editor;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.IDialogResultHandler;
import com.monkeysonnet.engine.IScreen;

public class StringDialog implements IScreen, IButtonEventHandler
{
	private static final float PADDING = 8;
	private Stage _stage;
	private TextField _txt;
	private ButtonActor _btnOk, _btnCancel;
	private IDialogResultHandler _handler;
	private boolean _result;
	
	public StringDialog()
	{
		initStage();
	}
	
	public void resize()
	{
		initStage();
	}
	
	public void show(IDialogResultHandler handler)
	{
		_handler = handler;
		Z.screens.push(this);
	}
	
	public boolean getResult()
	{
		return _result;
	}
	
	@Override
	public void pause()
	{
	}
	
	public String getValue()
	{
		String txt = _txt.getText();
		if(txt == "")
			return null;
		else return txt;
	}
	
	public void setValue(String v)
	{
		if(v == null)
			v = "";
		_txt.setText(v);
	}
	
	@Override
	public void show()
	{
		
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(_stage);
		_stage.setKeyboardFocus(_txt);
	}

	@Override
	public void render()
	{
		_stage.draw();
	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
		//_stage.dispose();
	}

	@Override
	public boolean isFullScreen()
	{
		return false;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		return true;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		_result = sender == _btnOk;
		_handler.onDialogResult(this);		
		Z.screens.pop();		
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}
	
	private void initStage()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		TextFieldStyle style = new TextFieldStyle();
		style.font = Z.font;	
		style.fontColor = Color.WHITE;
		style.background = Z.ninePatch;
		_txt = new TextField(style);
		
		NinePatchGroup grp = new NinePatchGroup(Z.ninePatch);
		grp.width = _txt.width + PADDING + PADDING;
		grp.height = _txt.height + _txt.height + PADDING + PADDING + PADDING;
		grp.x = (_stage.width() - grp.width) / 2f;
		grp.y = (_stage.height() - grp.height) / 2f;
		
		_stage.addActor(grp);
		
		_txt.x = PADDING;
		_txt.y = grp.height - _txt.height - PADDING;
		
		_btnOk = new ButtonActor(
				grp.width - PADDING - _txt.height - PADDING - _txt.height,
				_txt.y - PADDING - _txt.height, 
				_txt.height, 
				_txt.height, 
				Z.textures.get("button-editor-yes"), 
				this);
		
		_btnCancel = new ButtonActor(
				grp.width - PADDING - _txt.height, 
				_btnOk.y, 
				_btnOk.width, 
				_btnOk.height, 
				Z.textures.get("button-editor-no"), 
				this);
		
		grp.addActor(_txt);
		grp.addActor(_btnOk);
		grp.addActor(_btnCancel);
		
		_stage.addActor(grp);
	}
}
