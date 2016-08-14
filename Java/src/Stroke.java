import processing.core.*;
import processing.event.MouseEvent;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;

public class Stroke 
{
	//===========//
	// ESSENTIAL //
	//===========//
	AnimationController a;
	AniSketch p;
	
	//===================//
	// STROKE PROPERTIES //
	//===================//
	long start_frame = 0;
	ArrayList<StrokePoint> points;
	boolean marked_for_deletion = false;
	final static int PLAY_ONCE = 0;
	final static int LOOP = 1;
	final static int HOLD = 2;
	int play_mode = PLAY_ONCE;
	
	//====================//
	// DISPLAY PROPERTIES //
	//====================//
	final static int max_ink_width = 40;
	final static int visible_range = 150;
	final static int range_fade_amount = 50;
	
	//==============//
	// MOUSE STATES //
	//==============//
	boolean hover, selected;
	int hover_focus = -1; // Indicates the closest index to the mouse (if close enough)
	
	// SELECTION // 
	long last_time_selected = 0;
	
	Stroke(AniSketch p, AnimationController a)
	{
		this.p = p;
		this.a = a;
		this.hover = false;
		this.selected = false;
		this.points = new ArrayList<StrokePoint>();
	}
	
	//======//
	// MAIN //
	//======//
	// Currently empty
	public void update()
	{
	}
	
	void draw()
	{
		if(selected && p.main_windows.sheet.isCompositionMode())
		{
			drawInk((int)a.current_frame-(int)start_frame, visible_range, range_fade_amount, false, 30, 150);	
		}
		else
		{
			drawInk((int)a.current_frame-(int)start_frame, visible_range, range_fade_amount, false, 100, 30);
		}
		
		if(hover_focus != -1)
		{
			if(selected)
			{
				// We want to make sure that the focus is drawn over the stroke
				p.translate(0,0,10);
				drawInkFocus(hover_focus, 5, 1f);
				p.translate(0,0,-10);
			}
			else
			{
				drawInkFocus(hover_focus, 5, 0.75f);
			}
		}
		
		/*// Draws boundings boxes used in collisions.
		for(float[] cur_box: cur_bounds)
		{
			p.noFill();
			p.stroke(255,0,0);
			p.strokeWeight(2);
			p.rect(cur_box[0],cur_box[1],cur_box[2],cur_box[3]);
		}
		*/
	}
	
	//==========//
	// GRAPHICS //
	//==========//
	// Draws a simple solid stroke
	void drawLine()
	{
		p.stroke(0);
		p.strokeWeight(10);
		
		if(points != null && points.size() > 1)
		{
			for(int l=0; l<points.size()-1; l++)
			{
				p.line(points.get(l).pos.x, points.get(l).pos.y, points.get(l+1).pos.x, points.get(l+1).pos.y);
			}
		}
	}
	
	// Draws an 'ink' version of the stroke, width and opacity of the stroke segments are determined by the average neighbour distances
	void drawInk(int index_focus, int render_range, int edge_fade_amt, boolean show_all, float base_lightness, float base_opacity)
	{
		if(points != null && points.size() > 1)
		{
			int start_index = -1;
			int end_index = -1;
			
			if(show_all)
			{
				start_index = 0;
				end_index = points.size();
			}
			else
			{
				int[] index_range = findPointsIndexRangeForFocus(index_focus,render_range);
				start_index = index_range[0];
				end_index = index_range[1];
			}
			
			if(show_all || start_index != -1)
			{
				int fade_in_limit  = index_focus-render_range+edge_fade_amt;
				int fade_out_limit = index_focus+render_range-edge_fade_amt;
				
				//p.println("FADE OUT AT INDEX " + fade_out_limit);
				
				for(int pt=start_index; pt<end_index-1; pt++)
				{
					float width = widthFilter(points.get(pt).avg_neighbour_dist);
					float opacity = base_opacity;
					
					if(fade_in_limit >= 0 && pt < fade_in_limit)
					{
						opacity = -base_opacity * (((float)(fade_in_limit-pt)/edge_fade_amt)-1);
					}					
					if(fade_out_limit < points.size() && pt > fade_out_limit)
					{
						opacity = -base_opacity * (((float)(pt-fade_out_limit)/edge_fade_amt)-1);
					}
					
					p.noFill();
					p.stroke(base_lightness,opacity);
					p.strokeWeight(width);
					if(width < 20)
					{
						p.line(points.get(pt).pos.x, points.get(pt).pos.y, points.get(pt+1).pos.x, points.get(pt+1).pos.y);
					}
					
					if(width > 5)
					{
						p.noStroke();
						p.fill(base_lightness,opacity*0.75f);
						p.ellipse(points.get(pt).pos.x, points.get(pt).pos.y, width, width);
					}
				}
			}
		}
		drawCursor();
	}
	
