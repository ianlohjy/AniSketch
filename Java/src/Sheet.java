import processing.core.PApplet;

public class Sheet extends Element{

	Style default_style;
	
	Sheet(int x, int y, int w, int h, PApplet p)
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
