import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.util.Arrays; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class optimal_path extends PApplet {




int theSize = 10;
boolean gameOver = false;
OriginalBot player1 = new OriginalBot();
FireBot player2 = new FireBot();
int lastx;
int lasty;
public void setup(){
	lastx=mouseX;
	lasty=mouseY;
	rectMode(CENTER);
	size(500,500);
	frameRate(20);
	background(0);
}

//firebot
class FireBot {
	private int[] coords;
	private int[] goal;
	private int c;
	private String dir = "west";
	private boolean alive;
	private int nPoints = 0;
	private int sPoints = 0;
	private int ePoints = 0;
	private int wPoints = 0;

	FireBot(){
		coords = new int[] {495, 255};
		c = 0xff00D0FF;
		goal = new int[] {5,255};
	}

	public String getDir(){
		return this.dir;
	}
	public void setDir(String str){
		this.dir = str;
	}
	public int[] getCoords(){
		return this.coords;
	}

	public int getX(){
		return this.coords[0];
	}

	public int getY(){
		return this.coords[1];
	}

	public void changeX(int c){
		this.coords[0]+=c;
	}

	public void changeY(int c){
		this.coords[1]+=c;
	}

	public void chooseDir(){
		//hold already searched values
		Map<String, String> closed = new HashMap<String, String>(); //the key is the position, the cost is the value
		ArrayList<Node> fringe = new ArrayList<Node>();

		fringe.add(new Node(this.getCoords(), this.getDir(), goal, 0, null));		
		while(true){
			//if fringe is empty than the search has failed
			if(fringe.size()<=0){
				//println("failed");
				setDir("none");
				//println("closed:" + closed);
				break;
			}

			//set the current node to the first in the array: breadth search
			int target = 0;
			Node currentNode = fringe.get(0);
			String nodeCoords = Arrays.toString(currentNode.getCoords());

			//if the node is at the goal return the direction of the top of the node tree
			if(Arrays.equals(currentNode.getCoords(), goal)) {
				//println("SUCCESS");
				setDir(currentNode.getFirstMove().getDir());
				break;
			} else {
				//print(Arrays.toString(currentNode.getCoords()));
				//print(Arrays.toString(goal));
			}

			//if the node is not in the closed set or better than the one in the closed set
			if( !closed.containsKey(nodeCoords) 
				|| Integer.parseInt(closed.get(nodeCoords)) > currentNode.getPathCost() ){
				//put it in the closed set
				closed.put(nodeCoords, Integer.toString(currentNode.getPathCost()));

				//expand it
				ArrayList<Node> expansion = currentNode.expand();

				for(Node n : expansion){
					fringe.add(n);
				}
			}
			//remove target
			fringe.remove(target);

		}
	}


	public void drawBot(){
		fill(c);
		noStroke();
		rect(getX(),getY(),theSize,theSize);
	}

	public void move(){
		dir = getDir();
		if(dir == "east")
			changeX(10);
		if(dir == "west")
			changeX(-10);
		if(dir == "north")
			changeY(-10);
		if(dir == "south")
			changeY(10);
	}

	public void run(){
		chooseDir();
		//print(get(x,y) == -16776961 || get(x,y) == -16777216 );
		move();
		drawBot();
	}

}

class OriginalBot {

}

public void draw(){
	if(mousePressed){
		stroke(13,222,0,255);
		strokeWeight(10);
		line(mouseX, mouseY, pmouseX, pmouseY);
	}
	player2.run();
}

class Node {
	private int[] coords; //contain x, y
	private String dir;
	private int[] goal; //contain x, y
	private int pathCost;
	private int heuristic;
	private Node parent;
	private Node[] children;

	Node(int[] coords, String dir, int[] goal, int pathCost, Node parent){
		this.coords = coords;
		this.dir = dir;
		this.goal = goal;
		this.pathCost = pathCost;
		this.parent = parent;
		this.heuristic = getHeuristic();
	}

	public int getHeuristic(){
		return (abs(this.coords[0] - this.goal[0])+abs(this.coords[1]-this.goal[1]));
	}

	public int getTotalCost(){
		return this.pathCost + heuristic;
	}

	public int getPathCost(){
		return this.pathCost;
	}

	public int[] getGoal(){
		return this.goal;
	}

	public Node getParent(){
		return this.parent;
	}

	public Node[] getChildren(){
		return this.children;
	}

	public int[] getCoords(){
		return this.coords;
	}

	public int getX(){
		return this.coords[0];
	}

	public int getY(){
		return this.coords[1];
	}

	public boolean goalMet(){
		return this.coords == goal;
	}

	public String getDir(){
		return this.dir;
	}

	public ArrayList<Node> expand(){
		ArrayList<Node> expansion = new ArrayList<Node>();
		
		//check if surroundings are open and if so add node to arraylist

		//east
		if(dir != "west" && ( get(getX()+10, getY()) == -16776961 || get(getX()+10, getY()) == -16777216) ){
			expansion.add(new Node(new int[] {getX()+10, getY()}, "east", getGoal(), getPathCost()+1, this));
		}

		//west
		if(dir != "east" && ( get(getX()-10, getY()) == -16776961 || get(getX()-10, getY()) == -16777216) ){
			expansion.add(new Node(new int[] {getX()-10, getY()}, "west", getGoal(), getPathCost()+1, this));
		}

		//south
		if(dir != "north" && ( get(getX(), getY()+10) == -16776961 || get(getX(), getY()+10) == -16777216) ){
			expansion.add(new Node(new int[] {getX(), getY()+10}, "south", getGoal(), getPathCost()+1, this));
		}

		//north
		if(dir != "south" && ( get(getX(), getY()-10) == -16776961 || get(getX(), getY()-10) == -16777216) ){
			expansion.add(new Node(new int[] {getX(), getY()-10}, "north", getGoal(), getPathCost()+1, this));

		}
		return expansion;
	}

	public ArrayList<Node> showPath(ArrayList<Node> branch){
		branch.add(0, this);
		if(getParent() == null)
			return branch;
		return getParent().showPath(branch);
	}

	public Node getFirstMove(){
		//we have to get the parent twice because the first node is simply our current position
		//so we want the second node
		if(this.getParent() != null && this.getParent().getParent() == null){
			return this;
		} else if(this.getParent() == null){
			return this;
		}

		return getParent().getFirstMove();
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "optimal_path" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
