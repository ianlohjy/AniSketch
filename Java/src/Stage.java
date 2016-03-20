import processing.core.PApplet;

public class Stage extends Element{

	Style default_style;
	
	Stage(int x, int y, int w, int h, PApplet p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.setDefault();
	}
	
	void draw()
	{
		default_style.apply();
		p.rect(x, y, w, h);
	}
	
}
