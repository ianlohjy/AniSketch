import processing.core.PApplet;

public class Timeline extends Element{
	Style default_style;
	
	Timeline(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(30,30,30,255);
	}
	
	void draw()
	{
		default_style.apply();
		p.rect(x, y, w, h);
	}
	
}
