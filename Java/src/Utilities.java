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
		try{
			return ((b.x - a.x)*(y_input - a.y) - (b.y - a.y)*(x_input - a.x)) > 0;
		}
		catch(Exception e)
		{
			return false;
		}
		
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
		// deviation = deviation * 2.35482f;
		return PApplet.exp(-(((position-offset)*(position-offset))/(2*(deviation)*(deviation))));
	}
	
	public static int[] randomColorPallete()
	{
		// Colours have been taken from the Google Material Design Specification
		// @ https://www.google.com/design/spec/style/color.html#color-color-palette
		
		int[][] colours = new int[9][3];
		
		int[] red     = {244,67,54 };
		int[] teal    = {0,150,136 };
		int[] blue    = {30,136,229};
		int[] orange  = {255,87,34 };
		int[] green   = {76,175,80 };
		int[] l_blue  = {41,182,246};
		int[] amber   = {255,150,7 }; // This has been adjusted for blending
		int[] l_green = {139,195,74};
		int[] b_grey  = {96,125,139};
		
		colours[0] = red;
		colours[1] = teal;
		colours[2] = blue;
		colours[3] = orange;
		colours[4] = green;
		colours[5] = l_blue;
		colours[6] = amber;
		colours[7] = l_green;
		colours[8] = b_grey;
		
		int selection = (int)(Math.random()*colours.length);
		
		switch(selection)
		{
			case 0:  return colours[0];
			case 1:  return colours[1];
			case 2:  return colours[2];
			case 3:  return colours[3];
			case 4:  return colours[4];
			case 5:  return colours[5];
			case 6:  return colours[6];
			case 7:  return colours[7];
			case 8:  return colours[8];
			default: return colours[0];
		}
		
		/*
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
		*/
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
	
	public static boolean withinBounds(float x, float y, float w, float h, float x_input, float y_input)
	{
		if(x_input > x && x_input < (x+w))
		{
			if(y_input > y && y_input < (y+h))
			{
				return true;
			}
		}
		return false;
	}

	public static float[] RBGtoHSL(int r, int g, int b)
	{
		// From https://www.wikiwand.com/en/HSL_and_HSV#/General_approach
		
		float _r = (float)r/255;
		float _g = (float)g/255;
		float _b = (float)b/255;
		
		float M = Math.max(_r, Math.max(_g, _b));
		float m = Math.min(_r, Math.min(_g, _b));
		float C = M-m;
		
		float _H = 0;
		
		if(C == 0)
		{
			_H = 0; // Undefined colour, since all values are balanced (achromatic)
		}
		else if(M == _r)
		{
			_H = ((_g-_b)/C) % 6;
		}
		else if(M == _g)
		{
			_H = ((_b-_r)/C) + 2;
		}
		else if(M == _r)
		{
			_H = ((_r-_g)/C) + 4;
		}
		
		float H = 60*_H;
		float L = (M+m)/2;
		float S = 0;

		if(L == 1)
		{
			S = 0;
		}
		else
		{
			S = C/(1-Math.abs(2*L-1));
		}
		
		float[] hsl = {H, S, L};
		return hsl;
	}
	
	public static int[] HSLtoRGB(float h, float s, float l)
	{
		// From https://www.wikiwand.com/en/HSL_and_HSV#/General_approach
		
		float C  = (1 - Math.abs(2*l-1)) * s;
		float _H = h/60;
		float X  = C * (1 - Math.abs(_H % 2 - 1));
		
		float _R = 0;
		float _G = 0;
		float _B = 0;
		
		//if   (h == 0)           { _R=0; _G=0; _B=0; }
		if     (_H >=0 && _H <=1) { _R=C; _G=X; _B=0; }
		else if(_H >=1 && _H <=2) { _R=X; _G=C; _B=0; }
		else if(_H >=2 && _H <=3) { _R=0; _G=C; _B=X; }
		else if(_H >=3 && _H <=4) { _R=0; _G=X; _B=C; }
		else if(_H >=4 && _H <=5) { _R=X; _G=0; _B=C; }
		else if(_H >=5 && _H <=6) { _R=C; _G=0; _B=X; }
		
		float m = l-(C/2);
		int[] rgb = {(int)((_R+m)*255) , (int)((_G+m)*255), (int)((_B+m)*255)};
		
		return rgb;
	}
	
}
