
import java.util.ArrayList;
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
	
	void drawCursor()
	{
		p.noStroke();
		p.fill(0);
		
		if(a.current_frame >= start_frame && a.current_frame <= start_frame+points.size()-1 )
		{
			p.ellipse(points.get((int)(a.current_frame-start_frame)).x, points.get((int)(a.current_frame-start_frame)).y, 20f, 20f);
		}
		
	}
	
	void drawLine()
	{
		
		p.stroke(0);
		p.strokeWeight(10);
		
		if(points != null && points.size()>1)
		{
			for(int l=0; l<points.size()-1; l++)
			{
				p.line(points.get(l).x, points.get(l).y, points.get(l+1).x, points.get(l+1).y);
			}
		}
	}
	
	void drawSimple()
	{
		p.stroke(0,50);
		p.strokeWeight(10);
		p.noFill();
		
		if(points != null)
		{
			if(points.size()>1 && points.size()<4)
			{
				for(int l=0; l<points.size()-1; l++)
				{	
					p.line(points.get(l).x, points.get(l).y, points.get(l+1).x, points.get(l+1).y);
				}
			}
			else if(points.size() >= 4)
			{
				p.beginShape();
				
				for(int l=0; l<points.size(); l++)
				{	
					p.curveVertex(points.get(l).x, points.get(l).y);
				}
				p.endShape();
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
	
}
