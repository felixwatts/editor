package com.monkeysonnet.editor;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.ITextureSource;

public class MultiTextureSource implements ITextureSource
{
	private final TextureAtlas _builtinTextures = new TextureAtlas("pack");	
	private final TextureAtlas _externalTextures;	
	//private final Array<AtlasRegion> _externalTextures = new Array<TextureAtlas.AtlasRegion>();
	private final Array<AtlasRegion> _allTextures = new Array<TextureAtlas.AtlasRegion>();
	
	public MultiTextureSource()
	{
		Settings settings = new Settings();
		settings.padding = 2;
		settings.maxWidth = 512;
		settings.maxHeight = 512;
		settings.minWidth = 1;
		settings.minHeight = 1;
		settings.incremental = false;
		settings.pot = true;
		settings.edgePadding = true;
		TexturePacker.process(settings, "./textures", "../texture-pack");
		
		_externalTextures = new TextureAtlas(Gdx.files.internal("../texture-pack/pack"));
		
//		FileHandle dir = Gdx.files.internal("textures");
//		if(dir.exists() && dir.isDirectory())
//		{
//			for(FileHandle file : dir.list(".png"))
//			{
//				if(file.name().startsWith("env-"))
//				{				
//					Texture tex = new Texture(file);
//					tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
//					AtlasRegion r = new AtlasRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
//					r.name = file.nameWithoutExtension();
//					
//					_externalTextures.add(r);
//				}
//			}
//		}
		
		for(AtlasRegion r : _builtinTextures.getRegions())
			_allTextures.add(r);
		for(AtlasRegion r : _externalTextures.getRegions())
			_allTextures.add(r);
		//_allTextures.addAll(_externalTextures);
	}

	@Override
	public TextureRegion get(String textureName)
	{
		TextureRegion tex = _builtinTextures.findRegion(textureName);
		if(tex == null)
			tex = _externalTextures.findRegion(textureName);
		
		if(tex == null)
			Gdx.app.log("monkeysonnet", "missing texture: " + textureName);
		
		return tex;
	}

	@Override
	public Iterable<AtlasRegion> getAll()
	{
		return _allTextures;
	}

	@Override
	public List<AtlasRegion> getFrames(String name)
	{
		return _externalTextures.findRegions(name);
	}

}
