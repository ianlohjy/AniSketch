import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;

public class AnimationController {

	static final int framerate = 25; 
	static int millis_per_frame = 1000/framerate;
	
	static final int PAUSE = 0;
	static final int PLAY = 1;
	
	int playback = PAUSE;
	long frame_range[] = {0, 100};
	long current_frame = 0;		
	long last_checked_time = System.currentTimeMillis();
	
	AniSketch p;
	
	// Strokes
	ArrayList<Stroke> strokes;
	Stroke recorded_stroke;

	boolean recording_stroke = false;
	boolean was_playing = false;
	float last_recording_input_x = 0;
	float last_recording_input_y = 0;
	ArrayList<Key> delta_keys;
	
	KeyShapes keyshapes;
	Key default_key;
	
	boolean showing_compiled_keys = false;
	
	public AnimationController(AniSketch p) 
	{
		this.p = p;
		strokes = new ArrayList<Stroke>();
		keyshapes = new KeyShapes(p);
		delta_keys = new ArrayList<Key>();
		default_key = new Key(0, 0, 0, p);
	}

	public Key compileDeltaKeys(float x_input, float y_input)
	{
		Key compiled_key = new Key(0,0,0,p);
		float highest_weight = 0;
		
		ArrayList<KeyShapes.KeyWeight> key_weights = keyshapes.compileWeights(x_input, y_input);
		
		// For all delta keys
		for(Key delta_key: delta_keys)
		{
			// If the delta key has no connections (ie. if its not part of a bigger shape)
			if(delta_key.connections.size() == 0)
			{
				if(delta_key.getWeight(x_input, y_input) > highest_weight)
				{
					highest_weight = delta_key.getWeight(x_input, y_input);
				}
				// For all data in delta keys
				for(Key.PrimitiveData primitive_data: delta_key.primitive_data)
				{ 
					// Add the primitive data contained in the delta key, multiplied by the delta key's weighting
					compiled_key.addPrimitiveData(primitive_data.mult(delta_key.getWeight(x_input, y_input)));
				}
			}
		}
		
		// For Keyshapes
		for(KeyShapes.KeyWeight delta_key: key_weights)
		{
			// For all data in delta keys
			for(Key.PrimitiveData primitive_data: delta_key.key.primitive_data)
			{ 
				// Add the primitive data contained in the delta key, multiplied by the delta key's weighting
				compiled_key.addPrimitiveData(primitive_data.mult(delta_key.weight));
			}
		}
		
		/*
		float multiplier = 1f/highest_weight;
		
		if(highest_weight < 0.0000000000000000000001)
		{
			multiplier = 1;
		}
		
		p.println("HIGHEST WEIGHT IS : " + highest_weight + ", " + multiplier + ", " + highest_weight*multiplier);
		
		for(Key.PrimitiveData data: compiled_key.primitive_data)
		{
			data.set(data.mult(multiplier));
		}
		*/
		return compiled_key;
	}
	
	// KeyShape Handling
	
	public void disconnectKeys(Key key1, Key key2)
	{
		if(key1.connections.contains(key2))
		{
			key1.connections.remove(key2);
		}
		
		if(key2.connections.contains(key1))
		{
			key2.connections.remove(key1);
		}
		
		keyshapes.disconnectKeys(key1, key2);
	}
	
