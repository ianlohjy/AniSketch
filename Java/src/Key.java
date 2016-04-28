import java.util.ArrayList;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class Key {

	float x;
	float y;
	float d;
	float deviation_width;
	
	AniSketch p;
	ArrayList<DeltaData> deltas;
	
	float[][] shape;
	
	Key(float x, float y, float d, AniSketch p)
	{
		this.x = x;
		this.y = y;
		this.d = d;
		this.p = p;
		
		deltas = new ArrayList<DeltaData>();
		cacheCircle(24);
	}
	
	public void cacheCircle(int sides)
	{
		shape = new float[sides+1][2];
		
		for(int a=0; a<=sides; a++)
		{
			shape[a][0] = PApplet.cos(PApplet.radians(360/sides*a));
			shape[a][1] = PApplet.sin(PApplet.radians(360/sides*a));
		}
	}
	
	public void update()
	{
		
	}
	
	public void draw()
	{
		p.fill(255,0,0,255);
		
		p.noStroke();
		p.beginShape(p.TRIANGLE_FAN);
		//p.stroke(0,255);
		p.vertex(x,y);
		
		p.fill(255,0,0,0);
		//p.stroke(0,0);
		for(float[] point: shape)
		{
			p.vertex((d/2*point[0]) + x,(d/2*point[1]) + y);
		}
		p.endShape();
		
		//p.ellipse(x, y, d, d);
	}
	
	public float getWeight(float x_input, float y_input)
	{
		return Utilities.gaussian1d(x_input, this.x, this.d/6f) * Utilities.gaussian1d(y_input, this.y, this.d/6f);
	}
	
	public void checkMouseEvent(MouseEvent e)
	{
		p.println(getWeight(e.getX(), e.getY()));
	}
	
	class DeltaData
	{
		
		Primitive primitive;
		float x, y, t, l, b, r, rotation;
		
		DeltaData()
		{
			this.x = 0;
			this.y = 0;
			this.l = 0;
			this.r = 0;
			this.t = 0;
			this.b = 0;
			this.rotation = 0;
		}
	}
}
