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
	ArrayList<Key> keys;
	
	public AnimationController(AniSketch p) 
	{
		this.p = p;
		strokes = new ArrayList<Stroke>();
		keys = new ArrayList<Key>();
	}
	
	public void addKey(float x, float y, float d)
	{
		keys.add(new Key(x, y, d, p));
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