	public void connectKeys(Key key1, Key key2)
	{
		boolean connection_added = false;
		
		if(!key1.connections.contains(key2))
		{
			key1.connections.add(key2);
			connection_added = true;
		}
		else
		{
			Utilities.printAlert("Keys are already connected");
		}
		
		if(!key2.connections.contains(key1))
		{
			key2.connections.add(key1);
			connection_added = true;
		}
		else
		{
			Utilities.printAlert("Keys are already connected");
		}
		
		// If a new connection was made, handle the connection
		if(connection_added)
		{
			keyshapes.connectKeys(key1, key2);
		}
			/*
			ArrayList<KeyShape> shapes_with_key1 = findKeyShapesWithKey(key1);
			ArrayList<KeyShape> shapes_with_key2 = findKeyShapesWithKey(key2);
			KeyShape key1_shape = null; 
			KeyShape key2_shape = null;
			
			if(shapes_with_key1.size() > 1 || shapes_with_key2.size() > 1)
			{
				Utilities.printError("There are more than 2 KeyShapes that contain these keys. That should not be happening");
				return;
			}
			else
			{
				// Reassign the keyshapes in question for readibility
				if(shapes_with_key1.size() == 1)
				{
					key1_shape = shapes_with_key1.get(0);
				}
				
				if(shapes_with_key2.size() == 1)
				{
					key2_shape = shapes_with_key2.get(0);
				}
			}
			
			// If the there are no found keyshapes for either keys, make a new shape
			if(key1_shape == null && key2_shape == null)
			{
				KeyShape new_keyshape = new KeyShape(p);
				new_keyshape.connectKeys(key1, key2);
				keyshapes.add(new_keyshape);
			}
			// If one shape has the key, add the loose key to that shape
			else if(key1_shape != null && key2_shape == null)
			{
				key1_shape.connectKeys(key1, key2);
			}
			else if(key2_shape != null && key1_shape == null)
			{
				key2_shape.connectKeys(key1, key2);
			}
			// If both keys are already part of the existing shapes
			else if(key1_shape != null && key2_shape != null)
			{
				// If the keys belong to the same shape
				if(key1_shape == key2_shape)
				{
					key1_shape.connectKeys(key1, key2);
				}
				// If the shapes are not the same
				else if(key1_shape != key2_shape)
				{
					key1_shape.mergeWith(key2_shape);
				}
			}

		}
		*/
	}
	
	/*
	public ArrayList<KeyShape> findKeyShapesWithKey(Key key)
	{
		// Returns the number of Keyshapes that contain a specific key, returns an empty list if there is none
		ArrayList<KeyShape> returned_shapes = new ArrayList<KeyShape>();
		
		for(KeyShape keyshape: keyshapes)
		{
			if(keyshape.contains(key))
			{
				returned_shapes.add(keyshape);
			}
		}
		return returned_shapes;
	}
	
*/
	
	public void handleKeyDeletion()
	{
		for(int k=0; k<delta_keys.size(); k++)
		{
			if(delta_keys.get(k).marked_for_deletion)
			{
				deleteKey(delta_keys.get(k));
				k--;
			}
		}		
	}
	
	public void handleStrokeDeletion()
	{
		for(int s=0; s<strokes.size(); s++)
		{
			if(strokes.get(s).marked_for_deletion)
			{
				deleteStroke(strokes.get(s));
				s--;
			}
		}		
	}
	
	public void deleteStroke(Stroke to_delete)
	{
		int index_to_delete = strokes.indexOf(to_delete);
		
		if(index_to_delete != -1)
		{
			strokes.remove(index_to_delete);
		}
		else
		{
			Utilities.printAlert("Stroke does not exist. Cannot delete");
		}
	}
	
	public void deleteKey(Key to_delete)
	{
		int index_to_delete = delta_keys.indexOf(to_delete);
		
		if(index_to_delete != -1)
		{
			to_delete.disconnectAllKeys();
			delta_keys.remove(index_to_delete);
		}
		else
		{
			Utilities.printAlert("Key does not exist. Cannot delete");
		}
	}
	
	public void addKey(float x, float y, float d)
	{
		delta_keys.add(new Key(x, y, d, p));
	}
	
	//public void addKeyLine(Key key1, Key key2)
	//{
	///	keylines.add(new KeyLine(key1, key2, p));
	//}
	
	public void setCursor()
	{
		
	}
	
	public void recordStroke(float x, float y)
	{
		last_recording_input_x = x;
		last_recording_input_y = y;
		
		if(!recording_stroke)
		{
			recording_stroke = true;
			recorded_stroke = new Stroke(p,this);
			recorded_stroke.start_frame = current_frame;
			
			if(playback == PLAY)
			{
				was_playing = true;
			}
			else
			{
				was_playing = false;
			}
			
			play();
			p.println("RECORDING STROKE");
		}	
	}
	
	public Key calculateCurrentFrame(int frame)
	{
		Key added_keys = new Key(0,0,0,p);
		
		for(Stroke stroke: strokes)
		{
			PVector stroke_position = stroke.positionAtFrame(frame);
			if(stroke_position != null)
			{
				added_keys = added_keys.add(compileDeltaKeys(stroke_position.x, stroke_position.y));
			}
		}
		
		if(p.main_windows.sheet.animation_mode == p.main_windows.sheet.DRAW)
		{
			if(p.main_windows.sheet.withinBounds(p.mouseX, p.mouseY));
			{
				added_keys = added_keys.add(compileDeltaKeys(p.mouseX, p.mouseY));
			}
		}
		
		return added_keys;
	}
	