	// Overlays an ink style cursor to mark where on the stroke is the current frame 
	void drawCursor()
	{
		//p.println(a.current_frame + " " + (start_frame+points.size()-1));
		if(a.current_frame >= start_frame && a.current_frame <= start_frame+points.size())
		{
			drawInkCursor((int)(a.current_frame-start_frame));
		}
		else if(play_mode == HOLD && a.current_frame > start_frame+points.size())
		{
			drawInkCursor(points.size());
		}
		else if(play_mode == LOOP && a.current_frame > start_frame+points.size())
		{
			drawInkCursor((int)(a.current_frame%points.size())-1);
		}
	}
	
	// Given an index to focus on, a highlight will be drawn on top of the stroke, fading away from the focus
	void drawInkFocus(int index_focus, int range, float fade)
	{
		// Fade should be between 0 and 1
		int[] index_range = findPointsIndexRangeForFocus(index_focus, range);
		
		int num_left_points = index_focus-index_range[0];
		if(num_left_points >= 0)
		{
			for(int i=index_range[0]; i<index_focus; i++)
			{
				float opacity = -1*(((index_focus-i)/(float)range)-1) * fade;
				float width = widthFilter(points.get(i).avg_neighbour_dist);
				
				//p.println(i + " " + (index_focus-index_range[0]));
				
				if(width < 20)
				{
					p.noFill();
					p.stroke(0,opacity*255);
					p.strokeWeight(width);
					p.line(points.get(i).pos.x, points.get(i).pos.y, points.get(i+1).pos.x, points.get(i+1).pos.y);
				}
				
				if(width > 10)
				{
					p.fill(0,opacity*255*0.75f);
					p.noStroke();
					p.ellipse(points.get(i).pos.x, points.get(i).pos.y, width, width);
				}	
			}
		}
		
		int num_right_points = index_range[1]-index_focus;
		if(num_right_points >= 0)
		{
			for(int i=index_focus; i<index_range[1]-1; i++)
			{
				float opacity = -1*((((i-index_focus+1)/(float)range))-1) * fade;
				float width = widthFilter(points.get(i).avg_neighbour_dist);
				
				//p.println(opacity);
				if(width < 20)
				{
					p.noFill();
					p.stroke(0,opacity*255);
					p.strokeWeight(width);
					p.line(points.get(i).pos.x, points.get(i).pos.y, points.get(i+1).pos.x, points.get(i+1).pos.y);	
				}
				
				if(width > 10)
				{
					p.fill(0,opacity*255*0.75f);
					p.noStroke();
					p.ellipse(points.get(i).pos.x, points.get(i).pos.y, width, width);
				}
			}
		}
	}
	
	// Highlights an index on the stroke, fading away as the index gets smaller.
	void drawInkCursor(int index)
	{
		int trail_length = 10;
		int start_index = index-trail_length;
		int end_index = index-1;
		int num_points = 0;

		if(start_index < 0)
		{
			start_index = 0;	
		}
		num_points = end_index-start_index;
		p.noFill();
		for(int c=start_index; c<end_index; c++)
		{
			float opacity = 0;
			float width = widthFilter(points.get(c).avg_neighbour_dist);
			
			if(c != 0 && num_points!=0)
			{
				opacity = ((float)(c-start_index)/(float)num_points)*255;
			}
			
			p.stroke(0,opacity);
			p.strokeWeight(width);
			p.line(points.get(c).pos.x, points.get(c).pos.y, points.get(c+1).pos.x, points.get(c+1).pos.y);
			
			if(width > 10)
			{
				p.noStroke();
				p.fill(0,opacity);
				p.ellipse(points.get(c).pos.x, points.get(c).pos.y, width, width);
			}	
			
			if(c==end_index-1)
			{
				p.fill(0);
				p.ellipse(points.get(c+1).pos.x, points.get(c+1).pos.y, width, width);
				p.fill(0);
				p.ellipse(points.get(c+1).pos.x, points.get(c+1).pos.y, width, width);
			}
		}
	}
	
