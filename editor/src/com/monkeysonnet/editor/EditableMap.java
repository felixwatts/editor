package com.monkeysonnet.editor;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.editor.Graphic;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;
import com.monkeysonnet.engine.editor.SectionEndMarker;
import com.monkeysonnet.engine.editor.Shape;

public class EditableMap
{
	public static final int FILE_VERSION = 3;
	
	public final Array<ControlGraphic> gfxBg = new Array<ControlGraphic>();
	public final Array<ControlGraphic> gfxFg = new Array<ControlGraphic>();
	public final Array<ControlPoint> points = new Array<ControlPoint>();
	public Array<IShapeControl> shapes = new Array<IShapeControl>();
	
	private boolean _hasChanges;
	
	public EditableMap(String filename)
	{
		this(new Map(filename, 1f, 0f, false));
	}
	
	public EditableMap(Map map)
	{
		if(map.gfxBg() != null)
		{
			for(Graphic g : map.gfxBg())
				gfxBg.add(new ControlGraphic(g));
			
			gfxBg.sort(new Comparator<ControlGraphic>()
					{
						@Override
						public int compare(ControlGraphic arg0, ControlGraphic arg1)
						{
							if(arg0.data().zIndex > arg1.data().zIndex)
								return 1;
							else if(arg0.data().zIndex < arg1.data().zIndex)
								return -1;
							else return 0;
						}
					});
		}
		if(map.gfxFg() != null)
		{
			for(Graphic g : map.gfxFg())
				gfxFg.add(new ControlGraphic(g));
			
			gfxFg.sort(new Comparator<ControlGraphic>()
					{
						@Override
						public int compare(ControlGraphic arg0, ControlGraphic arg1)
						{
							if(arg0.data().zIndex > arg1.data().zIndex)
								return 1;
							else if(arg0.data().zIndex < arg1.data().zIndex)
								return -1;
							else return 0;
						}
					});
		}
		if(map.points() != null)
			for(Point p : map.points())
				points.add(new ControlPoint(p));
		if(map.shapes() != null)
			for(Shape s : map.shapes())
				shapes.add(ControlShape.create(s));
	}
	
	public EditableMap()
	{
	}
	
	public void changed()
	{
		_hasChanges = true;		
	}
	
	public boolean hasChanges()
	{
		return _hasChanges;
	}
	
	public void serialize(String filename)
	{
		OutputStream strm = Gdx.files.absolute(filename).write(false);
		serialize(strm);
		try
		{
			strm.flush();
			strm.close();			
		} catch (IOException e)
		{
			e.printStackTrace();
		}		
	}

	public void serialize(OutputStream s)
	{
		ObjectOutputStream strm;
		try
		{
			strm = new ObjectOutputStream(s);
			
			if(points.size != 0)
			{
				strm.writeUTF("points");
				strm.writeInt(FILE_VERSION);
				strm.writeInt(points.size);
				for(ControlPoint p : points)
					p.data().serialize(strm);
				strm.writeObject(new SectionEndMarker());
			}
			
			if(shapes.size != 0)
			{
				strm.writeUTF("shapes");
				strm.writeInt(FILE_VERSION);
				strm.writeInt(shapes.size);
				for(IShapeControl shape : shapes)
				{
					shape.updateShapeData();
					shape.data().serialize(strm);
				}
				strm.writeObject(new SectionEndMarker());
			}
					
			if(gfxBg.size != 0)
			{
				strm.writeUTF("gfx-bg");
				strm.writeInt(FILE_VERSION);
				strm.writeInt(gfxBg.size);
				for(ControlGraphic g : gfxBg)
				{
					g.data().zIndex = gfxBg.indexOf(g, true);
					g.data().serialize(strm);
				}
				strm.writeObject(new SectionEndMarker());
			}
					
			if(gfxFg.size != 0)
			{
				strm.writeUTF("gfx-fg");
				strm.writeInt(FILE_VERSION);
				strm.writeInt(gfxFg.size);
				for(ControlGraphic g : gfxFg)
				{
					g.data().zIndex = gfxFg.indexOf(g, true);
					g.data().serialize(strm);
				}
				strm.writeObject(new SectionEndMarker());
			}
					
			strm.flush();
			
			_hasChanges = false;
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void shiftGraphic(ControlGraphic item, boolean foreground, boolean forward)
	{
		Array<ControlGraphic> items = foreground ? gfxFg : gfxBg;
		int i = items.indexOf(item, true);
		if((forward && i == items.size-1) || ((!forward) && i == 0))
			return;
		items.removeIndex(i);
		items.insert(forward ? i+1 : i-1, item);
	}
}
