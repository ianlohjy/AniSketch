import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;


public class GestureHandler {

	GestureEngine gesture_engine;
	ArrayList<PVector> points;
	Style line_style_default;
	PApplet p;
	
	GestureHandler(PApplet p)
	{
		
		points = new ArrayList<PVector>();
		line_style_default = new Style(p);
		this.p = p;
		setupStyles();
		setupGestureEngine();
	}
	
	public void setupGestureEngine()
	{
		gesture_engine = new GestureEngine();
		gesture_engine.loadGestureTemplatesFrom("./gestures/", true);
	}
	
	public void setupStyles()
	{
		line_style_default.noFill();
		line_style_default.stroke(0,0,0,255);
		line_style_default.strokeWeight(3);
	}
	
	public void checkMouseEvent(MouseEvent e)
	{
		if(e.getButton() == 39)
		{ // If its a right click
			if(e.getAction() == 1)
			{
				// Mouse Pressed
				points.clear();
				points.add(new PVector(e.getX(), e.getY()));
			}
			else if(e.getAction() == 2)
			{
				// Mouse Released
				if(points.size() > 1)
				{
					GestureEngine.GestureResponse response = gesture_engine.recogniseGesture(points);
					response.printTopGuesses(10);
				}
			}
			else if(e.getAction() == 4)
			{
				// Mouse Dragged
				points.add(new PVector(e.getX(), e.getY()));
			}
			else if(e.getAction() == 5)
			{
			}
		}
	}
	
	public void drawLine()
	{
		if(points.size()>1)
		{
			line_style_default.apply();
			for(int i=0; i<points.size()-1; i++)
			{
				p.line(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
			}
		}
	}
	
	public void update()
	{
		drawLine();
	}
	
}
