import java.util.*;
import java.util.Arrays;

int theSize = 10;
boolean gameOver = false;
OriginalBot player1 = new OriginalBot();
FireBot player2 = new FireBot();
int lastx;
int lasty;
void setup(){
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

	FireBot(){
		coords = new int[] {495, 255};
		c = #00D0FF;
		goal = new int[] {5,255};
	}

	String getDir(){
		return this.dir;
	}
	void setDir(String str){
		this.dir = str;
	}
	int[] getCoords(){
		return this.coords;
	}

	int getX(){
		return this.coords[0];
	}

	int getY(){
		return this.coords[1];
	}

	void changeX(int c){
		this.coords[0]+=c;
	}

	void changeY(int c){
		this.coords[1]+=c;
	}

	void chooseDir(){
		//hold already searched values
		Map<String, String> closed = new HashMap<String, String>(); //the key is the position, the cost is the value
		ArrayList<Node> fringe = new ArrayList<Node>(); //nodes we have yet to look at

		fringe.add(new Node(this.getCoords(), this.getDir(), goal, 0, null));		
		while(true){

			//if fringe is empty than the search has failed so stop the bot
			if(fringe.size()<=0){
				setDir("none");
				break;
			}

			//set the current node to the first in the array: breadth search
			int target = 0;
			Node currentNode = fringe.get(0); //variable to hold the node for reference
			String nodeCoords = Arrays.toString(currentNode.getCoords()); //variable to hold the coords for reference

			//If the node is at the goal return the direction of the top of the node tree 
			//This will return the directions for the first step of the current optimal path
			if(Arrays.equals(currentNode.getCoords(), goal)) {
				setDir(currentNode.getFirstMove().getDir());
				break;
			}

			//If the node is not in the closed set (already looked at) or better than the one in the closed set:
			//we should explore and expand the node to see if it leads to a optimum path
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
			//Now that we have looked at the node, we remove it from the fringe because we no longer need it
			fringe.remove(target);

		}
	}

	//draws the rectangle
	void drawBot(){
		fill(c);
		noStroke();
		rect(getX(),getY(),theSize,theSize);
	}

	//moves the rectangle in the current stated direction
	void move(){
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

	//this is called every frame to run the bot
	void run(){
		chooseDir();
		move();
		drawBot();
	}

}

//A node in a tree of decisions representing a coordinate and a facing direction (the bot can make a 180 degree turn)
class Node {
	private int[] coords; //contain x, y of this node
	private String dir; //current facing direction
	private int[] goal; //contain x, y of what the bot is trying to reach
	private int pathCost; //how many moves it would take to get to this coordinate
	private int heuristic; //makes node search more intelligent, but I was to lazy to implement it
	private Node parent; //the previous node that led to this
	private Node[] children; //nodes that can be expanded from this

	//instantiate the node
	Node(int[] coords, String dir, int[] goal, int pathCost, Node parent){
		this.coords = coords;
		this.dir = dir;
		this.goal = goal;
		this.pathCost = pathCost;
		this.parent = parent;
		this.heuristic = getHeuristic();
	}

	int getHeuristic(){
		return (abs(this.coords[0] - this.goal[0])+abs(this.coords[1]-this.goal[1]));
	}

	int getTotalCost(){
		return this.pathCost + heuristic;
	}

	int getPathCost(){
		return this.pathCost;
	}

	int[] getGoal(){
		return this.goal;
	}

	Node getParent(){
		return this.parent;
	}

	Node[] getChildren(){
		return this.children;
	}

	int[] getCoords(){
		return this.coords;
	}

	int getX(){
		return this.coords[0];
	}

	int getY(){
		return this.coords[1];
	}

	boolean goalMet(){
		return this.coords == goal;
	}

	String getDir(){
		return this.dir;
	}

	//returns the children of this node
	ArrayList<Node> expand(){
		//will hold what we will return
		ArrayList<Node> expansion = new ArrayList<Node>();
		
		//each if statements checks an adjacent coord is viable for a node and if so adds the node to expansion arraylist

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

	//returns the total path of nodes to get to the current one
	ArrayList<Node> showPath(ArrayList<Node> branch){
		branch.add(0, this);
		if(getParent() == null)
			return branch;
		return getParent().showPath(branch);
	}

	//get the first node of this subranch
	Node getFirstMove(){
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

//This is the main function called every frame that runs the program
void draw(){

	//If mouse is pressed, draw a barrier
	if(mousePressed){
		stroke(13,222,0,255);
		strokeWeight(10);
		line(mouseX, mouseY, pmouseX, pmouseY);
	}

	//run the bot
	player2.run();
}
