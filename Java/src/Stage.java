import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Stage extends Element{

	Style default_style;
	Primitive test;
	Primitive test2;
	float count = 1;
	ArrayList<Primitive> primitives;
	
	Stage(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(255,70,0,255);
		
		primitives = new ArrayList<Primitive>();
		addPrimitive(250,200,100,200,this,p);
		
		test = new Primitive(50,50,100,100,this,p);
		test.rotation = 150;
		test.setPivot(50,50);
	}
	
	void draw()
	{

		p.clip(x, y, w, h);
		default_style.apply(); // Apply style for Stage window
		
		p.rect(x, y, w, h);
		test.update();
		
		updatePrimitives();
		
		p.noClip();
	}

	void updatePrimitives()
	{
		for(int p=0; p<primitives.size(); p++)
		{
			primitives.get(p).update();
		}
	}
	
	void addPrimitive(float x, float y, float w, float h, Stage stage, AniSketch p)
	{
		primitives.add(new Primitive(x, y, w, h, this, p));
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		if(withinBounds(e.getX(), e.getY()))
		{
			for(int p=0; p<primitives.size(); p++)
			{
				primitives.get(p).checkMouseEvent(e);
			}
			test.checkMouseEvent(e);
		}
	}
}
