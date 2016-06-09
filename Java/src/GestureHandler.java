import java.awt.dnd.peer.DropTargetPeer;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.jogamp.common.util.IOUtil;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.JSONObject;
import processing.event.MouseEvent;


public class GestureHandler {

	GestureEngine gesture_engine;
	ArrayList<PVector> drawn_points;
	Style line_style_default;
	AniSketch p;
	float min_draw_distance = 1;
	
	GestureState gesture_state; // Gesture state shows the number of objects that were detected by the gesture, and what their properties are at the time
	
	boolean gesture_active = false;
	
	GestureHandler(AniSketch p)
	{
		
		drawn_points = new ArrayList<PVector>();
		line_style_default = new Style(p);
		this.p = p;
		setupStyles();
		setupGestureEngine();
	}
	
	public void setupGestureEngine()
	{
		gesture_state = new GestureState();
		gesture_engine = new GestureEngine();
		
		// Load gesture templates
		if(p.export)
		{
			// If AniSketch is packaged into a jar file, we need to extract the gestures beforehand
			
			try
			{	
				// Adapted from http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
				CodeSource src = AniSketch.class.getProtectionDomain().getCodeSource();
				if(src != null)
				{
					URL jar = src.getLocation();
					ZipInputStream zip = new ZipInputStream(jar.openStream());
					
					while(true) 
					{
						ZipEntry e = zip.getNextEntry();
					    if (e == null)
					    {
					    	break;
					    }
					    
					    String gesture_path = e.getName();
					    // If a gesture was found, load it
					    if(gesture_path.endsWith(".gst"))
					    {
					    	p.println("Found gesture @ " + gesture_path);
					    	// Loading gesture into template
					    	// Adapted from 
					    	// http://stackoverflow.com/questions/11496040/how-should-resources-in-a-compiled-jar-be-accessed
					    	// http://stackoverflow.com/questions/22461663/convert-inputstream-to-jsonobject
					    	
					    	//InputStream input_stream = AniSketch.class.getResourceAsStream("/resources/gestures/ARROW_0.gst");
					    	InputStream input_stream = AniSketch.class.getResourceAsStream("/" + gesture_path);
					    	
							try
							{
								BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream, "UTF-8"));
								StringBuilder out_string = new StringBuilder();
								String read_string;
								
								while(true)
								{
									read_string = reader.readLine();
									
									if(read_string == null)
									{
										break;
									}
									out_string.append(read_string);
								}
								JSONObject return_json = p.parseJSONObject(out_string.toString());
								gesture_engine.loadGestureTemplate(return_json, false);
								Utilities.printAlert("Gesture loaded successfully");
							}
							catch(Exception e1)
							{
								Utilities.printError("Could not parse gesture");
								e1.printStackTrace();
							}
					    }
					}
				}
			}
			catch(Exception e)
			{
				Utilities.printError("There was a problem unpacking gestures");
				e.printStackTrace();
			}
			
			
			
			//String test_gesture_url = p.getResource("/resources/gestures/ARROW_0.gst");
			
			//ClassLoader classLoader = getClass().getClassLoader();
			//File file = new File(classLoader.getResourceAsStream("/resources/gestures/ARROW_0.gst").ger);
			
			//p.println("THIS IS A TEST " + file.getAbsolutePath());
			
