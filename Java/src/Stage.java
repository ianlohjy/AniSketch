import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Stage extends Element{

	Style default_style;
	Primitive test;
	Primitive test2;
	float count = 1;
	
	Stage(int x, int y, int w, int h, PApplet p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(200,200,200,255);
		
		test = new Primitive(50,50,100,100,this,p);
		test.rotation = 0;
		test.setPivot(50,50);
		test.setPivot(50,50);
		test.setPivot(50,100);
		//test.setPivot(100,100);
		test2 = new Primitive(250,200,100,200,this,p);
		test2.rotation = 15;
	}
	
	void draw()
	{
		PVector test_vector = new PVector(100,0);
		
		//test.rotation = p.frameCount/5;
		test.setPivot(0, count);
		count+=0.1;
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		test.update();
		test2.update();
		p.noClip();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		if(withinBounds(e.getX(), e.getY()))
		{
			test.checkMouseEvent(e);
			test2.checkMouseEvent(e);
		}
		
	}
	
}
