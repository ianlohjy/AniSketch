
import java.util.ArrayList;

import javax.swing.GroupLayout.SequentialGroup;

import jogamp.graph.geom.plane.Crossing.CubicCurve;
import processing.core.*;
import processing.event.MouseEvent;

public class Stroke {

	AniSketch p;
	long start_frame = 0;
	//long current_position = 0;
	ArrayList<StrokePoint> points;
	AnimationController a;
	
	final static int visible_range = 150;
	final static int range_fade_amount = 50;
	final static int max_ink_width = 40;
	Stroke(AniSketch p, AnimationController a)
	{
		this.p = p;
		this.a = a;
		points = new ArrayList<StrokePoint>();
	}
	
	boolean hover = false;
	boolean selected = false;
	int hover_focus = -1;
	
	
	public void update()
	{
		
	}
	
	//ArrayList<float[]> cur_bounds = new ArrayList<float[]>();
	
	void addPoint(float x, float y)
	{	
		points.add(new StrokePoint(x,y));
		
		for(StrokePoint point: points)
		{
			point.updateNeighbourData();
		}
	}

	
	void draw()
	{
		drawInk((int)a.current_frame-(int)start_frame, visible_range, range_fade_amount, false);
		if(hover_focus != -1)
		{
			//p.stroke(255,0,0);
			//p.strokeWeight(5);
			p.noStroke();
			p.fill(255,0,0);
			p.ellipse(points.get(hover_focus).pos.x, points.get(hover_focus).pos.y, 10, 10);
			//p.line(points.get(hover_focus).pos.x, points.get(hover_focus).pos.y, points.get(hover_focus+1).pos.x, points.get(hover_focus+1).pos.y);
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
	
	void drawCursor()
	{
		p.noStroke();
		p.fill(0);
		
		if(a.current_frame >= start_frame && a.current_frame <= start_frame+points.size()-1 )
		{
			drawCursor2((int)(a.current_frame-start_frame));
			//p.ellipse(points.get((int)(a.current_frame-start_frame)).pos.x, points.get((int)(a.current_frame-start_frame)).pos.y, 20f, 20f);
		}
	}
	
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
	
	boolean checkCollision()
	{
		int[] range_to_check = findPointsIndexRangeForFocus((int)a.current_frame-(int)start_frame, visible_range);
	
		if(range_to_check[0] != -1)
		{
			 
		}
		return false;
	}
	
	float[] mouseDistToStrokeSegment(int index, float x_input, float y_input)
	{
		// Find the distance of the mouse to a stroke segment, returning the distance, and the ratio of how close the mouse is to both points
		// If the line is not in range, it will return a -1
		// If the stroke in question is too short, it will return -2, in this case, a distance check is recomended instead
		float[] distances = {-1,-1,-1};
		
		if(index+1 < points.size() && index+1 > 0)
		{	
			// Get the 2 points
			StrokePoint cur_point = points.get(index);
			StrokePoint next_point = points.get(index+1);
			
			PVector cur_line = next_point.pos.copy().sub(cur_point.pos);
			
			//stroke_buffer  = stroke_buffer.rotate(PApplet.HALF_PI);
			
			//p.println(cur_line.mag() + " " + PApplet.dist(cur_point.pos.x, cur_point.pos.y, next_point.pos.x, next_point.pos.y));
			
			if(cur_line.mag() > 5) // If the stroke is really short do not do a check
			{
				// Set the buffer size
				float buffer = (widthFilter(cur_point.avg_neighbour_dist)*0.5f)+5;
				PVector line_rot = cur_line.copy().rotate(PApplet.HALF_PI).setMag(30);
				//PApplet.println("DIST: " + dist_from_line);
				
				p.strokeWeight(10);
				p.stroke(0,0,255);
				//p.point(cur_point.pos.x, cur_point.pos.y);
				//p.point(next_point.pos.x, next_point.pos.y);
				
				if(!Utilities.isPointLeftOfLine(cur_point.pos.x, cur_point.pos.y, cur_point.pos.x+line_rot.x, cur_point.pos.y+line_rot.y, x_input, y_input))
				{
					//p.line(cur_point.pos.x, cur_point.pos.y, cur_point.pos.x+line_rot.x,cur_point.pos.y+line_rot.y);
					if(Utilities.isPointLeftOfLine(next_point.pos.x, next_point.pos.y, next_point.pos.x+line_rot.x, next_point.pos.y+line_rot.y, x_input, y_input))
					{
						//p.line(next_point.pos.x, next_point.pos.y, next_point.pos.x+line_rot.x, next_point.pos.y+line_rot.y);
						float dist_from_line = Utilities.distToLine(cur_point.pos.x, cur_point.pos.y, next_point.pos.x, next_point.pos.y, x_input, y_input);
						float dist_along_line = Utilities.distToLine(cur_point.pos.x, cur_point.pos.y, cur_point.pos.x+line_rot.x, cur_point.pos.y+line_rot.y, x_input, y_input);
						distances[0] = dist_from_line;
						distances[1] = dist_along_line/cur_line.mag();
						distances[2] = 1 - distances[1];	
						//p.println("DIST :  " + dist_from_line);
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
	
	void checkMouseEvent(MouseEvent e)
	{
		int[] index_range = findPointsIndexRangeForFocus((int)a.current_frame-(int)start_frame,visible_range-(range_fade_amount/5));
		
		if(index_range[0] != -1) // If the line is being displayed
		{
			ArrayList<int[]> segments = withinRangeSegments(index_range[0], index_range[1], 50, e.getX(), e.getY());	
			
			if(segments.size() != 0) // If there are segments to check
			{
				int closest_index = -1;
				float closest_distance = -1;
				
				//p.println(segments.size());
				
				for(int[] segment: segments) // For each segment
				{
					//p.stroke(0);
					//p.strokeWeight(10);
					//p.line(points.get(segment[0]).pos.x, points.get(segment[0]).pos.y, points.get(segment[1]).pos.x, points.get(segment[1]).pos.y);
					
					//p.println("C IS " + segment[0] + " " + segment[1]);
					
					for(int c=segment[0]; c<segment[1]; c++) // step through individual strokes and check detection
					{	
						float[] check_result = mouseDistToStrokeSegment(c, e.getX(), e.getY());
						
						if(check_result[0] != -1 && check_result[0] != -2)
						{
							if(closest_distance == -1)
							{
								if(check_result[1] < 0.5)
								{
									closest_index = c;
								}
								else
								{
									closest_index = c+1;
								}
								closest_distance = check_result[0];
							}
							if(closest_distance >= check_result[0])
							{
								if(check_result[1] < 0.5)
								{
									closest_index = c;
								}
								else
								{
									closest_index = c+1;
								}
								closest_distance = check_result[0];
							}
						}
						else if(check_result[0] == -2) // A return value of -2 is given if the segment is too small to check, so instead we will do a distance check
						{
							float dist_to_e = PApplet.dist(points.get(c).pos.x, points.get(c).pos.y, e.getX(), e.getY());
							
							if(closest_distance >= dist_to_e || closest_distance == -1)
							{
								//p.println("SADASD!");
								closest_distance = dist_to_e;
								closest_index = c;
							}
							p.println(dist_to_e + " / " + closest_distance);
						}
					}
				}
				
				if(closest_index != -1)
				{
					if(closest_distance <15)
					{
						hover_focus = closest_index;
					}
					else
					{
						hover_focus = -1;
					}
					//p.println(closest_distance);
					//p.line(points.get(closest_index).pos.x, points.get(closest_index).pos.y,points.get(closest_index+1).pos.x, points.get(closest_index+1).pos.y);
					//p.ellipse(points.get(closest_index).pos.x, points.get(closest_index).pos.y, 15, 15);
				}
				//p.println("CLOSEST INDEX IS " + closest_index);
				p.println("CLOSEST INDEX: " + hover_focus + "/" + (points.size()-1));
			}
		}
	}
	
	ArrayList<int[]> withinRangeSegments(int start_range, int stop_range, int segment_length, float x_input, float y_input)
	{
		//cur_bounds.clear();
		
		ArrayList<int[]> segments = new ArrayList<int[]>();
		
		float dist_traversed = 0;
		
		int cur_segment_start = 0;
		float cur_segment_max_l = 0;
		float cur_segment_max_r = 0;
		float cur_segment_max_t = 0;
		float cur_segment_max_b = 0;
		int bounding_box_buffer = 10;
		
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
	
	void drawInk(int index_focus, int render_range, int edge_fade_amt, boolean show_all)
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
				float base_opacity = 30;
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
					p.stroke(100,opacity);
					p.strokeWeight(width);
					if(width < 20)
					{
						p.line(points.get(pt).pos.x, points.get(pt).pos.y, points.get(pt+1).pos.x, points.get(pt+1).pos.y);
					}
					
					if(width > 15)
					{
						p.noStroke();
						p.fill(100,opacity*0.75f);
						p.ellipse(points.get(pt).pos.x, points.get(pt).pos.y, width, width);
					}
				}
			}
		}
		drawCursor();
		//drawCursor2(points.size()-1);
	}
	
	float widthFilter(float width)
	{
		float result = width;
		
		result = (2/result)*80;
		
		if(result > max_ink_width)
		{
			result = max_ink_width;
		}
		return result;
	}
	
	void drawCursor2(int index)
	{
		int trail_length = 10;
		int start_index = index-trail_length;
		int end_index = index;
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
			
			//p.println(opacity);
			
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
	
	void drawStroke()
	{
		if(points != null && points.size() > 1)
		{
			for(int l=0; l<points.size()-1; l++)
			{	
				p.noStroke();
				p.fill(0,50);
				p.ellipse(points.get(l).pos.x, points.get(l).pos.y,10,10);
				
				p.noFill();
				p.stroke(0,50);
				p.strokeWeight(10);
				p.line(points.get(l).pos.x, points.get(l).pos.y, points.get(l+1).pos.x, points.get(l+1).pos.y);
			}
			drawCursor();
		}
	}
	
	PVector positionAtFrame(int frame_number)
	{
		if(frame_number >= start_frame && frame_number <= start_frame+points.size()-1)
		{
			return points.get(frame_number-(int)start_frame).pos;
		}
		else
		{
			return null;
		}
	}
	
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