	//================//
	// STROKE EDITING //
	//================//
	
	// Adds a StrokePoint object to points, and updates stroke neighbour data
	void addPoint(float x, float y)
	{	
		points.add(new StrokePoint(x,y));
		
		for(StrokePoint point: points)
		{
			point.updateNeighbourData();
		}
	}

	// Marks stroke for deletion. Deletion is handled during the animation controller's update loop
	void delete()
	{
		marked_for_deletion = true;
	}
	
	// Cycles in between the different possible stroke play modes
	void cyclePlayMode()
	{
		if(play_mode == PLAY_ONCE)
		{
			play_mode = HOLD;
		}
		else if(play_mode == HOLD)
		{
			play_mode = LOOP;
		}
		else if(play_mode == LOOP)
		{
			play_mode = PLAY_ONCE;
		}
	}
	
	//======================//
	// STROKE DATA HANDLING //
	//======================//
	// Given the current stroke points and an index 'focus', find the start and stop index that is +/- in range from the focus
	// Returns -1,-1 if it is out of range
	int[] findPointsIndexRangeForFocus(int index_focus, int range)
	{
		int[] return_index = {-1, -1}; // [0] = start index / [1] = stop index
		
		if(points != null && points.size() > 1)
		{
			if(index_focus <= 0)// && (index_focus+range) > 0) // If the index focus is before the stroke start
			{
				return_index[1] = index_focus + range;
				if(return_index[1] > 0) // If the object is in range
				{
					return_index[0] = 0;
				}
				if(return_index[1] > points.size()-1)
				{
					return_index[1] = points.size()-1;
				}
			}
			else if(index_focus >= points.size()-1)// && (index_focus-range) > points.size()-1) // If the index focus is after the stroke end
			{
				return_index[0] = index_focus-range;
				if(return_index[0] < points.size())
				{
					return_index[1] = points.size();
				}
				if(return_index[0] < 0)
				{
					return_index[0] = 0;
				}
			}
			else if(index_focus < points.size() && index_focus >= 0) // If the index focus is within the stroke
			{
				return_index[0] = index_focus - range;
				return_index[1] = index_focus + range;
				
				if(return_index[0] < 0)
				{
					return_index[0] = 0;
				}
				if(return_index[1] > points.size())
				{
					return_index[1] = points.size();
				}
			}	
		}
		return return_index;
	}
	
	// Returns a width valued based on an input value (should be average neighbour distance). Clamps the width to a maximum set by 'max_ink_width'
	float widthFilter(float value)
	{
		float result = value;
		
		result = (2/result)*80;
		
		if(result > max_ink_width)
		{
			result = max_ink_width;
		}
		return result;
	}
	
	// Returns the PVector position of the stroke at a certain frame. Returns null if out of range
	PVector positionAtFrame(int frame_number)
	{
		if(frame_number >= start_frame && frame_number <= start_frame+points.size()-1)
		{
			return points.get(frame_number-(int)start_frame).pos;
		}
		else if(frame_number > start_frame+points.size()-1 && play_mode == HOLD)
		{
			return points.get(points.size()-1).pos;
		}
		else if(frame_number > start_frame+points.size() && play_mode == LOOP)
		{
			return points.get((frame_number%points.size())).pos;
		}
		else 
		{
			return null;
		}
	}
	
	//======================//
	// COLLISION AND BOUNDS //
	//======================//
	
