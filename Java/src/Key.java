import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Key {

	float x;
	float y;
	float d;
	float deviation_width;
	float[] color = new float[3];
	
	AniSketch p;
	ArrayList<DeltaData> deltas;
	
	float[][] shape;

	//==============//
	// MOUSE STATES //
	//==============//
	boolean pressed, hover, selected;
	
	//==================//
	// TRANSFORM STATES //
	//==================//
	// Transform States
	int transform_mode;
	final static int NONE = 0;
	final static int MOVE = 1;
	// Transform Data
	PVector transform_offset; // General purpose var for holding transform info

	
	Key(float x, float y, float d, AniSketch p)
	{
		this.x = x;
		this.y = y;
		this.d = d;
		this.p = p;
		
		deltas = new ArrayList<DeltaData>();
		color = Utilities.randomColorPallete();
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
	
	public void printDeltaData()
	{
		for(DeltaData data: deltas)
		{
			data.printDeltaData();
		}
	}
	
	public void mergeDeltasFromPrimitive(Primitive primitive)
	{
		DeltaData found_data = doesDeltaDataExistForPrimitive(primitive);
		
		if(found_data == null)
		{
			DeltaData new_data = new DeltaData(primitive);
			new_data.mergeCurrentStoredDeltas();
			deltas.add(new_data);
		}
		else if(found_data != null)
		{
			found_data.mergeCurrentStoredDeltas();
		}
	}
	
	public void removeDeltaDataForPrimitive(Primitive primitive)
	{
		for(DeltaData data: deltas)
		{
			DeltaData found_data = doesDeltaDataExistForPrimitive(primitive);
			
			if(found_data != null)
			{
				deltas.remove(deltas.indexOf(found_data));
			}
		}
	}
	
	// Checks if delta data for a primitive already exists
	public DeltaData doesDeltaDataExistForPrimitive(Primitive primitive)
	{
		for(DeltaData data: deltas)
		{
			if(data.matchesPrimitive(primitive))
			{
				return data;
			}
		}
		return null;
	}
	
	public void update()
	{
		
	}
	
	public void draw()
	{
		p.fill(color[0],color[1],color[2],255);
		
		p.noStroke();
		p.beginShape(p.TRIANGLE_FAN);
		//p.stroke(0,255);
		p.vertex(x,y);
		
		p.fill(color[0],color[1],color[2],0);
		//p.stroke(0,0);
		for(float[] point: shape)
		{
			p.vertex((d/2*point[0]) + x,(d/2*point[1]) + y);
		}
		p.endShape();
		
		if(hover || selected)
		{
			p.noFill();
			p.strokeWeight(2);
			p.stroke(0);
			p.ellipse(x,y,d,d);
		}
	}
	
	public float getWeight(float x_input, float y_input)
	{
		return Utilities.gaussian1d(x_input, this.x, this.d/6f) * Utilities.gaussian1d(y_input, this.y, this.d/6f);
	}
	
	public void doTranslate(float x_input, float y_input)
	{
		if(transform_mode == NONE) // If translate has not been started, initialise it
		{
			transform_offset  = new PVector(x_input-x, y_input-y);			
			transform_mode    = MOVE;
			
			PApplet.println("Started translate");
		}
		if(transform_mode == MOVE)
		{
			float amount_x = this.x - (x_input - transform_offset.x);
			float amount_y = this.y - (y_input - transform_offset.y);
			
			this.x = this.x - amount_x;
			this.y = this.y - amount_y;
		}
	}
	
	public void endTranslate(float x_input, float y_input)
	{
		if(transform_mode == MOVE)
		{
			transform_mode = NONE;
		}
	}
	
	public boolean withinBounds(float x_input, float y_input)
	{
		if(PApplet.dist(x_input, y_input, this.x, this.y) < d/2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		
		if(e.getAction() == 1)// Mouse Pressed
		{
			if(e.getButton() == 37) // If it was a left-click
			{
				if(within_bounds)
				{
					selected = true;
					p.main_windows.stage.goToActiveKey(this);
				}
				else
				{
					selected = false;
					p.main_windows.stage.exitActiveKey();
				}
			}
		}
		else if(e.getAction() == 2)// Mouse Released
		{
			if(e.getButton() == 37) // If it was a left-click
			{
				endTranslate(e.getX(), e.getY());
			}
		}
		else if(e.getAction() == 3) // Mouse Clicked
		{
		}
		else if(e.getAction() == 4) // Mouse Dragged
		{
			if(e.getButton() == 37) // If it was a left-click
			{
				if(within_bounds)
				{
					hover = true;
				}
				
				if(selected)
				{
					if(e.getButton() == 37)
					{
						doTranslate(e.getX(), e.getY());
					}	
				}
			}
		}
		else if(e.getAction() == 5) // When mouse is moved
		{
			if(within_bounds) 
			{
				hover = true;
			}
			else 
			{
				hover = false;
			}
		}
	}
	
	class DeltaData
	{
		
		Primitive primitive;
		float lx, ly, t, l, b, r, rotation;
		
		DeltaData(Primitive primitive)
		{
			this.lx = 0;
			this.ly = 0;
			this.l = 0;
			this.r = 0;
			this.t = 0;
			this.b = 0;
			this.rotation = 0;
			this.primitive = primitive;
		}
		
		boolean matchesPrimitive(Primitive primitive)
		{
			if(this.primitive == primitive)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		void mergeCurrentStoredDeltas()
		{
			
			this.lx += primitive.delta_local_x;
			this.ly += primitive.delta_local_y;
			this.l += primitive.delta_l;
			this.r += primitive.delta_r;
			this.t += primitive.delta_t;
			this.b += primitive.delta_b;
			this.rotation += primitive.delta_rotation;
			
			this.primitive.delta_local_x = 0;
			this.primitive.delta_local_y = 0;
			this.primitive.delta_l = 0;
			this.primitive.delta_r = 0;
			this.primitive.delta_t = 0;
			this.primitive.delta_b = 0;
			this.primitive.delta_rotation = 0;
		}
		
		void printDeltaData()
		{
			PApplet.println("DELTA DATA");
			PApplet.println("==========");	
			PApplet.println("LOCAL X  | " + this.lx);
			PApplet.println("LOCAL Y  | " + this.ly);
			PApplet.println("ROTATION | " + this.rotation);
			PApplet.println("TOP      | " + this.t);
			PApplet.println("BOTTOM   | " + this.b);
			PApplet.println("LEFT     | " + this.l);
			PApplet.println("RIGHT    | " + this.r);
			
		}
		
	}
}
