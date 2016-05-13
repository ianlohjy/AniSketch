import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

// A Key Shape is a series of keys combined together
public class KeyShapes {
	
	AniSketch p;
	//ArrayList<KeyTri> keytris;
	ArrayList<KeyLine> keylines;
	
 	KeyShapes(AniSketch p)
	{
		this.p = p;
		keylines = new ArrayList<KeyLine>();
	}
	
	void draw()
	{
		drawKeyLines();
	}
	
	void drawKeyLines()
	{
		for(KeyLine keyline: keylines)
		{
			keyline.draw();
		}
	}
	
	/*
	KeyShape mergeWith(KeyShape keyshape)
	{
		for(keyS)
		{
			
		}
	}
	*/
	
	KeyLine connectKeys(Key key1, Key key2)
	{
		// If the this connection does not exist
		KeyLine found_keyline = connectionExists(key1, key2);
		
		if(found_keyline == null)
		{
			KeyLine new_keyline = new KeyLine(key1, key2);
			keylines.add(new_keyline);
			return new_keyline;
		}
		else
		{
			return found_keyline;
		}
	}
	
	KeyLine connectionExists(Key key1, Key key2)
	{
		for(KeyLine keyline: keylines)
		{
			if(keyline.key1 == key1 && keyline.key2 == key2)
			{
				return keyline;
			}
			else if(keyline.key1 == key2 && keyline.key2 == key1)
			{
				return keyline;
			}
		}
		return null;
	}
	
	ArrayList<KeyWeight> compileWeights(float x_input, float y_input)
	{
		ArrayList<KeyWeight> key_weights = new ArrayList<KeyWeight>();
		
		// For all key lines
		for(KeyLine keyline: keylines)
		{
			float[] keyline_weight = keyline.getWeights(x_input, y_input);
			boolean checked_key1 = false;
			boolean checked_key2 = false;
			
			for(int k=0; k<key_weights.size(); k++)
			{
				// If the new key weight values are larger than the current weight, replace it
				// For key1
				if(key_weights.get(k).contains(keyline.key1))
				{
					if(key_weights.get(k).weight < keyline_weight[0])
					{
						key_weights.get(k).setWeight(keyline_weight[0]);
						checked_key1 = true;
					}
				}
				// For key2
				if(key_weights.get(k).contains(keyline.key1))
				{
					if(key_weights.get(k).weight < keyline_weight[1])
					{
						key_weights.get(k).setWeight(keyline_weight[1]);
						checked_key2 = true;
					}
				}
			}
			
			// If both keys have been found and checked, exit the loop
			if(checked_key1 && checked_key2)
			{
				break;
			}
			// If we have checked all key weights and no key weights were checked/found
			if(!checked_key1)
			{
				KeyWeight key1_weight = new KeyWeight(keyline.key1);
				key1_weight.setWeight(keyline_weight[0]);
				key_weights.add(key1_weight);
			}
			if(!checked_key2)
			{
				KeyWeight key2_weight = new KeyWeight(keyline.key2);
				key2_weight.setWeight(keyline_weight[1]);
				key_weights.add(key2_weight);
			}
			
		}
		return key_weights;
	}
	
	boolean contains(Key key)
	{
		for(KeyLine keyline: keylines)
		{
			if(keyline.key1 == key || keyline.key2 == key)
			{
				return true;
			}
		}
		return false;
	}
	
	void getWeights(float input_x, float input_y)
	{
		for(KeyLine keyline: keylines)
		{
			keyline.getWeights(input_x, input_y);
		}
	}
	
	class KeyWeight
	{
		// The KeyWeight is purely for holding data while compiling weights for the shape
		float weight;
		Key key;
		
		KeyWeight(Key key)
		{
			weight = 0;
			this.key = key;
		}
		
		void setWeight(float value)
		{
			this.weight = value;
		}
		
		boolean contains(Key key)
		{
			if(this.key == key)
			{
				return true;
			}
			return false;
		}
	}
	
	class KeyLine
	{
		Key key1;
		Key key2;
		
