import java.util.ArrayList;

import processing.core.PVector;
import processing.event.KeyEvent;

public class AnimationController {

	static final int framerate = 25; 
	static int millis_per_frame = 1000/framerate;
	
	static final int PAUSE = 0;
	static final int PLAY = 1;
	
	int playback = PAUSE;
	long frame_range[] = {0, 250};
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
	Key default_key;
	
	boolean showing_compiled_keys = false;
	
	public AnimationController(AniSketch p) 
	{
		this.p = p;
		strokes = new ArrayList<Stroke>();
		delta_keys = new ArrayList<Key>();
		default_key = new Key(0, 0, 0, p);
	}

	public Key compileDeltaKeys(float x_input, float y_input)
	{
		Key compiled_key = new Key(0,0,0,p);
		
		// For all delta keys
		for(Key delta_key: delta_keys)
		{
			// For all data in delta keys
			for(Key.PrimitiveData primitive_data: delta_key.primitive_data)
			{ 
				// Add the primitive data contained in the delta key, multiplied by the delta key's weighting
				compiled_key.addPrimitiveData(primitive_data.mult(delta_key.getWeight(x_input, y_input)));
			}
		}
		return compiled_key;
	}
	
	public void addKey(float x, float y, float d)
	{
		delta_keys.add(new Key(x, y, d, p));
	}
	
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
	}
	
	public void checkKeyEvent(KeyEvent e)
	{
		if(e.getKeyCode() == 32)
		{
			if(playback == PAUSE)
			{
				p.println(System.currentTimeMillis());
				play();
			}
			else if(playback == PLAY)
			{
				p.println(System.currentTimeMillis());
				pause();
			}
		}
		if(e.getKeyCode() == 67)
		{
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
