import processing.core.PApplet;
import processing.event.MouseEvent;

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
		
		drawTimeline();
	}
	
	void drawTimeline()
	{
		// Find the line length
		float length = PApplet.dist(x+(50), 0, x+(w-50), 0);
		// Draw base timeline
		p.noFill();
		p.stroke(255);
		p.strokeWeight(5);
		p.line(x+(50), y+(h*0.5f), x+(w-50), y+(h*0.5f));
		// Draw time progression line
		float progression_length = (float)p.animation.current_frame/p.animation.frame_range[1]*length;
		if(progression_length > length)
		{
			progression_length = length;
		}
		p.stroke(0,150);
		p.line(x+(50), y+(h*0.5f), x+(50+progression_length), y+(h*0.5f));
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		
		//if(e.getAction() =)
	}
	
}
