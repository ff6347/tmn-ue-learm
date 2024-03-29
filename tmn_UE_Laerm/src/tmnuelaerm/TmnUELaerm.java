package tmnuelaerm;

import TUIO.TuioListener;
import interaction.TNObstacleObject;

import java.util.ArrayList;
import java.util.List;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioTime;

import processing.core.PApplet;
import processing.core.PVector;

import util.Debug;
import util.Overlay;
import util.Style;
import util.XMLImporter;

import particleSystem.PSUtil;
import particleSystem.Particle;
import particleSystem.ParticleSystem;
import particleSystem.Path;
import particleSystem.Property;

/**
 * This is the main Class of the Multitouch project by the-moron.net<br>
 * represented by fabiantheblind and PDXIII <br>
 * students at the University of Applied Sciences Potsdam (FHP) <br>
 * during the class of Till Nagel "Urbane Ebenen" (urban layers).<br>
 * the {@code code} is available here: <a
 * href="http://code.google.com/p/tmn-ue-learm/" target="blanc">Google Code</a><br>
 * or here: <a href="http://github.com/fabiantheblind/TMN_UE_Laerm.git"
 * target="blanc"> GitHub</a><br>
 * 
 * MISO http://www.omkrets.se/typografi/
 * 
 * Unibody8 http://www.underware.nl/site2/index.php?id1=unibody&id2=info
 * 
 * @author PDXIII
 * @author fabiantheblind
 * @version 0.123
 * 
 * 
 */
public class TmnUELaerm extends PApplet implements TuioListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1217858472488377688L;
	/**
	 * An List of TNObstacleObject
	 * 
	 * @see Class TNObstacleObject Class
	 */
	List<TNObstacleObject> transObjects = new ArrayList<TNObstacleObject>();

	/**
	 * controls the amount of TNObstleOjects
	 */
	public int howManyObstacles = 8;

	/**
	 * The Tuio Client
	 */
	public TuioClient tuioClient = new TuioClient();
	/**
	 * a ArrayList of tuio cursors
	 */
	public ArrayList<TuioCursor> tuioCursorList;
	/**
	 * An ArrayList of Particles
	 * 
	 * @see Class Particle Class
	 */
	private ArrayList<Particle> ptclsList = new ArrayList<Particle>();

	/**
	 * our particle System
	 * 
	 * @see Class ParticleSystem Class
	 */
	public ParticleSystem ps;
	/**
	 * An ArrayList of Paths
	 * 
	 * @see Class Path Class
	 */
	public ArrayList<Path> pathsList = new ArrayList<Path>();
	/**
	 * number of <code>Particle</code>'s
	 */
	int numPtcls = 1000;
	/**
	 * An ArrayList of {@link PSUtil#initPropertysList()} for all the Objects
	 * 
	 * @see Class Property Class
	 */
	public ArrayList<Property> propertysList;
	/**
	 * standard radius for the <code>Particle</code>'s<br>
	 * every particle can have his own force / radius / speed<br>
	 * they can be changed later<br>
	 * this is for the collision of <code>Particle</code>'s<br>
	 */
	float ptclRadius = 2;
	/**
	 * a boolean for switching the path
	 */
	private boolean switchPath = false;
	/**
	 * the time switches with the color
	 * 
	 */
	boolean DAY = true;
	/**
	 * display the debug Stuff if {@code true}
	 */
	boolean showDebug = false;
	/**
	 * display the paths if {@code true}
	 */
	boolean showDebugPath = false;
	
	
	/**
	 * 
	 */
	boolean showOverlay = false;

	/**On Object of the class overlay
	 * would be good to move all displayed stuff from debug to overlay
	 * 
	 */
	private Overlay overlay;
	
	/**
	 * show the life of one particle
	 */
	private boolean showDebugPtcl = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		colorMode(HSB, 360, 100, 100);
		// passing the PApplet thru to all static methods
		// make the Styling
		Style.setPApplet(this);
		Style.create();
		// this is for overlays and Stuff
		Debug.setPAppletDebug(this);
		// these are some methods that help with the ParticleSystem
		PSUtil.setPApplet(this);
		// this is for importing the Property of the objects from an .xml file
		XMLImporter.setPAppelt(this);
		propertysList = PSUtil.initPropertysList();

