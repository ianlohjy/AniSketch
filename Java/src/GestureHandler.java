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
		line_style_default.stroke(150,150,150,255);
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
					Gesture candidate_gesture = new Gesture(gesture_engine, points);
					GestureEngine.GestureResponse response = gesture_engine.recogniseGesture(candidate_gesture);
					response.printTopGuesses(10);
					p.print("Candidate: " + candidate_gesture.initialSize[0] + " x " + candidate_gesture.initialSize[1]);
					p.println(" @ " + candidate_gesture.centroid);
					points.clear();
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
	
	public void processResponse(GestureEngine.GestureResponse gesture_response)
	{
		
	}
	
	public void drawLine()
	{
		float stroke_opacity;
		float min_opacity = 20;
		
		if(points.size()>1)
		{
			p.beginShape();
			for(int i=0; i<points.size(); i++)
			{
				//stroke_opacity = (float)i/points.size() * (150);
				//stroke_opacity += 255-150;
				//stroke_opacity = p.abs(stroke_opacity-255);
				//line_style_default.stroke(stroke_opacity, stroke_opacity, stroke_opacity, 200);
				line_style_default.apply();
				p.vertex(points.get(i).x, points.get(i).y);
				//p.line(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
			}
			p.endShape();
		}
	}
	
	public void update()
	{
		p.blendMode(p.SUBTRACT);
		drawLine();
		p.blendMode(p.NORMAL);
	}
	
}
