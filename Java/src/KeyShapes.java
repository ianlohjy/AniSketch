import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

// A Key Shape is a series of keys combined together
public class KeyShapes {
	
	AniSketch p;
	ArrayList<KeyTri> keytris;
	ArrayList<KeyLine> keylines;
	
 	KeyShapes(AniSketch p)
	{
		this.p = p;
		keylines = new ArrayList<KeyLine>();
		keytris  = new ArrayList<KeyTri>();
	}
	
	void draw()
	{
		drawKeyTris();
		drawKeyLines();
	}
	
	void drawKeyLines()
	{
		for(KeyLine keyline: keylines)
		{
			keyline.draw();
		}
	}
	
	void drawKeyTris()
	{
		for(KeyTri keytri: keytris)
		{
			keytri.draw();
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
	
	void disconnectKeys(Key key1, Key key2)
	{
		KeyLine found_keyline = connectionExists(key1, key2);
		
		if(found_keyline != null)
		{
			keylines.remove(found_keyline);
		}
		
		for(int kt=0; kt<keytris.size(); kt++)
		{
			if(keytris.get(kt).containsBoth(key1, key2))
			{
				for(KeyLine keyline: keylines)
				{
					if(keyline.associated_tris.contains(keytris.get(kt)))
					{
						keyline.associated_tris.remove(keytris.get(kt));
					}
				}
				keytris.remove(kt);
				kt--;
			}
		}
	}
	
	KeyLine connectKeys(Key key1, Key key2)
	{
		// If the this connection does not exist
		KeyLine found_keyline = connectionExists(key1, key2);
		
		if(found_keyline == null)
		{
			KeyLine new_keyline = new KeyLine(key1, key2);
			keylines.add(new_keyline);
			checkConnectionForTriangles(new_keyline);
			return new_keyline;
		}
		else
		{
			return found_keyline;
		}
	}
	
	void checkConnectionForTriangles(KeyLine keyline)
	{
		for(Key key1_connection: keyline.key1.connections)
		{
			for(Key key1_connection_2: key1_connection.connections)
			{
				// If key1's adjacent connections also contain key1
				if(key1_connection_2.connections.contains(keyline.key1))
				{
					KeyTri found_keytri = triangleExists(keyline.key1, key1_connection, key1_connection_2);
					
					if(found_keytri == null)
					{
						keytris.add(new KeyTri(keyline.key1, key1_connection, key1_connection_2));
					}
				}
			}
		}
	}
	
	KeyTri triangleExists(Key key1, Key key2, Key key3)
	{
		for(KeyTri keytri: keytris)
		{
			if(keytri.contains(key1, key2, key3))
			{
				return keytri;
			}
		}
		return null;
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
			
			// For keyline.key1
			boolean found_key1 = false;
			for(KeyWeight weight: key_weights)
			{
				if(weight.contains(keyline.key1))
				{
					weight.updateWeightIfHigher(keyline_weight[0]);
					found_key1 = true;
					break;
				}
			}
			if(!found_key1)
			{
				KeyWeight key1_weight = new KeyWeight(keyline.key1);
				key1_weight.setWeight(keyline_weight[0]);
				key_weights.add(key1_weight);
			}
			
			// For keyline.key2
			boolean found_key2 = false;
			for(KeyWeight weight: key_weights)
			{
				if(weight.contains(keyline.key2))
				{
					weight.updateWeightIfHigher(keyline_weight[1]);
					found_key2 = true;
					break;
				}
			}
			if(!found_key2)
			{
				KeyWeight key2_weight = new KeyWeight(keyline.key2);
				key2_weight.setWeight(keyline_weight[1]);
				key_weights.add(key2_weight);
			}
		}
		
		// For all key tris
		for(KeyTri keytri: keytris)
		{
			float[] keytri_weight = keytri.getWeights(x_input, y_input);
			
			if(keytri_weight[0] != -1)
			{
				boolean checked_key1 = false;
				boolean checked_key2 = false;
				boolean checked_key3 = false;
				
				// For keytri.key1
				boolean found_key1 = false;
				for(KeyWeight weight: key_weights)
				{
					if(weight.contains(keytri.key1))
					{
						weight.updateWeightIfHigher(keytri_weight[0]);
						found_key1 = true;
						break;
					}
				}
				if(!found_key1)
				{
					KeyWeight key1_weight = new KeyWeight(keytri.key1);
					key1_weight.setWeight(keytri_weight[0]);
					key_weights.add(key1_weight);
				}
				
				// For keytri.key2
				boolean found_key2 = false;
				for(KeyWeight weight: key_weights)
				{
					if(weight.contains(keytri.key2))
					{
						weight.updateWeightIfHigher(keytri_weight[1]);
						found_key2 = true;
						break;
					}
				}
				if(!found_key2)
				{
					KeyWeight key2_weight = new KeyWeight(keytri.key2);
					key2_weight.setWeight(keytri_weight[1]);
					key_weights.add(key2_weight);
				}
				
				// For keytri.key3
				boolean found_key3 = false;
				for(KeyWeight weight: key_weights)
				{
					if(weight.contains(keytri.key3))
					{
						weight.updateWeightIfHigher(keytri_weight[2]);
						found_key3 = true;
						break;
					}
				}
				if(!found_key3)
				{
					KeyWeight key3_weight = new KeyWeight(keytri.key3);
					key3_weight.setWeight(keytri_weight[2]);
					key_weights.add(key3_weight);
				}
			}
			
			for(KeyWeight weights: key_weights)
			{
				p.println(weights.key.toString() + " " + weights.weight);
			}
		}
		
		/*
		p.println("NUM KEYWEIGHTS: " + key_weights.size());
		for(KeyWeight weights: key_weights)
		{
			p.println(weights.key.toString() + " " + weights.weight);
		}*/
		
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
		
		boolean updateWeightIfHigher(float value)
		{
			if(this.weight < value)
			{
				setWeight(value);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	class KeyTri
	{
		Key key1;
		Key key2;
		Key key3;
		
		KeyLine keyline1;
		KeyLine keyline2;
		KeyLine keyline3;
		
		KeyTri(Key key1, Key key2, Key key3)
		{
			this.key1 = key1;
			this.key2 = key2;
			this.key3 = key3;
			
			// Find the associated keylines for easy reference
			this.keyline1 = connectionExists(key1, key2);
			this.keyline2 = connectionExists(key2, key3);
			this.keyline3 = connectionExists(key3, key1);
			
			if(!keyline1.associated_tris.contains(this))
			{
				keyline1.associated_tris.add(this);
			}
			if(!keyline2.associated_tris.contains(this))
			{
				keyline2.associated_tris.add(this);
			}
			if(!keyline3.associated_tris.contains(this))
			{
				keyline3.associated_tris.add(this);
			}
		}
		
		void draw()
		{
			p.noStroke();

			p.beginShape(p.TRIANGLES);
			p.fill(key1.color[0]+100, key1.color[1]+100, key1.color[2]+100, 255);
			p.vertex(key1.x, key1.y);
			p.fill(key2.color[0]+100, key2.color[1]+100, key2.color[2]+100, 255);
			p.vertex(key2.x, key2.y);
			p.fill(key3.color[0]+100, key3.color[1]+100, key3.color[2]+100, 255);
			p.vertex(key3.x, key3.y);
			p.endShape();
			
			//float[] weights = getWeights(p.mouseX, p.mouseY);
			
			//p.println(withinBounds(p.mouseX, p.mouseY));
			
		}
		
		boolean withinBounds(float x_input, float y_input)
		{
			boolean state1 = Utilities.isPointLeftOfLine(key1.x, key1.y, key2.x, key2.y, x_input, y_input);
			boolean state2 = Utilities.isPointLeftOfLine(key2.x, key2.y, key3.x, key3.y, x_input, y_input);
			boolean state3 = Utilities.isPointLeftOfLine(key3.x, key3.y, key1.x, key1.y, x_input, y_input);
			
			if(state1 == state2 && state1 == state3)
			{
				return true;
			}
			return false;
		}
		
		float[] getWeights(float x_input, float y_input)
		{
			return Utilities.cartesianToBarycentric(key1.x, key1.y, key2.x, key2.y, key3.x, key3.y, x_input, y_input);
		}
		
		boolean contains(Key key1, Key key2, Key key3)
		{
			ArrayList<Key> contained_keys = new ArrayList<Key>();
			contained_keys.add(this.key1);
			contained_keys.add(this.key2);
			contained_keys.add(this.key3);
			
			if(contained_keys.contains(key1) && contained_keys.contains(key2) && contained_keys.contains(key3))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		boolean containsBoth(Key key1, Key key2)
		{
			ArrayList<Key> contained_keys = new ArrayList<Key>();
			contained_keys.add(this.key1);
			contained_keys.add(this.key2);
			contained_keys.add(this.key3);
			
			if(contained_keys.contains(key1) && contained_keys.contains(key2))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		Key findOtherKey(Key key1, Key key2)
		{
			if(this.key1 != key1 && this.key1 != key2)
			{
				return this.key1;
			}
			else if(this.key2 != key1 && this.key2 != key2)
			{
				return this.key2;
			}
			else if(this.key3 != key1 && this.key3 != key2)
			{
				return this.key3;
			}
			else
			{
				return null;	
			}
		}
	}
	
	class KeyLine
	{
		Key key1;
		Key key2;
		
		ArrayList<KeyTri> associated_tris;
		
		KeyLine(Key key1, Key key2)
		{
			this.key1 = key1;
			this.key2 = key2;
			this.associated_tris = new ArrayList<KeyTri>();
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
					
					float diameter = PApplet.lerp(key1.d, key2.d, ratio); 
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
			boolean has_tri_on_left = false;
			boolean has_tri_on_right = false;
			
			// First we need to check the line is part of a triangle
			for(KeyTri tri: associated_tris)
			{
				Key other_key = tri.findOtherKey(this.key1, this.key2);
				
				boolean is_left = Utilities.isPointLeftOfLine(key1.x, key1.y, key2.x, key2.y, other_key.x, other_key.y);
				
				if(is_left)
				{
					has_tri_on_left = true;
				}
				else
				{
					has_tri_on_right = true;
				}
			}
			
			PVector line = new PVector(key2.x, key2.y);
			line = line.sub(key1.x, key1.y);
			line = line.rotate(p.HALF_PI);
			line = line.normalize();
			
			PVector key1_disp = line.copy().setMag(key1.d/2);
			PVector key2_disp = line.copy().setMag(key2.d/2);
			
			//float angle = line.heading() + p.HALF_PI;
			
			if(!has_tri_on_left)
			{
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
			}
			
			if(!has_tri_on_right)
			{
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
			}
		}	
	}
	
	
}
