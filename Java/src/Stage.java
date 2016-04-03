import processing.core.PApplet;
import processing.event.MouseEvent;

public class Stage extends Element{

	Style default_style;
	Primitive test;
	
	Stage(int x, int y, int w, int h, PApplet p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(200,200,200,255);
		
		test = new Primitive(200,200,100,200,this,p);
		test.rotation = 15;
	}
	
	void draw()
	{
		//test.rotation = p.frameCount;
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		test.update();
		p.noClip();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		test.checkMouseEvent(e);
	}
	
}
