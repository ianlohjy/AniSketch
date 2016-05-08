
import java.util.ArrayList;

import jogamp.graph.geom.plane.Crossing.CubicCurve;
import processing.core.*;

public class Stroke {

	AniSketch p;
	long start_frame = 0;
	//long current_position = 0;
	ArrayList<PVector> points;
	AnimationController a;
	
	Stroke(AniSketch p, AnimationController a)
	{
		this.p = p;
		this.a = a;
		points = new ArrayList<PVector>();
	}
	
	public void update()
	{
		
	}
	
	void addPoint(float x, float y)
	{	
		points.add(new PVector(x,y));
	}

	
	void draw()
	{
		drawSkin();
	}
	
	void drawLine()
	{
		p.stroke(0);
		p.strokeWeight(10);
		
		if(points != null && points.size() > 1)
		{
			for(int l=0; l<points.size()-1; l++)
			{
				p.line(points.get(l).x, points.get(l).y, points.get(l+1).x, points.get(l+1).y);
			}
		}
	}
	
	void drawCursor()
	{
		p.noStroke();
		p.fill(0);
		
		if(a.current_frame >= start_frame && a.current_frame <= start_frame+points.size()-1 )
		{
			p.ellipse(points.get((int)(a.current_frame-start_frame)).x, points.get((int)(a.current_frame-start_frame)).y, 20f, 20f);
		}
	}
	
	// A skin is a live computed representation of the line, that looks visually pleasing but does not neccessarily use all the stroke's raw data.
	void drawSkin()
	{
		if(points != null && points.size() > 1)
		{
			//float min_dist = 10;
			///float dist_traversed = 0;
			//PVector segment_start = points.get(0);	
			//int num_neighbors = 0;
			int neighbours_to_check = 5;
			
			for(int pt=0; pt<points.size()-1; pt++)
			{
				float avg_neighbour_dist = 0;
				int neighbours_checked = 0;
				int index_to_check = pt;
				boolean checked_left = false;
				boolean checked_right = false;
				
				while(!checked_left)
				{
					index_to_check--;
					if(index_to_check < 0 || index_to_check < pt-neighbours_to_check)
					{
						checked_left = true;
						index_to_check = pt;
						break;
					}
					avg_neighbour_dist += PApplet.dist(points.get(pt).x, points.get(pt).y, points.get(index_to_check).x, points.get(index_to_check).y);
					neighbours_checked++;
				}
				
				while(!checked_right)
				{
					index_to_check++;
					if(index_to_check > points.size()-1 || index_to_check > pt+neighbours_to_check)
					{
						checked_right = true;
						index_to_check = pt;
						break;
					}
					avg_neighbour_dist += PApplet.dist(points.get(pt).x, points.get(pt).y, points.get(index_to_check).x, points.get(index_to_check).y);
					neighbours_checked++;
				}
				
				// Get the average ditance of the neighbours distance
				if(neighbours_checked != 0)
				{
					avg_neighbour_dist /= neighbours_checked;
				}
				
				float width = (2/avg_neighbour_dist)*100;
				if(width > 20)
				{
					width = 20;
				}
				
				p.noFill();
				p.stroke(100,50);
				p.strokeWeight(width);
				p.line(points.get(pt).x, points.get(pt).y, points.get(pt+1).x, points.get(pt+1).y);
				
				if(width > 10)
				{
					p.noStroke();
					p.fill(100,50);
					p.ellipse(points.get(pt).x, points.get(pt).y, width, width);
				}
				/*
				dist_traversed += PApplet.dist(segment_start.x, segment_start.y, points.get(pt).x, points.get(pt).y);
				
				if(dist_traversed >= min_dist || pt == points.size()-1)
				{
					p.noFill();
					p.stroke(0);
					p.strokeWeight((num_neighbors*5) + 1 );
					p.line(segment_start.x, segment_start.y, points.get(pt).x, points.get(pt).y);
					
					dist_traversed = 0;
					segment_start = points.get(pt);
					num_neighbors = 0;
				}
				else
				{
					num_neighbors ++; 
				}
				*/
			}
		}
		drawCursor();
	}
	
	void drawStroke()
	{
		if(points != null && points.size() > 1)
		{
			for(int l=0; l<points.size()-1; l++)
			{	
				p.noStroke();
				p.fill(0,50);
				p.ellipse(points.get(l).x, points.get(l).y,10,10);
				
				p.noFill();
				p.stroke(0,50);
				p.strokeWeight(10);
				p.line(points.get(l).x, points.get(l).y, points.get(l+1).x, points.get(l+1).y);
			}

			drawCursor();
		}
	}
	
	PVector positionAtFrame(int frame_number)
	{
		if(frame_number >= start_frame && frame_number <= start_frame+points.size()-1)
		{
			return points.get(frame_number-(int)start_frame);
		}
		else
		{
			return null;
		}
	}
	
	class Segment
	{
		float width = 0;
		float max_width = 30;
		
		float start_point_index = 0;
		float end_point_index = 0;
		
		Segment()
		{
			
		}
	}
	
}
