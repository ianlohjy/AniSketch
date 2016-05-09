
import java.util.ArrayList;

import jogamp.graph.geom.plane.Crossing.CubicCurve;
import processing.core.*;

public class Stroke {

	AniSketch p;
	long start_frame = 0;
	//long current_position = 0;
	ArrayList<StrokePoint> points;
	AnimationController a;
	
	Stroke(AniSketch p, AnimationController a)
	{
		this.p = p;
		this.a = a;
		points = new ArrayList<StrokePoint>();
	}
	
	public void update()
	{
		
	}
	
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
		drawInk((int)a.current_frame-(int)start_frame, false);
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
	
	void drawInk(int index_focus, boolean show_all)
	{
		int range = 250; // The number of points to render on either side of index focus
		int fade_dist = 50;
		//p.println(index_focus + " " + points.size());
		
		if(points != null && points.size() > 1)
		{
			int start_index = -1;
			int end_index = -1;
			
			if(show_all)
			{
				start_index = 0;
				end_index = points.size()-1;
			}
			else
			{
				if(index_focus <= 0) // If the index focus is before the stroke start
				{
					end_index = index_focus + range;
					if(end_index > 0) // If the object is in range
					{
						start_index = 0;
					}
					if(end_index > points.size()-1)
					{
						end_index = points.size()-1;
					}
				}
				else if(index_focus >= points.size()-1) // If the index focus is after the stroke end
				{
					start_index = index_focus-range;
					if(start_index < points.size()-1)
					{
						end_index = points.size()-1;
					}
					if(start_index < 0)
					{
						start_index = 0;
					}
				}
				else if(index_focus < points.size() && index_focus >= 0) // If the index focus is within the stroke
				{
					start_index = index_focus - range;
					end_index   = index_focus + range;
					
					if(start_index < 0)
					{
						start_index = 0;
					}
					if(end_index > points.size()-1)
					{
						end_index = points.size()-1;
					}
				}
			}
			if(show_all || start_index != -1)
			{
				float base_opacity = 30;
				int fade_in_limit  = index_focus-range+fade_dist;
				int fade_out_limit = index_focus+range-fade_dist;
				
				p.println("FADE OUT AT INDEX " + fade_out_limit);
				
				for(int pt=start_index; pt<end_index; pt++)
				{
					float width = widthFilter(points.get(pt).avg_neighbour_dist);
					float opacity = base_opacity;
					
					if(fade_in_limit >= 0 && pt < fade_in_limit)
					{
						opacity = -base_opacity * (((float)(fade_in_limit-pt)/fade_dist)-1);
					}					
					if(fade_out_limit < points.size() && pt > fade_out_limit)
					{
						opacity = -base_opacity * (((float)(pt-fade_out_limit)/fade_dist)-1);
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
		
		if(result > 40)
		{
			result = 40;
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
			
			p.println(opacity);
			
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