		KeyLine(Key key1, Key key2)
		{
			this.key1 = key1;
			this.key2 = key2;
		}
		
		float[] getWeights(float x_input, float y_input)
		{
			// Return the weights of both keys as a float array
			float[] weights = {0,0};
			
			PVector line = new PVector(key2.x, key2.y);
			line = line.sub(key1.x, key1.y);
			line = line.rotate(p.HALF_PI);
			line = line.normalize();
			
			if(!Utilities.isPointLeftOfLine(new PVector(key1.x, key1.y), line.copy().add(key1.x, key1.y), x_input, y_input))
			{
				//p.println("ASDASDA");
				if(Utilities.isPointLeftOfLine(new PVector(key2.x, key2.y), line.copy().add(key2.x, key2.y), x_input, y_input))
				{
					float dist_between_keys = PApplet.dist(key1.x, key1.y, key2.x, key2.y);
					float dist_to_key1 = Utilities.distToLine(key1.x, key1.y, key1.x+line.x, key1.y+line.y, x_input, y_input);
					float ratio = dist_to_key1/dist_between_keys;
					float dist_from_line = Utilities.distToLine(key1.x, key1.y, key2.x, key2.y, x_input, y_input);
					
					float diameter = PApplet.lerp(key2.d, key1.d, ratio); 
					
					// Correct weighting of line
					float line_weight = Utilities.gaussian1d(dist_from_line, 0, diameter/6f);
					
					weights[0] = (1-ratio)*line_weight; 
					weights[1] =	ratio*line_weight;	
					//p.println(dist_to_key1);
				}
				else
				{
					//p.println("OUTSIDE AT KEY2");
					weights[1] = key2.getWeight(x_input, y_input);
				}
			}
			else
			{
				//p.println("OUTSIDE AT KEY1");
				weights[0] = key1.getWeight(x_input, y_input);
			}
			
			//PApplet.println("\n");
			//PApplet.println("KEY1 : " + weights[0]);
			//PApplet.println("KEY2 : " + weights[1]);
			return weights;
		}
		
		void draw()
		{ 
			PVector line = new PVector(key2.x, key2.y);
			line = line.sub(key1.x, key1.y);
			line = line.rotate(p.HALF_PI);
			line = line.normalize();
			
			PVector key1_disp = line.copy().setMag(key1.d/2);
			PVector key2_disp = line.copy().setMag(key2.d/2);
			
			//float angle = line.heading() + p.HALF_PI;
			p.beginShape(p.QUADS);
			
			p.fill(key1.color[0]+100, key1.color[1]+100, key1.color[2]+100, 255);
			p.vertex(key1.x, key1.y);
			
			p.fill(key1.color[0]+180, key1.color[1]+180, key1.color[2]+180);
			p.vertex(key1.x+key1_disp.x, key1.y+key1_disp.y);
			
			p.fill(key2.color[0]+180, key2.color[1]+180, key2.color[2]+180);
			p.vertex(key2.x+key2_disp.x, key2.y+key2_disp.y);
			
			p.fill(key2.color[0]+100, key2.color[1]+100, key2.color[2]+100, 255);
			p.vertex(key2.x, key2.y);
			
			p.endShape();
			
			p.beginShape(p.QUADS);
			
			p.fill(key1.color[0]+100, key1.color[1]+100, key1.color[2]+100, 255);
			p.vertex(key1.x, key1.y);
			
			p.fill(key1.color[0]+180, key1.color[1]+180, key1.color[2]+180);
			p.vertex(key1.x-key1_disp.x, key1.y-key1_disp.y);
			
			p.fill(key2.color[0]+180, key2.color[1]+180, key2.color[2]+180);
			p.vertex(key2.x-key2_disp.x, key2.y-key2_disp.y);
			
			p.fill(key2.color[0]+100, key2.color[1]+100, key2.color[2]+100, 255);
			p.vertex(key2.x, key2.y);
			
			p.endShape();
			
			
			
			//p.stroke(0);
			//p.strokeWeight(6);
			//p.line(key1.x, key1.y, key2.x, key2.y);
		}	
	}
	
	
}
