import processing.core.PApplet;
import processing.core.PVector;

public class Utilities {

	public Utilities()
	{
	}
	
	public static void dottedLine(float x1, float y1, float x2, float y2, float stroke, float spacing, PApplet p)
	{
		PVector stroke_vector = new PVector(x2-x1, y2-y1);
		stroke_vector.setMag(stroke);
		
		PVector space_vector = stroke_vector.copy();
		space_vector.setMag(spacing);
		
		float distance = PApplet.dist(x1, y1, x2, y2);
		float distance_travelled = 0; 
		
		PVector place_point = new PVector(x1,y1);
		
		while(distance_travelled < distance)
		{
			p.line(place_point.x, place_point.y, place_point.x+stroke_vector.x, place_point.y+stroke_vector.y);
			place_point = place_point.add(stroke_vector);
			place_point = place_point.add(space_vector);
			
			distance_travelled += stroke_vector.mag();
			distance_travelled += space_vector.mag();
		}	
	}
	
}