	// Returns collision details about the line or point that is closest to the mouse
	float[] checkCollision(MouseEvent e)
	{
		// Response details
		// If the mouse is closest to a line segment
		// {closest distance, line index1, line index2, index ratio of index1 along line} 
		// If the mouse is closest to a point (happens when the line segment it too short for accurate collisions)
		// {closest distance, point index, point index, 1} 
		
		int[] index_range = findPointsIndexRangeForFocus((int)a.current_frame-(int)start_frame,visible_range);//-(range_fade_amount/5));
		float[] collision_response = {-1,-1,-1,-1}; 
		
		if(index_range[0] != -1) // If the line is being displayed
		{
			ArrayList<int[]> segments = segmentsWithinRange(index_range[0], index_range[1], 50, e.getX(), e.getY());	
			
			if(segments.size() != 0) // If there are segments to check
			{
				// If the values are -1, it means that the result is invalid
				int closest_index = -1;
				float closest_distance = -1;
				
				for(int[] segment: segments) // For each segment
				{	
					for(int c=segment[0]; c<segment[1]; c++) // step through individual strokes and check detection
					{	
						float[] check_result = mouseDistToStrokeSegment(c, e.getX(), e.getY());
						
						if(check_result[0] != -1 && check_result[0] != -2) // If the result is valid
						{
							// If the current closest distance has not been set yet OR if closest distance is larger than the checked distance
							if(closest_distance == -1 || closest_distance >= check_result[0]) 
							{
								// Update the closest distance and closest index
								closest_distance = check_result[0];
								if(check_result[1] < 0.5) {closest_index = c;}
								else {closest_index = c+1;}
								// Update the return result
								collision_response[0] = closest_distance;
								collision_response[1] = c;
								collision_response[2] = c+1;
								collision_response[3] = check_result[1];
							}
						}
						else if(check_result[0] == -2) // A return value of -2 is given if the segment is too small to check, so instead we will do a distance check
						{
							float dist_to_e = PApplet.dist(points.get(c).pos.x, points.get(c).pos.y, e.getX(), e.getY());
							
							// If the checked distance is smaller, or if closest distance is invalid
							if(closest_distance >= dist_to_e || closest_distance == -1)
							{
								closest_distance = dist_to_e;
								closest_index = c;
								collision_response[0] = closest_distance;
								collision_response[1] = c;
								collision_response[2] = c;
								collision_response[3] = 1;
							}
						}
					}
				}
			}
		}
		return collision_response;
	}
	
	// Find the distance of the mouse to a stroke segment, returning the distance, and the ratio of how close the mouse is to both points
	float[] mouseDistToStrokeSegment(int index, float x_input, float y_input)
	{
		// If the line is not in range, it will return a -1
		// If the stroke in question is too short, it will return -2, in this case, a distance check is recomended instead
		float[] distances = {-1,-1,-1};
		
		if(index+1 < points.size() && index+1 > 0)
		{	
			// Get the 2 points
			StrokePoint cur_point = points.get(index);
			StrokePoint next_point = points.get(index+1);
			PVector cur_line = next_point.pos.copy().sub(cur_point.pos);
		
			if(cur_line.mag() > 5) // If the stroke is really short do not do a check, return -2 instead
			{
				PVector line_rot = cur_line.copy().rotate(PApplet.HALF_PI).setMag(30);
				
				if(!Utilities.isPointLeftOfLine(cur_point.pos.x, cur_point.pos.y, cur_point.pos.x+line_rot.x, cur_point.pos.y+line_rot.y, x_input, y_input))
				{
					if(Utilities.isPointLeftOfLine(next_point.pos.x, next_point.pos.y, next_point.pos.x+line_rot.x, next_point.pos.y+line_rot.y, x_input, y_input))
					{
						// If the mouse is within the bounds of the line, find the distances to the mouse
						float dist_from_line = Utilities.distToLine(cur_point.pos.x, cur_point.pos.y, next_point.pos.x, next_point.pos.y, x_input, y_input);
						float dist_along_line = Utilities.distToLine(cur_point.pos.x, cur_point.pos.y, cur_point.pos.x+line_rot.x, cur_point.pos.y+line_rot.y, x_input, y_input);
						distances[0] = dist_from_line;
						distances[1] = dist_along_line/cur_line.mag();
						distances[2] = 1 - distances[1];	
					}
				}
			}			
			else
			{
				distances[0] = -2;
				distances[1] = -2;
				distances[2] = -2;
			}
		}
		return distances;
	}

