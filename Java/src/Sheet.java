import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Sheet extends Element{

	Style default_style;
	//ArrayList<PVector> drawn_points;
	PVector camera;
	boolean drawing;
	long time_since_last_point = System.currentTimeMillis();
	
	static final int UP = 0;
	static final int DOWN = 1;
	
	int framerate = 25; // The framerate sets limits the number of points per second that you are able to draw
	int millis_per_frame = 1000/25;
	
	int mouse_state = UP;
	AnimationController a;
	
	Sheet(int x, int y, int w, int h, AniSketch p, AnimationController a)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(230,230,230,255);
		//drawn_points = new ArrayList<PVector>();
		drawing = false;
		this.a = a;
		
		a.addKey(200, 200, 150);
	}
	
	void draw()
	{
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		drawStrokes();
		drawKeys();
		p.noClip();
		
		update();
	}
	
	void drawKeys()
	{
		for(Key key: a.keys)
		{
			key.draw();
		}
	}
	
	void drawStrokes()
	{
		for(Stroke stroke: a.strokes)
		{
			stroke.drawSimple();
		}
		
		if(a.recorded_stroke != null)
		{
			a.recorded_stroke.drawSimple();
		}
	}
	
	void update()
	{
		if(drawing)
		{
			a.recordStroke(p.mouseX, p.mouseY);
		}
		else
		{
			a.stopStroke();
		}
		//drawMotionLine();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		if(withinBounds(e.getX(), e.getY()))
		{
			if(e.getButton() == 37)
			{ // If its a left click
				if(e.getAction() == 1)// Mouse Pressed
				{
					drawing = true;
				}
				else if(e.getAction() == 2)// Mouse Released
				{
					//a.stopStroke();
					drawing = false;
				}
				else if(e.getAction() == 3) // Mouse Clicked
				{
				}
				else if(e.getAction() == 4) // Mouse Dragged
				{
					//a.recordStroke(e.getX(), e.getY());
				}
				else if(e.getAction() == 5)
				{
				}
			}
		}
	}
	
	/*
	void drawMotionLine()
	{
		p.stroke(0);
		p.strokeWeight(10);
		
		if(drawn_points != null && drawn_points.size()>1)
		{
			for(int l=0; l<drawn_points.size()-1; l++)
			{
				p.line(drawn_points.get(l).x, drawn_points.get(l).y, drawn_points.get(l+1).x, drawn_points.get(l+1).y);
			}
		}
	}
	*/
	
	
}
