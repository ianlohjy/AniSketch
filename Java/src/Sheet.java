import processing.core.PApplet;

public class Sheet extends Element{

	Style default_style;
	
	float x_camera_offset;
	float y_camera_offset;
	
	Sheet(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(230,230,230,255);
	}
	
	void draw()
	{
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		p.noClip();
	}
	
}
