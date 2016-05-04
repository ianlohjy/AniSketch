import processing.core.*;

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
	
	public static void printError(String message)
	{
		System.err.println("<ERROR> " + message);
	}
	
	public static void printAlert(String message)
	{
		System.err.println("<ALERT> " + message);
	}
	
	public static float gaussian1d(float position, float offset, float deviation)
	{
		// Gaussian function from https://en.wikipedia.org/wiki/Gaussian_function
		// Returns the value (0-1) at 'position'
		// Offset moves the center of the gaussian curve
		// Deviation is the width of 1 standard deviation (You should expect close to 0 by the 3rd or 4th deviation [+-3s/+-4s] )
		return PApplet.exp(-(((position-offset)*(position-offset))/(2*(deviation)*(deviation))));
	}
	
	public static float[] randomColorPallete()
	{
		float lightness = (float)(0.75+(Math.random()*0.5));
		float google_blue[] = {lightness*66,lightness*133,lightness*244}; // Google Blue
		float google_green[] = {lightness*52,lightness*168,lightness*83}; // Google Green
		float google_yellow[] = {lightness*251,lightness*188,lightness*5}; // Google Yellow
		float google_red[] = {lightness*234,lightness*67,lightness*53}; // Google Red
		
		int selection = (int)(Math.random()*4);
		
		switch(selection)
		{
			case 0:  return google_blue;
			case 1:  return google_green;
			case 2:  return google_yellow;
			case 3:  return google_red;
			default: return google_blue;
		}
	}
	

}