			//URL test_gesture_url = AniSketch.class.getResource("/resources/gestures/ARROW_0.gst");

			
			
			
			//InputStream is = AniSketch.class.getResourceAsStream("/resources/gestures/ARROW_0.gst");
			//JSONObject test = PApplet.loadJSONObject(new File("/C:/Users/Ian/Desktop/AniSketchDemo.jar!/resources/gestures/ARROW_0.gst")); 
			//p.println(" !TESF " + is.toString());
			
			
			
			
		}
		else
		{
			String gestures_url = p.getResource("/resources/gestures/");
			p.println(gestures_url);
			gesture_engine.loadGestureTemplatesFromFolder(gestures_url, false);
		}
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
		//p.println(e.isAltDown());
		
		if(e.getButton() == 39)
		{ // If its a right click
			if(e.getAction() == 1)
			{
				// Mouse Pressed
				drawn_points.clear();
				drawn_points.add(new PVector(e.getX(), e.getY()));
				gesture_active = true;
				
			}
			else if(e.getAction() == 2)
			{
				// Mouse Released
				if(drawn_points.size() > 1)
				{
					Gesture gesture_candidate = new Gesture(gesture_engine, drawn_points); // The input gesture
					GestureEngine.GestureResponse gesture_response = gesture_engine.recogniseGesture(gesture_candidate); // The response based on input gesture
					gesture_response.printTopGuesses(10);
					
					// Set Start and End point of input gesture to gesture response
					gesture_response.startPoint = drawn_points.get(0);
					gesture_response.endPoint   = new PVector(e.getX(), e.getY());
					
					processResponse(gesture_response, gesture_state, gesture_candidate);
					
					// Reset gestures state
					drawn_points.clear();
					gesture_state.reset();
					gesture_active = false;	
				}
			}
			else if(e.getAction() == 3) // Mouse Clicked
			{
			}
			else if(e.getAction() == 4) // Mouse Dragged
			{
				// If mouse distance to last point is larger than the threshold, add a point
				if(p.dist(drawn_points.get(drawn_points.size()-1).x, drawn_points.get(drawn_points.size()-1).y, e.getX(), e.getY()) >= min_draw_distance)
				{
					drawn_points.add(new PVector(e.getX(), e.getY()));
				}
				
			}
			else if(e.getAction() == 5)
			{
			}
		}
	}
	
	public void processResponse(GestureEngine.GestureResponse response, GestureState state, Gesture candidate){
		
		// If the highest score is greater than the threshold
		
		if(response.bestScore > 75)
		{
			switch (response.bestGesture.gestureName) {
			
			case "CANCEL":
				if(state.startObjectSize() == 1)
				{
					if(state.start_point_objects.get(0).mouse_state_selected && state.start_point_objects.get(0).isPrimitive())
					{
						Primitive found_object = (Primitive)state.start_point_objects.get(0).object;
            			found_object.unparent();
					}
					if(state.start_point_objects.get(0).mouse_state_selected && state.start_point_objects.get(0).isKey())
					{
						Key found_object = (Key)state.start_point_objects.get(0).object;
            			found_object.disconnectAllKeys();
					}
				}
			break;
			
            case "CIRCLE":
            	// BREAKS HERE
            	if(p.main_windows.stage.withinBounds((int)response.endPoint.x, (int)response.endPoint.y))
            	{
            		ArrayList<GestureState.ObjectState> found_selected_objects = state.selectedStartObjectsSize();
            		if(found_selected_objects.size() == 1)
            		{
            			if(state.start_point_objects.get(0).isPrimitive())
	            		{
		            		if(candidate.initialSize[0] <= 50 && candidate.initialSize[1] <= 50)
		            		{
		            			Primitive found_object = (Primitive)state.start_point_objects.get(0).object;
		            			found_object.setPivotUsingGlobalPosition(candidate.centroid.x, candidate.centroid.y);
		            		}	
	            		}
            		}
            	}
            	if(p.main_windows.sheet.withinBounds((int)response.startPoint.x, (int)response.startPoint.y))
            	{
            		Sheet sheet = p.main_windows.sheet;
            		p.animation.addKey(candidate.centroid.x, candidate.centroid.y, (candidate.initialSize[0]+candidate.initialSize[1])/2);
            	}
            p.println("THIS IS A CIRCLE");
            break;
            
            case "SQUARE_CW":
            	if(p.main_windows.stage.withinBounds((int)response.startPoint.x, (int)response.startPoint.y))
            	{
            		if(p.main_windows.stage.withinBounds((int)response.endPoint.x, (int)response.endPoint.y))
                	{
            			if(candidate.initialSize[0] > 50)
            			{
            				Stage stage = p.main_windows.stage;
            				p.main_windows.stage.addPrimitive(candidate.centroid.x - stage.camera.x, candidate.centroid.y - stage.camera.y, candidate.initialSize[0], candidate.initialSize[1], p.main_windows.stage, p.main_windows.sheet, p.animation, p);
            			}
            		}
            	}
            p.println("THIS IS A SQUARE");
            break;
			
            case "SQUARE_CCW":
            	if(p.main_windows.stage.withinBounds((int)response.startPoint.x, (int)response.startPoint.y))
            	{
            		if(p.main_windows.stage.withinBounds((int)response.endPoint.x, (int)response.endPoint.y))
                	{
            			//Stage stage = p.main_windows.stage;
            			//float[] box_shape = findBoxShapeAndAngle(candidate.input_points); //!!! BUGGY
            			if(candidate.initialSize[0] > 50)
            			{
            				Stage stage = p.main_windows.stage;
                			p.main_windows.stage.addPrimitive(candidate.centroid.x - stage.camera.x, candidate.centroid.y - stage.camera.y, candidate.initialSize[0], candidate.initialSize[1], p.main_windows.stage, p.main_windows.sheet, p.animation, p);
            				//p.println("BOX SHAPE " + box_shape[0] + " " + box_shape[1] + " " + box_shape[2] + " " + box_shape[3] + " " + box_shape[4] + " " );
            				//Primitive new_primitive = p.main_windows.stage.addPrimitive(box_shape[0] - stage.camera.x, box_shape[1] - stage.camera.y, box_shape[3], box_shape[4], p.main_windows.stage, p.main_windows.sheet, p.animation, p);
            				//new_primitive.rotation = box_shape[2]+90;
            			}
            			
                	}
            	}
            p.println("THIS IS A SQUARE");
            break;
            
            case "RECT_CCW":
            	if(p.main_windows.stage.withinBounds((int)response.startPoint.x, (int)response.startPoint.y))
            	{
            		if(p.main_windows.stage.withinBounds((int)response.endPoint.x, (int)response.endPoint.y))
                	{
            			if(candidate.initialSize[0] > 50)
            			{
            				Stage stage = p.main_windows.stage;
            				p.main_windows.stage.addPrimitive(candidate.centroid.x - stage.camera.x, candidate.centroid.y - stage.camera.y, candidate.initialSize[0], candidate.initialSize[1], p.main_windows.stage, p.main_windows.sheet, p.animation, p);
            			}
            		}
            	}
            p.println("THIS IS A RECT");
            break;
			
            case "RECT_CW":
            	if(p.main_windows.stage.withinBounds((int)response.startPoint.x, (int)response.startPoint.y))
            	{
            		if(p.main_windows.stage.withinBounds((int)response.endPoint.x, (int)response.endPoint.y))
                	{
            			if(candidate.initialSize[0] > 50)
            			{
            				Stage stage = p.main_windows.stage;
            				p.main_windows.stage.addPrimitive(candidate.centroid.x - stage.camera.x, candidate.centroid.y - stage.camera.y, candidate.initialSize[0], candidate.initialSize[1], p.main_windows.stage, p.main_windows.sheet, p.animation, p);
            			}
                	}
            	}
            p.println("THIS IS A RECT");
            break;
            
            case "DELETE":
            	ArrayList<GestureState.ObjectState> found_selected_objects = state.selectedStartObjectsSize();
            	
            	if(found_selected_objects.size() == 1)
            	{
            		if(found_selected_objects.get(0).isPrimitive())
        			{
        				Primitive found_object = (Primitive)found_selected_objects.get(0).object;
            			//if(found_object.withinBounds(response.startPoint.x, response.startPoint.y))
            			//{
            			found_object.delete();
            			Utilities.printAlert("DELETING OBJECT");
            			//}
        			}
        			else if(found_selected_objects.get(0).isKey())
        			{
        				Key found_object = (Key)found_selected_objects.get(0).object;
            			//if(found_object.withinBounds(response.startPoint.x, response.startPoint.y))
            			//{
            			found_object.delete();
            			Utilities.printAlert("DELETING KEY");
            			//}
        			}
        			else if(found_selected_objects.get(0).isStroke())
        			{
        				Stroke found_object = (Stroke)found_selected_objects.get(0).object;
            			//if(found_object.withinBounds(response.startPoint.x, response.startPoint.y))
            			//{
        				found_object.delete();
            			Utilities.printAlert("DELETING STROKE");
            			//}
        			}
            	}
            p.println("EXECUTING DELETE");
            break;
			}
		}
		// If the gesture score is too low
		// Check to see if the it is more or less a straight line
		else if(checkAngleVarience(drawn_points) < 30)
		{
			PApplet.println("SAME OBJECTS? " + state.ifSameObject());
			PApplet.println("Gesture is likely a straight line");
			PApplet.println("OBJECT SIZES " + state.startObjectSize() + "  " + state.endObjectSize());
			// If there was only one object at the start and end, and the start object was selected when the gesture was performed
			if(state.startObjectSize() == 1 && state.endObjectSize() == 1 && state.start_point_objects.get(0).mouse_state_selected)
			{
				PApplet.println("There is only 1 start and 1 end object, and start object is selected");
				// If the objects are not the same
				if(!state.ifSameObject())
				{
					PApplet.println("Objects are not the same instance");
					// If both start and end objects are Primitives
					if(state.end_point_objects.get(0).isPrimitive() && state.start_point_objects.get(0).isPrimitive())
					{
						PApplet.println("PARENTING");
						Primitive start_object = (Primitive)state.start_point_objects.get(0).object;
						Primitive end_object = (Primitive)state.end_point_objects.get(0).object;
						start_object.setParent(end_object);
					}
					if(state.end_point_objects.get(0).isKey() && state.start_point_objects.get(0).isKey())
					{
						PApplet.println("CONNECTING");
						Key start_object = (Key)state.start_point_objects.get(0).object;
						Key end_object = (Key)state.end_point_objects.get(0).object;
						start_object.connectToKey(end_object);
					}
				}
			}
		}
	}
	
	public float[] findBoxShapeAndAngle(ArrayList<PVector> input_points)
	{
		PVector top_point;
		PVector bottom_point;
		PVector left_point;
		PVector right_point;
		
		if(input_points != null && input_points.size() != 0)
		{
			top_point = input_points.get(0).copy();
			bottom_point = input_points.get(0).copy();
			left_point = input_points.get(0).copy();
			right_point = input_points.get(0).copy();
			
			for(PVector cur_point: input_points)
			{
				if(cur_point.y < top_point.y)
				{
					top_point = cur_point;
				}
				if(cur_point.y > bottom_point.y)
				{
					bottom_point = cur_point;
				}
				if(cur_point.x > right_point.x)
				{
					right_point = cur_point;
				}
				if(cur_point.x < left_point.x)
				{
					left_point = cur_point;
				}
			}
			
			// Find the 'vertical' vector axis of the box shape
			PVector top_local_pt    = new PVector((left_point.x+top_point.x)/2, (left_point.y+top_point.y)/2);
			PVector bottom_local_pt = new PVector((right_point.x+bottom_point.x)/2, (right_point.y+bottom_point.y)/2);
			PVector vertical_axis = top_local_pt.copy().sub(bottom_local_pt);

			// Find the 'horizontal' vector axis of the box shape
			PVector left_local_pt = new PVector((left_point.x+bottom_point.x)/2, (left_point.y+bottom_point.y)/2);
			PVector right_local_pt = new PVector((top_point.x+right_point.x)/2, (top_point.y+right_point.y)/2);
			PVector horizontal_axis = right_local_pt.copy().sub(left_local_pt);

			// Find the center point by averaging all points
			PVector center = new PVector((top_local_pt.x+bottom_local_pt.x+left_local_pt.x+right_local_pt.x)/4, (top_local_pt.y+bottom_local_pt.y+left_local_pt.y+right_local_pt.y)/4);
			
			float[] result = {center.x, center.y, PApplet.degrees(vertical_axis.heading()), horizontal_axis.mag(), vertical_axis.mag()};
			return result;
		}
		return null;
	}
	
	public float checkAngleVarience(ArrayList<PVector> points)
	{
		float average_angle_variance = 0;
		
		PVector global_vector = points.get(points.size()-1).copy();
		global_vector = global_vector.sub(points.get(0));
		
		for(int p=0; p<points.size()-1; p++)
		{
			PVector local_vector = points.get(p+1).copy();
			local_vector = local_vector.sub(points.get(p));
			
			average_angle_variance += PVector.angleBetween(global_vector, local_vector);	
		}
		
		average_angle_variance = average_angle_variance/(points.size()-1);
		PApplet.println( "ANGLE VARIANCE IS " + PApplet.degrees(average_angle_variance) );
		return PApplet.degrees(average_angle_variance);
	}
	
	/*
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
	*/
	
	// registerObject method is used in other parts of the program to register objects with the gesture state. 
	void registerObject(Object object, MouseEvent e)
	{
		if(e.getButton() == 39 && e.getAction() == 1)
		{
			gesture_active = true;
		}
		
		if(gesture_active) // If gesture mode (right-click) is active
		{	
			if(e.getAction() == 1) // Mouse Pressed (This represents the start of the gesture)
			{
				gesture_state.addStartObject(object);
			}
			if(e.getAction() == 2) // Mouse Released (This represents the end of the gesture)
			{
				gesture_state.addEndObject(object);
			}
		}
	}
	
	public void drawLine()
	{
		float stroke_opacity;
		float min_opacity = 20;
		
		p.pushMatrix();
		p.translate(0,0,100);
		
		if(drawn_points.size()>1)
		{
			p.fill(255,255);
			p.noStroke();
			p.ellipse(drawn_points.get(0).x, drawn_points.get(0).y, 10, 10);
			p.beginShape();
			for(int i=0; i<drawn_points.size(); i++)
			{
				line_style_default.apply();
				p.vertex(drawn_points.get(i).x, drawn_points.get(i).y);
			}
			p.endShape();
		}
		p.popMatrix();
	}
	
	public void update()
	{
		p.blendMode(p.SUBTRACT);
		drawLine();
		p.blendMode(p.NORMAL);
	}
	
	class GestureState
	{
		// GestureStateReport holds the data needed for 
		// the GestureHandler to decide what gesture/operation 
		// has been triggered. AppGestureWindowReport is 
		// designed to be generated by the individual windows 
		// (Stage, Sheet, Time line) and returned when 
		// handleGestureResponse() is run.
		
		ArrayList<ObjectState> start_point_objects;
		ArrayList<ObjectState> end_point_objects;
		
		GestureState()
		{
			start_point_objects = new ArrayList<ObjectState>();
			end_point_objects   = new ArrayList<ObjectState>();
		}
		
		boolean ifSameObject()
		{
			// If there is only one object for each, and they are the same objects
			if(startObjectSize() == 1 && endObjectSize() == 1)
			{
				PApplet.println(start_point_objects.get(0).object);
				PApplet.println(end_point_objects.get(0).object);
				if(start_point_objects.get(0).object == end_point_objects.get(0).object)
				{
					return true;
				}
			}
			return false;
		}
		
		ArrayList<ObjectState> selectedStartObjectsSize()
		{
			ArrayList<ObjectState> selected = new ArrayList<ObjectState>();
			for(ObjectState object: start_point_objects)
			{
				if(object.selected())
				{
					selected.add(object);
				}
			}
			return selected;
		}
		
		ArrayList<ObjectState> selectedEndObjectsSize()
		{
			ArrayList<ObjectState> selected = new ArrayList<ObjectState>();
			for(ObjectState object: end_point_objects)
			{
				if(object.selected())
				{
					selected.add(object);
				}
			}
			return selected;
		}
		
		int startObjectSize()
		{
			return start_point_objects.size();
		}
		
		int endObjectSize()
		{
			return end_point_objects.size();
		}
		
		void reset()
		{
			start_point_objects.clear();
			end_point_objects.clear();
		}
		
		void addStartObject(Object object)
		{
			start_point_objects.add(new ObjectState(object));
		}
		
		void addEndObject(Object object)
		{
			end_point_objects.add(new ObjectState(object));
		}
		
		class ObjectState
		{
			// Object state holds the object as well as some other data 
			// regarding its properties at the time of event
			
			// Object Types
			static final int SHEET = -2;
			static final int STAGE = -1;
			static final int NONE  =  0; 
			static final int PRIMITIVE = 1;
			static final int KEY       = 2;
			static final int STROKE    = 3;
			
			boolean mouse_state_selected = false;
			boolean mouse_state_hover = false;
			int object_type = NONE;
			int window = NONE;
			Object object;
			
			ObjectState(Object input_object)
			{
				object = input_object;
				
				if(input_object instanceof Primitive)
				{
					object_type = PRIMITIVE;
					Primitive primitive = (Primitive)input_object;
					mouse_state_selected = primitive.selected;
					mouse_state_hover = primitive.hover;
					window = STAGE;
				}
				
				if(input_object instanceof Key)
				{
					object_type = KEY;
					Key key = (Key)input_object;
					mouse_state_selected = key.selected;
					mouse_state_hover = key.hover;
					window = SHEET;
				}
				
				if(input_object instanceof Stroke)
				{
					object_type = STROKE;
					Stroke stroke = (Stroke)input_object;
					mouse_state_selected = stroke.selected;
					mouse_state_hover = stroke.hover;
					window = SHEET;
				}
			}
			
			boolean selected()
			{
				if(mouse_state_selected)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			boolean hover()
			{
				if(mouse_state_hover)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			boolean isPrimitive()
			{
				if(object_type == PRIMITIVE)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			boolean isKey()
			{
				if(object_type == KEY)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			boolean isStroke()
			{
				if(object_type == STROKE)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}
	
}
