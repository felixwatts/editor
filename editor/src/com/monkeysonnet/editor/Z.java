package com.monkeysonnet.editor;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.monkeysonnet.engine.ITextureSource;
import com.monkeysonnet.engine.ScreenManager;

public class Z
{
	public static final ScreenManager screens = new ScreenManager();
	public static EditableMap map;
	public static Renderer renderer;
	public static ITextureSource textures;
	public static BitmapFont font;
	public static EditorScreen editor;
	public static NinePatch ninePatch;
}
