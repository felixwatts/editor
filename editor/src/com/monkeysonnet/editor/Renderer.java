package com.monkeysonnet.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IProjection;

public class Renderer implements IProjection
{
	private static final float FRUSTUM_WIDTH = 10;
	
	private static final Plane _xyPlane = new Plane(new Vector3(0, 0, 1), 0);
	
	public final OrthographicCamera cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_WIDTH * ((float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()));
	
	private final SpriteBatch _sprites = new SpriteBatch();
	
	private final TextureRegion _texGrid = Z.textures.get("grid");
	
	public void resize(float w, float h)
	{
		cam.setToOrtho(false, cam.viewportWidth, cam.viewportWidth * (h / w));
		cam.position.set(0, 0, 0);
	}
	
	public void render()
	{
		cam.update();
		
		GL10 gl = Gdx.app.getGraphics().getGL10();		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
        cam.apply(gl);  
        
        _sprites.setProjectionMatrix(cam.combined);
        _sprites.begin();		
		renderGrid();
		renderMap();			
		_sprites.end();
		
		Z.editor.getStage().draw();
		renderLabels();
	}

	private void renderLabels()
	{
		// labels
		
		Z.editor.getStage().getSpriteBatch().begin();
		
		for(IControl g : Z.map.gfxBg)
		{
			g.drawLabel(Z.editor.getStage().getSpriteBatch());
		}
		
		for(IControl s : Z.map.shapes)
		{
			s.drawLabel(Z.editor.getStage().getSpriteBatch());
		}
		
		for(IControl p : Z.map.points)
		{
			p.drawLabel(Z.editor.getStage().getSpriteBatch());
		}
		
		for(IControl g : Z.map.gfxFg)
		{
			g.drawLabel(Z.editor.getStage().getSpriteBatch());
		}
		
		Z.editor.getStage().getSpriteBatch().end();
	}

	private void renderMap()
	{
		for(ControlGraphic g : Z.map.gfxBg)
		{
			g.sprite().draw(_sprites);
		}
		
		for(IShapeControl s : Z.map.shapes)
		{
			for(Sprite e : s.getEdgeSprites())
				e.draw(_sprites);
		}
		
//		for(PointControl p : Z.map.points)
//		{
//			
//		}
		
		for(ControlGraphic g : Z.map.gfxFg)
		{
			g.sprite().draw(_sprites);
		}
	}

	private void renderGrid()
	{
		float frustrumWidth = Math.abs(cam.frustum.planePoints[0].x - cam.frustum.planePoints[2].x);
        float frustrumHeight = Math.abs(cam.frustum.planePoints[0].y - cam.frustum.planePoints[2].y);    
        
        _sprites.disableBlending();
        
//		float x = Gdx.graphics.getFramesPerSecond();
//		if(x > 55)
//			_sprites.setColor(Color.WHITE);
//		else if(x > 50)
//			_sprites.setColor(Color.YELLOW);
//		else _sprites.setColor(Color.RED);
        
    	// background
		float grid = Z.editor.getGrid()*2f;
//		if(grid < 0)
//			grid = BACKGROUND_TEXTURE_SCALE;
	
		float u1 = ((cam.position.x % grid) / grid) - ((frustrumWidth / grid) / 2f);
		float v1 = ((cam.position.y % grid) / grid) - ((frustrumHeight / grid) / 2f);
		float u2 = u1 + frustrumWidth / grid;
		float v2 = v1 + frustrumHeight / grid;
		
		_sprites.draw(
				_texGrid.getTexture(), 
				cam.position.x - frustrumWidth / 2f,
				cam.position.y - frustrumHeight / 2f,
				frustrumWidth, 
				frustrumHeight, 
				u1,//		Color c = new Color();
//				Z.editor.propsToColour(props, c);
//				_sprite.setColor(c);
				v1, 
				u2, 
				v2);

        _sprites.enableBlending();
		_sprites.setColor(Color.WHITE);      
	}

	public void screenToWorld(Vector2 screenPoint)
	{
		Ray ray = cam.getPickRay(screenPoint.x, screenPoint.y);
		Intersector.intersectRayPlane(ray, _xyPlane, Game.workingVector3);
		screenPoint.set(Game.workingVector3.x, Game.workingVector3.y);
	}
	
	public void worldToScreen(Vector2 worldPoint)
	{
		Game.workingVector3.set(worldPoint.x, worldPoint.y, 0);
		cam.project(Game.workingVector3);
		worldPoint.set(Game.workingVector3.x, Game.workingVector3.y);
	}
	
	public void zoom(float factor)
	{
		cam.zoom *= factor;
	}
}
