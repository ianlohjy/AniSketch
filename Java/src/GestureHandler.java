import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;


public class GestureHandler {

	GestureEngine gesture_engine;
	ArrayList<PVector> points;
	Style line_style_default;
	AniSketch p;
	float min_draw_distance = 1;
	
	GestureStateReport gesture_state_report;
	//ArrayList<Object> start_point_objects;
	//ArrayList<Object> end_point_objects;
	
	boolean gesture_active = false;
	
	GestureHandler(AniSketch p)
	{
		
		points = new ArrayList<PVector>();
		line_style_default = new Style(p);
		this.p = p;
		setupStyles();
		setupGestureEngine();
	}
	
	public void setupGestureEngine()
	{
		gesture_state_report = new GestureStateReport();
		gesture_engine = new GestureEngine();
		gesture_engine.loadGestureTemplatesFrom("./gestures/", false);
	}
	
	public void setupStyles()
	{
		line_style_default.noFill();
		line_style_default.stroke(150,150,150,255);
		line_style_default.strokeWeight(3);
	}
	
	public boolean hasStarted()
	{
		return false;
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
				gesture_active = true;
				
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
					p.println("Start Point Objects: " + gesture_state_report.start_point_objects.size());
					p.println("End Point Objects: " + gesture_state_report.end_point_objects.size());
					
					// Set Start and End point of input gesture to gesture response
					response.startPoint = points.get(0);
					response.endPoint   = new PVector(e.getX(), e.getY());
					
					points.clear();
					gesture_active = false;
					processResponse(response, gesture_state_report, candidate_gesture);
					
					// Reset gesture_state_report
					gesture_state_report = new GestureStateReport();
				}
			}
			else if(e.getAction() == 3) // Mouse Clicked
			{
			}
			else if(e.getAction() == 4)
			{
				// Mouse Dragged
				if(p.dist(points.get(points.size()-1).x, points.get(points.size()-1).y, e.getX(), e.getY()) >= min_draw_distance)
				{
					points.add(new PVector(e.getX(), e.getY()));
				}
				
			}
			else if(e.getAction() == 5)
			{
			}
		}
	}
	
	public void processResponse(GestureEngine.GestureResponse gesture_response, GestureStateReport gesture_state_report, Gesture gesture_candidate)
	{
		GestureStateReport gsr = gesture_state_report;
		
		if(gsr.start_point_objects.size() >= 1 && gsr.end_point_objects.size() >= 1)
		{
			if(gsr.start_point_objects.size() == 1 && gsr.start_point_objects.size() == 1)
			{
				p.println("1 start/end point object found for each");
				
				if(gsr.start_point_objects.get(0) == gsr.end_point_objects.get(0))
				{
					p.println("Start/End objects are the same");
					if(gsr.start_point_objects.get(0) instanceof Primitive)
					{
						p.println("Object is a Primitive");
						if(gesture_response.bestGuess.equals("CIRCLE") && gesture_response.bestScore > 70)
						{
							p.println("CIRCLE gesture detected");
							if(gesture_candidate.initialSize[0] <= 30 && gesture_candidate.initialSize[1] <= 30)
							{
								p.println("Looks like we want to change the pivot position for primitive @" + (Primitive)gsr.start_point_objects.get(0));
								Primitive detected_primitive = (Primitive)gsr.start_point_objects.get(0);
								detected_primitive.setPivotUsingGlobalPosition(gesture_candidate.centroid.x, gesture_candidate.centroid.y);
							}
						}
					}
				}
			}
		}
	}
	
	void registerObject(Object object, MouseEvent e)
	{
		if(e.getButton() == 39 && e.getAction() == 1)
		{
			gesture_active = true;
		}
		
		if(gesture_active)
		{	
			if(e.getAction() == 1) // Mouse Pressed
			{
				if(object instanceof GestureHandler)
				{
					p.println("instance of GH");
				}
				if(object instanceof Primitive)
				{
					p.println("instance of PR");
				}
				gesture_state_report.start_point_objects.add(object);
			}
			if(e.getAction() == 2) // Mouse Released
			{
				gesture_state_report.end_point_objects.add(object);
			}
		}
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
				line_style_default.apply();
				p.vertex(points.get(i).x, points.get(i).y);
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
	
	class GestureStateReport
	{
		// GestureStateReport holds the data needed for 
		// the GestureHandler to decide what gesture/operation 
		// has been triggered. AppGestureWindowReport is 
		// designed to be generated by the individual windows 
		// (Stage, Sheet, Time line) and returned when 
		// handleGestureResponse() is run.
		
		ArrayList<Object> start_point_objects;
		ArrayList<Object> end_point_objects;
		
		GestureStateReport()
		{
			start_point_objects = new ArrayList<Object>();
			end_point_objects   = new ArrayList<Object>();
		}
	}
	
	/*
	class AppGestureWindowReport
	{
		
		
		boolean window_registered_start;
		boolean window_registered_end;
		ArrayList<Object> registered_objects_start;
		
		AppGestureWindowReport()
		{
			boolean window_registered = false;
			// registered_objects = new ArrayList<Object>();
		}
	}
	*/
	
}