//		background(0);
		size(1024, 768, OPENGL);
		frameRate(35);
		// init TUIO
		tuioClient.addTuioListener(this);
		tuioClient.connect();

		// making ObstacleObjects
		Property property;
		for (int i = 0; i < howManyObstacles; i++) {
			property = propertysList.get(i);
			TNObstacleObject transObstclObj= new TNObstacleObject(this, 0, 0, 0, 0,property);

			firstPosition(transObstclObj);

			transObjects.add(transObstclObj);
		}

		// end PDXIII TUIO Stuff

		// particle stuff
		
		PSUtil.makeSpaces(pathsList);
		// We are now making random <code>Particle</code>'s and storing them in an ArrayList ptclsList
		ptclsList = PSUtil.initParticles(numPtcls, ptclRadius, ptclsList);

		// add the Path ptclPoints ArrayList of <code>Particle</code>'s to the ptclsList
		
		
		for(Path path : pathsList){
			
			for(Particle ptcl :path.getPtclPoints() ){
				ptclsList.add(ptcl);
				
			}
		}

		// we need the particle system to interact with the TNObstacleObject
//		ps = new ParticleSystem(this, new PVector(width / 2, height / 2),ptclsList);
		ps = new ParticleSystem(this, 100, new PVector(width / 2, height / 2), ptclsList);

		overlay = new Overlay(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		smooth();

		DAY = Style.switchTime(DAY);
		switchPath = Style.switchPath(DAY,switchPath);
		Style.theBackground();
		// this is for the <code>Particle</code>'s that make the paths
		// to get them back into their original position we have to reset them
		// in the function Path.resetPointPtcls() you can set
		// how fast and strong the want to get back
		PSUtil.resetPath( pathsList);
		// this is for switching every 300 frames the path to follow
		PSUtil.applyPaths(ptclsList, switchPath, pathsList);

		for (TNObstacleObject transformableObject : transObjects) {
			transformableObject.draw();
		}
		// pass all Objects over to the ParticleSystem
		ps.applyObstcles(transObjects, DAY);
		// Run the Particle System
		ps.run();


		// PDXIII TUIO Stuff
		tuioCursorList = new ArrayList<TuioCursor>(tuioClient.getTuioCursors());
		// //end PDXIII TUIO Stuff

		// DEBUGGING START press "d"
		if (showDebug) {
			
			if (showDebugPtcl){
				Debug.watchAParticle(ptclsList, ps);
			
			}
//			Debug.watchARepellers(someRepellers);
//			PSUtil.displaySomeRepellers(someRepellers);

			if (showDebugPath) {
				Debug.displayAllPaths(pathsList);
			}
//			Debug.drawFrameRate();
//			Debug.drawFrameCount();
			
			if (showOverlay){
				
				overlay.display();
			}
		}
		// //just for adjustment
		// debug.drawGrid();
		Debug.drawCursors(tuioCursorList);
		// Debug.drawCursorCount(tuioCursorList);
		Debug.writeIMGs();
		// DEBUGGING END
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#addTuioCursor(TUIO.TuioCursor)
	 */

	
	void firstPosition(TNObstacleObject transObstclObj){
		
//		following sets the ObstclObjects on the edges of the screen
		
//		if(transObstclObj.property.index == 0){
//			
//			transObstclObj.addOffset(width/ 5, height - 50);
//		}
//		
//		if(transObstclObj.property.index == 1){
//			
//			transObstclObj.addOffset(width/ 5 * 3, height - 50);
//		}
//		
//		
//		if(transObstclObj.property.index == 2){
//			
//			transObstclObj.addOffset(width/ 5 * 2, 50);
//			transObstclObj.rotate(PI);
//		}
//		
//		if(transObstclObj.property.index == 3){
//			
//			transObstclObj.addOffset(width/ 5 * 4, 50);
//			transObstclObj.rotate(PI);
//		}
//		
//		if(transObstclObj.property.index == 4){
//			
//			transObstclObj.addOffset(50, height/5);
//			transObstclObj.rotate(PI/2);
//		}
//		
//		if(transObstclObj.property.index == 5){
//			
//			transObstclObj.addOffset(50, height/5 * 3);
//			transObstclObj.rotate(PI/2);
//		}
//		
//		if(transObstclObj.property.index == 6){
//			
//			transObstclObj.addOffset(width - 50, height/5 * 2);
//			transObstclObj.rotate(-PI/2);
//		}
//		
//		if(transObstclObj.property.index == 7){
//			
//			transObstclObj.addOffset(width - 50, height/5 * 4);
//			transObstclObj.rotate(-PI/2);
//		}
		
		if(transObstclObj.property.index == 0){
			
			transObstclObj.addOffset(width/2, height/2);
		}
		
		if(transObstclObj.property.index == 1){
			
			transObstclObj.addOffset(width/2, height/2);
			transObstclObj.rotate(PI/2);

		}
		
		
		if(transObstclObj.property.index == 2){
			
			transObstclObj.addOffset(width/2, height/2);
			transObstclObj.rotate(PI);
		}
		
		if(transObstclObj.property.index == 3){
			
			transObstclObj.addOffset(width/2, height/2);
			transObstclObj.rotate(-PI/2);
		}
		
		if(transObstclObj.property.index == 4){
			
			transObstclObj.addOffset(width/2, height/2 + 50);
		}
		
		if(transObstclObj.property.index == 5){
			
			transObstclObj.addOffset(width/2 -50, height/2);
			transObstclObj.rotate(PI/2);
		}
		
		if(transObstclObj.property.index == 6){
			
			transObstclObj.addOffset(width/2, height/2 - 50);
			transObstclObj.rotate(PI);
		}
		
		if(transObstclObj.property.index == 7){
			
			transObstclObj.addOffset(width/2 + 50, height/2);
			transObstclObj.rotate(-PI/2);
		}
		
	}
	@Override
	public void addTuioCursor(TuioCursor tcur) {
		// Hit test for all objects: first gets the hit, ordered by creation.
		// TODO Order by z-index, updated by last activation/usage
		for (TNObstacleObject ttObj : transObjects) {
			if (ttObj.isHit(tcur.getScreenX(width), tcur.getScreenY(height))) {
				ttObj.addTuioCursor(tcur);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#updateTuioCursor(TUIO.TuioCursor)
	 */

	@Override
	public void updateTuioCursor(TuioCursor tcur) {
		for (TNObstacleObject ttObj : transObjects) {
			if (ttObj.isHit(tcur.getScreenX(width), tcur.getScreenY(height))) {
				ttObj.updateTuioCursor(tcur);

				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#removeTuioCursor(TUIO.TuioCursor)
	 */

	@Override
	public void removeTuioCursor(TuioCursor tcur) {
		for (TNObstacleObject ttObj : transObjects) {
			// Pass trough remove-event to all objects, to allow fingerUp also
			// out of boundaries,
			// as objects decide themselves (via cursor-id) whether cursor
			// belongs to it.

			ttObj.removeTuioCursor(tcur);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#addTuioObject(TUIO.TuioObject)
	 */
	@Override
	public void addTuioObject(TuioObject arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#refresh(TUIO.TuioTime)
	 */
	@Override
	public void refresh(TuioTime arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#removeTuioObject(TUIO.TuioObject)
	 */
	@Override
	public void removeTuioObject(TuioObject arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TUIO.TuioListener#updateTuioObject(TUIO.TuioObject)
	 */
	@Override
	public void updateTuioObject(TuioObject arg0) {
		// TODO Auto-generated method stub

	}

	// TUIO methods end

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#keyPressed()
	 */
	public void keyPressed() {
		if (key == 'd') {
			// do something fancy
			if (showDebug == true) {
				showDebug = false;

			} else {
				showDebug = true;

			}
		}
		if (key == 'p') {
			// do something fancy
			if (showDebugPath == true) {
				showDebugPath = false;

			} else {
				showDebugPath = true;

			}
		}
		if (key == 'l'){
			
			if(showOverlay){
				showOverlay = false;
			} else {
				
				showOverlay = true;
			}
		}
		if (key == 'k'){
			
			if(showDebugPtcl){
				showDebugPtcl = false;
			} else {
				
				showDebugPtcl = true;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#keyReleased()
	 * 
	 * @see Debug#saveFrame(float)
	 */
	public void keyReleased() {

		// not important for the main programm
		if (key == 's' || key == 'S') {

			// just for unique filenames when saving a frame as .jpg in the
			// folder data
			float time;
			time = millis();
			Debug.saveFrame(time);
			println("wrote \"MyImg" + time + ".jpg\" to the folder bilder");

		}
		if (key == 'e' || key == 'E') {
			exit();
		}
		if (key == 'i' || key == 'I') {
			Debug.writeImg = true;
		}
		if (key == 'o' || key == 'O') {
			Debug.writeImg = false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#mousePressed()
	 */
	@Override
	public void mousePressed() {
//		// PSUtil.newPtkl(this, mouseX, mouseY, ptclsList, ptclRadius);
//
//		for (int i = 0; i < someRepellers.size(); i++) {
//			Repeller r = someRepellers.get(i);
//			r.clicked(mouseX, mouseY);
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#mouseReleased()
	 */
	@Override
	public void mouseReleased() {
//
//		for (int i = 0; i < someRepellers.size(); i++) {
//			Repeller r = someRepellers.get(i);
//			r.stopDragging();
//		}
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main(new String[] { tmnuelaerm.TmnUELaerm.class.getName() });


	}
	
}