	void showCurrentFrame(int frame)
	{
		// Shows the current frame (with delta keys applied to primitives in stage)
		// Does not show frame when a key is selected and opened
		// When the sheet is recording, also take into account the mouse position
		
		//if(p.main_windows.sheet.animation_mode == p.main_windows.sheet.DRAW)
		//if(current_frame != 0 && p.main_windows.stage.opened_key == null)
		if(p.main_windows.stage.opened_key == null)
		{
			Key all_keys_added = calculateCurrentFrame(frame);
			
			if(p.main_windows.stage.opened_key != null)
			{
				p.main_windows.stage.exitActiveKey();
			}
			//p.println("DELTAS ARE");
			//p.animation.compileDeltaKeys(x_input, y_input).printDeltaData();
			p.main_windows.stage.applyDeltaKeyToAllPrimitivesInOrder(p.animation.default_key, all_keys_added);
		}
	}
	
	public void stopStroke()
	{
		if(recording_stroke)
		{
			if(!was_playing)
			{
				pause();
			}
			
			if(recorded_stroke != null)
			{
				if(recorded_stroke.points.size() > 0)
				{
					strokes.add(recorded_stroke);
					recorded_stroke = null;
					
					if(current_frame > frame_range[1])
					{
						frame_range[1] = current_frame;
					}
				}
			}
			recording_stroke = false;
			p.println("STOPPED RECORDING");
		}
	}

	public void update()
	{
		handleKeyDeletion();
		handleStrokeDeletion();
		
		if(playback == PLAY)
		{
			// Check to see if a tick has passed
			if(System.currentTimeMillis() - last_checked_time >= millis_per_frame)
			{
				// Add to current frame
				current_frame++;
				
				if(recording_stroke) // If a stroke is not being recorded, and the current frame has exceeded its range, loop it
				{
					recorded_stroke.addPoint(last_recording_input_x, last_recording_input_y);
				}
				else if(!recording_stroke) // Else if a the stroke is set to record, continue to progress the frame past the range
				{
					if(current_frame > frame_range[1])
					{
						current_frame = frame_range[0];
						pause();
						p.println(System.currentTimeMillis());
					}	
				}
				p.println("FRAME " + current_frame);
				// Update strokes
				for(Stroke stroke: strokes)
				{
				}
				// Update last time check
				last_checked_time = System.currentTimeMillis();
			}
		}
		
		showCurrentFrame((int)current_frame);
	}
	
	public boolean isPlaying()
	{
		if(playback == PLAY)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	void togglePlayback()
	{
		if(playback == PAUSE)
		{
			//p.println(System.currentTimeMillis());
			play();
		}
		else if(playback == PLAY)
		{
			//p.println(System.currentTimeMillis());
			pause();
		}
	}
	
	public void checkKeyEvent(KeyEvent e)
	{
		if(e.getKeyCode() == 32)
		{
			togglePlayback();
		}
		if(e.getKeyCode() == 68)
		{
			/*
			// If 'D' is pressed, toggle between drawing and composition mode
			if(p.main_windows.sheet.animation_mode == p.main_windows.sheet.DRAW)
			{
				p.main_windows.sheet.switchToCompositionMode();
			}
			else if(p.main_windows.sheet.animation_mode == p.main_windows.sheet.COMPOSITION)
			{
				p.main_windows.sheet.switchToDrawingMode();
			}
			
			if(!p.main_windows.stage.showing_compiled_keys)
			{
				Utilities.printAlert("Showing compiled keys");
				p.main_windows.stage.startCompiledKeys();
			}
			else if(p.main_windows.stage.showing_compiled_keys)
			{
				p.main_windows.stage.stopCompiledKeys();
				Utilities.printAlert("Stopping compiled keys");
			}
			*/
		}
	}

	public void play()
	{
		playback = PLAY;
		p.println("ANIMATION PLAYING");
	}
	
	public void pause()
	{
		playback = PAUSE;
		p.println("ANIMATION PAUSED");
	}	
}
