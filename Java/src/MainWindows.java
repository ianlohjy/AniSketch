
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
	
	final static int LEFT_HAND  = 0;
	final static int RIGHT_HAND = 1;
	int handedness = RIGHT_HAND;
	
	AniSketch p;		
			
	MainWindows(AniSketch p){
		this.p = p;
		setupDividers();
		setupWindows();
	}
	
	void setup()
	{
	}
	
	void setupWindows()
	{
		sheet  = new Sheet(0,0,vertical_divider.x,horizontal_divider.y,p,p.animation);
		stage  = new Stage(vertical_divider.x,0,p.width-vertical_divider.x,horizontal_divider.y,sheet,p);
		timeline = new Timeline(0,horizontal_divider.y,p.width,p.height-horizontal_divider.y,p,p.animation);
		
		updateWindowPositions();
	}
	
	void updateWindowPositions()
	{
		sheet.x = 0;
		sheet.y = 0;
		sheet.w = vertical_divider.x;
		sheet.h = horizontal_divider.y;
		
		stage.x = vertical_divider.x;
		stage.y = 0;
		stage.w = p.width-vertical_divider.x;
		stage.h = horizontal_divider.y;
		
		timeline.x = 0;
		timeline.y = horizontal_divider.y;
		timeline.w = p.width;
		timeline.h = p.height-horizontal_divider.y;
	}
	
	void setupDividers()
	{
		horizontal_divider = new Divider(p,this);
		horizontal_divider.setHorizontal(0,p.height,0.2f,0.9f,0.8f,12);
		
		vertical_divider = new Divider(p,this);
		vertical_divider.setVertical(0,p.width,0.05f,0.95f,0.5f,12); 
		
		horizontal_divider.w = p.width;
		horizontal_divider.x = 0;
		
		vertical_divider.h = horizontal_divider.y;
		vertical_divider.y = 0;
	}
	
	void updateVerticalDividerHeight()
	{	// Makes sure that the height of the vertical divider does not exceed the position of the horizontal divider
		vertical_divider.h = horizontal_divider.y+horizontal_divider.thickness/2;			
	}
	
	void drawDividers()
	{	// Draws dividers
		vertical_divider.draw();
		horizontal_divider.draw();
	}
	
	void drawWindows()
	{
		sheet.draw();
		stage.draw();
		timeline.draw();
	}
	
	void handleDividersOnResize()
	{	// Things that need to be updated on dividers when window is resized
		horizontal_divider.w = p.width;
		horizontal_divider.setBounds(0, p.height);
		vertical_divider.setBounds(0, p.width);
		
		vertical_divider.updateCurrentPosition();
		horizontal_divider.updateCurrentPosition();

		updateVerticalDividerHeight();
		updateWindowPositions();
	}
	
	void onScreenResize()
	{
		handleDividersOnResize();
		updateWindowPositions();
	}
	
	void callbackDividersUpdatePosition()
	{
		updateWindowPositions();
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
		updateVerticalDividerHeight();
		drawWindows();
		drawDividers();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		horizontal_divider.checkMouseEvent(e);
		vertical_divider.checkMouseEvent(e);
		if(!horizontal_divider.pressed && !vertical_divider.pressed)
		{
			stage.checkMouseEvent(e);
			sheet.checkMouseEvent(e);
			timeline.checkMouseEvent(e);
		}
	}
	
	void handleGestureResponse(GestureEngine.GestureResponse gesture_response)
	{
		
	}
	
	
}
