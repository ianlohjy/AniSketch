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
	
	public static float distToLine(float lx1, float ly1, float lx2, float ly2, float x, float y)
	{
	  // Based on the intuitive example from http://members.tripod.com/vector_applications/distance_point_line/
	  // We find the distance to the line using the pythagorean theorem (a^2 + b^2 = c^2)
	  
	  // Finding the hypothenuse
	  PVector hyp = new PVector(x, y);
	  hyp = hyp.sub(lx1,ly1);
	  float hyp_length = hyp.mag();

	  // Finding the second length of the triangle
	  PVector second_side = new PVector(lx2-lx1, ly2-ly1);
	  second_side = second_side.normalize();
	  float second_length = hyp.dot(second_side);
	    
	  // With the length of 2 sides, we can now calculate the last length (which is the perpendicular distance)
	  return PApplet.sqrt((hyp_length*hyp_length) - (second_length*second_length));
	}

	public static boolean isPointLeftOfLine(PVector a, PVector b, float x_input, float y_input)
	{
		return ((b.x - a.x)*(y_input - a.y) - (b.y - a.y)*(x_input - a.x)) > 0;
	}
	
	public static boolean isPointLeftOfLine(float ax, float ay, float bx, float by, float x_input, float y_input)
	{
		return ((bx - ax)*(y_input - ay) - (by - ay)*(x_input - ax)) > 0;
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
		float lightness = 1;//(float)(0.75+(Math.random()*0.5));
		float google_blue[] = {lightness*66,lightness*133,lightness*244}; // Google Blue
		float google_green[] = {lightness*52,lightness*168,lightness*83}; // Google Green
		float google_yellow[] = {lightness*251,lightness*188,lightness*5}; // Google Yellow
		float google_red[] = {lightness*234,lightness*67,lightness*53}; // Google Red
		float illustrator_orange[] = {lightness*255,lightness*123,lightness*25};
		
		int selection = (int)(Math.random()*4);
		
		switch(selection)
		{
			case 0:  return google_blue;
			case 1:  return google_green;
			case 2:  return illustrator_orange;//google_yellow;
			case 3:  return google_red;
			default: return google_blue;
		}
	}
	
	public static float[] cartesianToBarycentric(float x1, float y1, float x2, float y2, float x3, float y3, float x_input, float y_input)
	{
		float[] coords = {-1,-1,-1};
		
		boolean state1 = isPointLeftOfLine(x1, y1, x2, y2, x_input, y_input);
		boolean state2 = isPointLeftOfLine(x2, y2, x3, y3, x_input, y_input);
		boolean state3 = isPointLeftOfLine(x3, y3, x1, y1, x_input, y_input);
		
		if(state1 == state2 && state1 == state3)
		{
			float b1 = ((y2-y3)*(x_input-x3) + (x3-x2)*(y_input-y3)) / ((y2-y3)*(x1-x3) + (x3-x2)*(y1-y3));
			float b2 = ((y3-y1)*(x_input-x3) + (x1-x3)*(y_input-y3)) / ((y2-y3)*(x1-x3) + (x3-x2)*(y1-y3));
			float b3 = 1 - b1 - b2;
			
			coords[0] = b1;
			coords[1] = b2;
			coords[2] = b3;
		}
		return coords;	
	}

}
