import processing.core.*;
import processing.event.MouseEvent;

public class MainWindows {
	/* This handles the main UI
	 *
	 *    vert. divider
	 *         |
	 *  _______v_______
	 * |       |       |
	 * | sheet | stage |
	 * |_______|_______|<--- horiz. divider
	 * |   time line   |
	 * |_______________|
	 */
	
	Sheet sheet;
	Stage stage;
	Timeline timeline;
	
	Divider vertical_divider;
	Divider horizontal_divider;
	
	//float vertical_divider = 0.5f;  // x position of the vertical divider (out of width)
	//float horizont_divider = 0.85f; // y position of the horizontal divider (out of height)
	
	final static int LEFT_HAND  = 0;
	final static int RIGHT_HAND = 1;
	int handedness = RIGHT_HAND;
	
	PApplet p;		
			
	MainWindows(PApplet p){
		this.p = p;
		setupDividers();
		//sheet  = new Sheet(0, 0, (int)(vertical_divider*p.width), (int)(horizont_divider*p.height), p);
		//stage  = new Stage((int)(vertical_divider*p.width), 0, (int)((1-vertical_divider)*p.width), (int)(horizont_divider*p.height), p);
	}
	
	void setup()
	{
	}
	
	void setupDividers()
	{
		horizontal_divider = new Divider(p);
		horizontal_divider.setHorizontal(0,p.height,0.2f,0.8f,0.5f,10);
		
		vertical_divider = new Divider(p);
		vertical_divider.setVertical(0,p.width,0.2f,0.8f,0.5f,10); 
		
		horizontal_divider.w = p.width;
		horizontal_divider.x = 0;
		
		vertical_divider.h = horizontal_divider.y;
		vertical_divider.y = 0;
		
		//vertical_divider.default_style = horizontal_divider.default_style;
		//vertical_divider.hover_style = horizontal_divider.hover_style;

		//vertical_divider.setBounds(0, p.width);
		//vertical_divider.setMovementRange(0.2f, 0.8f);
				
		//horizontal_divider.default_style.noStroke();
		//horizontal_divider.default_style.fill(10,10,10,255);
		//horizontal_divider.hover_style.noStroke();
		//horizontal_divider.hover_style.fill(80,80,80,255);
		//horizontal_divider.setBounds(0, p.height);
		//horizontal_divider.setMovementRange(0.5f, 0.9f);
	}
	
	void updateDividers()
	{
		vertical_divider.h = horizontal_divider.y+horizontal_divider.thickness/2;
		vertical_divider.draw();
		horizontal_divider.draw();
		
	}
	
	void handleDividersOnResize()
	{
		horizontal_divider.setBounds(0, p.height);
		horizontal_divider.w = p.width;
		vertical_divider.setBounds(0, p.width);
		
		vertical_divider.updateCurrentPosition();
		horizontal_divider.updateCurrentPosition();
		updateDividers();
	}
	
	void onScreenResize()
	{
		handleDividersOnResize();
	}
	
	
	void drawBorder()
	{
		p.noFill();
		p.stroke(20);
		p.strokeWeight(5);
		p.rect(2, 2, p.width-5, p.height-5);
	}
	
	void update()
	{
		updateDividers();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		horizontal_divider.checkMouseEvent(e);
		vertical_divider.checkMouseEvent(e);
	}
	
	
}
