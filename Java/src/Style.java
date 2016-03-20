import processing.core.*;

public class Style
	{
		boolean no_stroke;
		boolean no_fill;
		float[] stroke;
		float stroke_weight;
		float[] fill;
		PApplet p;
		
		Style(PApplet p)
		{
			this.p = p;
			no_stroke = false;
			stroke = new float[4];
			fill = new float[4];
			no_fill = false;
		}
		
		void apply()
		{
			if(no_stroke)
			{
				p.noStroke();
			}
			else 
			{
				p.strokeWeight(stroke_weight);
				p.stroke(stroke[0],stroke[1],stroke[2],stroke[3]);
			}
			if(no_fill)
			{
				p.noFill();
			}
			else 
			{
				p.fill(fill[0],fill[1],fill[2],fill[3]);
			}
		}
		
		void stroke(float sr, float sg, float sb, float sa)
		{
			stroke[0] = sr;
			stroke[1] = sg;
			stroke[2] = sb;
			stroke[3] = sa;
		}
		
		void noStroke()
		{
			no_stroke = true;
		}
		
		void strokeWeight(float sw)
		{
			stroke_weight = sw;
			no_stroke = false;
		}
		
		void fill(float fr, float fg, float fb, float fa)
		{
			fill[0] = fr;
			fill[1] = fg;
			fill[2] = fb;
			fill[3] = fa;
			no_fill = false;
		}
		
		void noFill()
		{
			no_fill = true;
		}
		
		void setDefault()
		{
			fill(150,150,150,255);
			stroke(0,0,0,255);
		}
	}