	// A first pass collision checker. Divides up and performs a bounding box check for different segments of the stroke
	// Returns a set of indexes that can be used for finer collision checking 
	ArrayList<int[]> segmentsWithinRange(int start_range, int stop_range, int segment_length, float x_input, float y_input)
	{
		ArrayList<int[]> segments = new ArrayList<int[]>();
		
		float dist_traversed = 0;
		int cur_segment_start = 0;
		float cur_segment_max_l = 0;
		float cur_segment_max_r = 0;
		float cur_segment_max_t = 0;
		float cur_segment_max_b = 0;
		int bounding_box_buffer = 5;
		
		for(int p=start_range; p<stop_range-1; p++)
		{
			StrokePoint cur_point = points.get(p);
			StrokePoint nxt_point = points.get(p+1);
			float next_buffer = (widthFilter(nxt_point.avg_neighbour_dist)*0.5f) + bounding_box_buffer;
			
			// If this is the very first point in the range, setup the bounding box values
			if(p == start_range)
			{
				float cur_buffer = (widthFilter(cur_point.avg_neighbour_dist)*0.5f + bounding_box_buffer);
				cur_segment_max_l = cur_point.pos.x - cur_buffer;
				cur_segment_max_r = cur_point.pos.x + cur_buffer;
				cur_segment_max_t = cur_point.pos.y - cur_buffer;
				cur_segment_max_b = cur_point.pos.y + cur_buffer;
			}
			
			// Update the segment's bounding box if the next point makes a larger box
			if(nxt_point.pos.x-next_buffer < cur_segment_max_l) {cur_segment_max_l = nxt_point.pos.x-next_buffer;}
			if(nxt_point.pos.x+next_buffer > cur_segment_max_r) {cur_segment_max_r = nxt_point.pos.x+next_buffer;}
			if(nxt_point.pos.y-next_buffer < cur_segment_max_t) {cur_segment_max_t = nxt_point.pos.y-next_buffer;}
			if(nxt_point.pos.y+next_buffer > cur_segment_max_b) {cur_segment_max_b = nxt_point.pos.y+next_buffer;}
			
			// Find the current distance traverse
			dist_traversed += PApplet.dist(cur_point.pos.x, cur_point.pos.y, nxt_point.pos.x, nxt_point.pos.y);
			
			// If the dist traversed meets the length requirments, or we have reached the end of the range
			if(dist_traversed >= segment_length || p == stop_range-2)
			{
				if(Utilities.withinBounds(cur_segment_max_l, cur_segment_max_t, cur_segment_max_r-cur_segment_max_l, cur_segment_max_b-cur_segment_max_t, x_input, y_input))
				{
					int[] segment_range = {cur_segment_start, p+1};
					segments.add(segment_range);
					//float[] cur_box = {cur_segment_max_l, cur_segment_max_t, cur_segment_max_r-cur_segment_max_l, cur_segment_max_b-cur_segment_max_t};
					//cur_bounds.add(cur_box);
					//PApplet.println("ADDING A SEGMENT " + cur_segment_start + " " + (p+1) + "/" + (points.size()-1));
				}
				cur_segment_start = p+1; // Set the new segment start
				cur_segment_max_l = nxt_point.pos.x - next_buffer; // Reset to new segment bounding values
				cur_segment_max_r = nxt_point.pos.x + next_buffer; 
				cur_segment_max_t = nxt_point.pos.y - next_buffer; 	
				cur_segment_max_b = nxt_point.pos.y + next_buffer;
				dist_traversed = 0; // Reset the traverse distance
			}
		}
		return segments;
	}
	
	//==========================//
	// MOUSE/SELECTION HANDLING //
	//==========================//
	
	void updateSelectionTime()
	{
		last_time_selected = System.currentTimeMillis();
	}
	
	public boolean oldestSelectionOf(ArrayList<Stroke> selectable_strokes)
	{
		// Checks if the current Stroke's last selected time is the lowest of all selectable strokes
		// Returns true if it is the lowest

		for(Stroke other_strokes: selectable_strokes)
		{
			if(other_strokes.last_time_selected < this.last_time_selected)
			{
				return false;
			}
		}
		return true;
	}
	
