import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Stage extends Element{

	Style default_style;
	Primitive test_child;
	Primitive test_parent;
	float count = 1;
	ArrayList<Primitive> primitives;
	
	PVector camera;
	
	Stage(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		
		this.camera = new PVector(x,y);
		
		default_style = new Style(p);
		//default_style.fill(255,70,0,255); // Orange
		default_style.fill(150,150,150,255);
		
		primitives = new ArrayList<Primitive>();
		//addPrimitive(250,200,100,200,this,p);
		
		test_parent = new Primitive(250,250,100,200,this,p);
		test_child = new Primitive(400,250,100,100,this,p);
		
		
		//test_child.setParent(test_parent);
		test_child.setPivot(0, 50);
	}
	
	void draw()
	{
		test_child.setPivot(0, p.frameCount/5);
		this.camera.x = x;
		this.camera.y = y;
		
		//test.setHeightTop(p.random(155));//p.frameCount/10f);
		p.clip(x, y, w, h);
		default_style.apply(); // Apply style for Stage window
		
		p.rect(x, y, w, h);
		
		test_child.update();
		test_parent.update();
		
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
			test_child.checkMouseEvent(e);
			test_parent.checkMouseEvent(e);
		}
	}
}