	int[] checkMouseEvent(MouseEvent e, Stroke active_stroke_selection, ArrayList<Stroke> selectable_strokes, boolean allow_switching)
	{
		float[] collision_response = checkCollision(e);
		boolean within_bounds = false;
		int[] mouse_status = {0,0};
		
		if(collision_response[0] != -1)
		{	
			within_bounds = true;
			
			if((int)collision_response[1] != (int)collision_response[2])
			{
				if(collision_response[3] < 0.5)
				{
					hover_focus = (int)collision_response[1];
				}
				else
				{
					hover_focus = (int)collision_response[2];
				}
			}
			else
			{
				hover_focus = (int)collision_response[1];
			}
		}
		else
		{
			hover_focus = -1;
		}
		
		if(e.getAction() == 1) // When mouse is pressed (down)
		{
			if(e.getButton() == 37)
			{
				if(within_bounds) 
				{
					//selected = true;
				}
				else 
				{
					selected = false;
					
				}
			}
			if(e.getButton() == 39)
			{
				// REGISTER GESTURE EVENT //
				if(within_bounds) {p.gesture_handler.registerObject(this, e);}
				////////////////////////////
			}
		}
		else if(e.getAction() == 2) // When mouse is released
		{
			// REGISTER GESTURE EVENT //
			if(within_bounds) {p.gesture_handler.registerObject(this, e);}
			////////////////////////////
		}
		else if(e.getAction() == 3) // When mouse is clicked (down then up)
		{
			// This is where we will handle selection switching
			if(e.getButton() == 37)
			{
				if(within_bounds)
				{
					// If this stroke is already selected, deselect it
					if(active_stroke_selection == this)
					{
						if(selectable_strokes.contains(this) 
						&& selectable_strokes.size() != 1)
						{
							selected = false;
							mouse_status[1] = -1;
						}
					}
					else
					{
						// If this is a valid selection
						if(oldestSelectionOf(selectable_strokes))
						{
							if(allow_switching)
							{
								// Deselect all other strokes
								for(Stroke other_strokes: selectable_strokes)
								{
									other_strokes.selected = false;
								}
								
								//selected = true;
								//updateSelectionTime();
								mouse_status[1] = 1;
							}
						}
					}
				}
			}
		}
		else if(e.getAction() == 4) // When mouse is dragged
		{
		}
		else if(e.getAction() == 5) // When mouse is moved
		{
			if(within_bounds) {hover = true;}
			else {hover = false;}
		}
		
		if(within_bounds)
		{
			mouse_status[0] = 1;
		}
		else
		{
			mouse_status[0] = -1;
		}
		
		return mouse_status;
		
	}
	
	//=======================//
	// STROKE POINT SUBCLASS //
	//=======================//
	// A class to hold data and calculate point information
	class StrokePoint
	{
		PVector pos;
		int num_neighbours = 0;
		int neighbours_to_check = 5;
		float avg_neighbour_dist = 0;
		
		StrokePoint(float x, float y)
		{
			pos = new PVector(x,y);
		}
		
		void updateNeighbourData()
		{
			num_neighbours = 0;
			avg_neighbour_dist = 0;
			// Find the current index
			int cur_index = points.indexOf(this);
			int check_index = cur_index;
			
			// Check neighbours on tbe left
			while(true)
			{
				if(check_index-1 < 0 || check_index-1 < cur_index-neighbours_to_check)
				{
					check_index = cur_index;
					break;	
				}
				avg_neighbour_dist += PApplet.dist(points.get(check_index).pos.x, points.get(check_index).pos.y, points.get(check_index-1).pos.x, points.get(check_index-1).pos.y);
				num_neighbours++;
				check_index--;
			}
			// Check neighbours on tbe right
			while(true)
			{
				if(check_index+1 > points.size()-1 || check_index+1 > cur_index+neighbours_to_check)
				{
					check_index = cur_index;
					break;
				}
				avg_neighbour_dist += PApplet.dist(points.get(check_index).pos.x, points.get(check_index).pos.y, points.get(check_index+1).pos.x, points.get(check_index+1).pos.y);
				num_neighbours++;
				check_index++;
			}
			avg_neighbour_dist /= num_neighbours;
		}
	}
	
}
