import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayDeque;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents a help class used for moving through the board as a graph
class GraphUtils {

  ArrayList<String> directions = new ArrayList<String>(
      Arrays.asList("left", "right", "up", "down"));

  // returns the farthest GamePieces from the given GamePiece in the given list
  GamePiece furthestPiece(GamePiece from, ArrayList<GamePiece> connect) {
    HashMap<GamePiece, Integer> dists = this.distanceMap(from);
    GamePiece currMax = from;
    int max = 0;
    for (GamePiece p : connect) {
      if (p.connectedToPower(from)) {
        if (dists.get(p) > max) {
          max = dists.get(p);
          currMax = p;
        }
      }
    }
    return currMax;
  }

  // returns the farthest GamePieces from the given GamePiece in the given list
  int furthestDist(GamePiece from, ArrayList<GamePiece> connect) {
    HashMap<GamePiece, Integer> dists = this.distanceMap(from);
    return dists.get(this.furthestPiece(from, connect));
  }

  // returns a hash map of all connected GamePiece distances from the given
  // GamePiece
  HashMap<GamePiece, Integer> distanceMap(GamePiece from) {
    ArrayDeque<GamePiece> worklist = new ArrayDeque<GamePiece>();
    HashMap<GamePiece, Integer> distances = new HashMap<GamePiece, Integer>();
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    distances.put(from, 0);
    worklist.add(from);

    while (worklist.size() > 0) {
      GamePiece next = worklist.removeFirst();

      if (seen.contains(next)) {
        // if seen has the next element don't do anything with it
      }
      else {
        for (String s : this.directions) {
          if (next.hasNeighbor(s) && next.connected(s) && !seen.contains(next.neighbors.get(s))) {
            distances.put(next.neighbors.get(s), distances.get(next) + 1);
            worklist.addFirst(next.neighbors.get(s));
          }
        }
        seen.add(next);
      }
    }
    return distances;
  }

  // returns a hash map of all pieces the given GamePiece is connected to
  HashMap<GamePiece, GamePiece> bfs(GamePiece from) {
    ArrayDeque<GamePiece> worklist = new ArrayDeque<GamePiece>();
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    HashMap<GamePiece, GamePiece> graph = new HashMap<GamePiece, GamePiece>();
    worklist.addFirst(from);
    graph.put(from, from);
    while (worklist.size() > 0) {
      GamePiece next = worklist.removeFirst();
      if (seen.contains(next)) {
        // if seen has the next element, don't do anything with it
      }
      else {
        for (String s : this.directions) {
          if (next.hasNeighbor(s) && next.connected(s)) {
            worklist.addFirst(next.neighbors.get(s));
            graph.put(next.neighbors.get(s), next);
          }
        }
        seen.add(next);
      }
    }
    return graph;
  }
}

// a class to represent a GamePiece in the Light Em All game
class GamePiece {
  public static final int GAMEPIECE_SIZE = 50;
  public final WorldImage SQUARE = new FrameImage(
      new RectangleImage(50, 50, OutlineMode.SOLID, Color.DARK_GRAY));
  public final Color OFF_WIRE_COLOR = Color.GRAY;
  public final WorldImage POWER_STATION = new OverlayImage(
      new StarImage(GAMEPIECE_SIZE / 3, 7, OutlineMode.OUTLINE, Color.RED),
      new StarImage(GAMEPIECE_SIZE / 3, 7, OutlineMode.SOLID, Color.GREEN));
  public final Color FULLPOWER_WIRE_COLOR = Color.YELLOW;

  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean up;
  boolean down;

  // represents the 4 neighbors a GamePiece can connect to
  // where the first element is the left neighbor, second element is the top
  // neighbor, third element is the right neighbor, and fourth element
  // is the bottom neighbor
  HashMap<String, GamePiece> neighbors;

  // whether the power station is on this piece
  boolean powerStation;

  // constructor to make a new GamePiece, originally with no wires or Neighbors
  GamePiece(int x, int y) {
    this.row = x;
    this.col = y;
    this.left = false;
    this.right = false;
    this.down = false;
    this.up = false;
    this.neighbors = new HashMap<String, GamePiece>();
    this.powerStation = false;
  }

  // rotates this piece such that it's wires now have new orientations
  // EFFECT: Changes the orientation of the wire fields (top/right.bottom/left)
  void rotatePiece() {
    boolean temp = this.left;
    boolean temp2 = this.up;
    boolean temp3 = this.right;
    boolean temp4 = this.down;
    this.left = temp4;
    this.up = temp;
    this.right = temp2;
    this.down = temp3;
  }

  // does this GamePiece have the neighbor with the given description?
  boolean hasNeighbor(String descrip) {
    return this.neighbors.containsKey(descrip);
  }

  // is this GamePiece connected to the neighbor of given description?
  boolean connected(String descrip) {
    if (this.hasNeighbor(descrip)) {
      if (descrip.equals("right")) {
        return this.neighbors.get("right").left && this.right;
      }
      else if (descrip.equals("up")) {
        return this.neighbors.get("up").down && this.up;
      }
      else if (descrip.equals("down")) {
        return this.neighbors.get("down").up && this.down;
      }
      else if (descrip.equals("left")) {
        return this.neighbors.get("left").right && this.left;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  // renders this GamePiece as an image, depending on what wires it has
  WorldImage drawPiece(GamePiece power, int radius) {
    WorldImage result = SQUARE;
    WorldImage par = new RectangleImage(GAMEPIECE_SIZE / 2 + 2, GAMEPIECE_SIZE / 10,
        OutlineMode.SOLID, this.chooseColor(power, radius));
    WorldImage perp = new RectangleImage(GAMEPIECE_SIZE / 10, GAMEPIECE_SIZE / 2 + 2,
        OutlineMode.SOLID, this.chooseColor(power, radius));
    if (this.left) {
      result = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, par, 0, 0, result);
    }
    if (this.right) {
      result = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, par, 0, 0, result);
    }
    if (this.up) {
      result = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, perp, 0, 0, result);
    }
    if (this.down) {
      result = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, perp, 0, 0, result);
    }
    if (this.powerStation) {
      result = new OverlayImage(POWER_STATION, result);
    }
    return result;
  }

  // chooses the appropriate color for this GamePiece
  Color chooseColor(GamePiece power, int radius) {
    if (this.connectedToPower(power)) {
      if (this.distanceFromPower(power) > radius) {
        return OFF_WIRE_COLOR;
      }
      else {
        return FULLPOWER_WIRE_COLOR;
      }
    }
    else {
      return OFF_WIRE_COLOR;
    }
  }

  // how far away is this GamePiece from the power station?
  // returns -1 if piece is not connected to power
  int distanceFromPower(GamePiece power) {
    if (this.connectedToPower(power)) {
      GraphUtils u = new GraphUtils();
      HashMap<GamePiece, Integer> distances = u.distanceMap(power);
      return distances.get(this);
    }
    else {
      return -1;
    }
  }

  // is this GamePiece connected to the power?
  boolean connectedToPower(GamePiece power) {
    if (this.powerStation) {
      return true;
    }
    else {
      GraphUtils u = new GraphUtils();
      HashMap<GamePiece, GamePiece> connections = u.bfs(power);
      return connections.containsKey(this);
    }
  }

  // helps to link GamePieces to this GamePiece, if the surrounding GamePieces
  // exist
  // EFFECT: Mutates this GamePiece's list of neighboring cells
  public void linkGamePieceHelp(int i, int j, ArrayList<ArrayList<GamePiece>> board) {
    if (coordOnBoard(i + 1, j, board)) {
      this.neighbors.put("right", board.get(i + 1).get(j));
    }
    if (coordOnBoard(i - 1, j, board)) {
      this.neighbors.put("left", board.get(i - 1).get(j));
    }
    if (coordOnBoard(i, j + 1, board)) {
      this.neighbors.put("down", board.get(i).get(j + 1));
    }
    if (coordOnBoard(i, j - 1, board)) {
      this.neighbors.put("up", board.get(i).get(j - 1));
    }
  }

  // returns whether the given coordinates are within the given board's boundaries
  public boolean coordOnBoard(int row, int col, ArrayList<ArrayList<GamePiece>> board) {
    return (0 <= row && row < board.size()) && (0 <= col && col < board.get(0).size());
  }

  // places power station on this GamePiece
  public void placePowerStation() {
    this.powerStation = true;

  }

}

// class representing the Light Em All game
class LightEmAll extends World {

  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;

  // list of all GamePiece on board
  ArrayList<GamePiece> nodes;

  // the width and height of the board
  int width;
  int height;

  // location of power station
  int powerRow;
  int powerCol;

  // power of effectiveness for power station
  int radius;

  // put restrictions on given width and height because if the board was 1 x 1 or
  // smaller, then it is impossible
  // for the player to win, since the power station would be on the only
  // GamePiece, or there are no GamePieces at all
  LightEmAll(int width, int height) {
    if (width >= 2 && height >= 1 || width >= 1 && height >= 2) {
      this.width = width;
      this.height = height;
      this.powerRow = this.width / 2;
      this.powerCol = 0;
      this.nodes = new ArrayList<GamePiece>();
      this.board = this.makeBoard();
      this.linkGamePieces();
      this.createWires();
      this.placePowerStation();
      this.radius = this.findRadius();
    }
    else {
      throw new IllegalArgumentException("The board must be atleast 2 x 1 in size");
    }
  }

  // places the power station in the appropriate place
  // EFFCT : Mutates the appropriate GamePiece in this board to hold power station
  void placePowerStation() {
    this.board.get(powerRow).get(powerCol).placePowerStation();
  }

  // rotates the GamePiece that the mouse click corresponds to
  // EFFECT: Rotates the GamePiece clicked on, mutating the world state and
  // increments clicked field
  public void onMouseClicked(Posn p) {
    Posn temp = this.getGamePiece(p);
    int tempx = temp.x;
    int tempy = temp.y;
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < height; j++) {
        if (tempx == i && tempy == j) {
          this.board.get(i).get(j).rotatePiece();
        }
      }
    }
  }

  // moves the power station according to the key press
  // EFFECT : Mutates where the power station is on the board
  public void onKeyEvent(String key) {
    if (this.board.get(powerRow).get(powerCol).hasNeighbor(key)) {
      if (key.equals("right") && this.board.get(powerRow).get(powerCol).connected("right")) {
        this.board.get(powerRow).get(powerCol).powerStation = false;
        powerRow++;
        this.placePowerStation();
      }
      else if (key.equals("left") && this.board.get(powerRow).get(powerCol).connected("left")) {
        this.board.get(powerRow).get(powerCol).powerStation = false;
        powerRow--;
        this.placePowerStation();
      }
      else if (key.equals("up") && this.board.get(powerRow).get(powerCol).connected("up")) {
        this.board.get(powerRow).get(powerCol).powerStation = false;
        powerCol--;
        this.placePowerStation();
      }
      else if (key.equals("down") && this.board.get(powerRow).get(powerCol).connected("down")) {
        this.board.get(powerRow).get(powerCol).powerStation = false;
        powerCol++;
        this.placePowerStation();
      }
    }
  }

  // converts the computer coordinates into the logical coordinates of the board
  Posn getGamePiece(Posn p) {
    return new Posn(p.x / GamePiece.GAMEPIECE_SIZE, p.y / GamePiece.GAMEPIECE_SIZE);
  }

  // initializes the board to contain new GamePieces
  ArrayList<ArrayList<GamePiece>> makeBoard() {
    ArrayList<ArrayList<GamePiece>> row = new ArrayList<ArrayList<GamePiece>>();
    for (int i = 0; i < this.width; i++) {
      ArrayList<GamePiece> column = new ArrayList<GamePiece>();
      for (int j = 0; j < this.height; j++) {
        GamePiece temp = new GamePiece(i, j);
        column.add(temp);
        nodes.add(temp);
      }
      row.add(column);
    }
    return row;
  }

  // Initializes wires of the board to follow fractal pattern
  // EFFECT: Mutates fields of each GamePiece in the board of this LightEmAll
  public void splitBoard(Posn start, int currRows, int currCols) {
    int startRow = start.x;
    int startCol = start.y;
    if (currRows != 1 && currCols != 1) {
      // top left piece
      this.board.get(startCol).get(startRow).down = true; 

      // top right piece
      this.board.get(startCol + currCols - 1).get(startRow).down = true; 

      // bottom left right
      this.board.get(startCol).get(startRow + currRows - 1).right = true; 
      this.board.get(startCol).get(startRow + currRows - 1).up = true; // bottom left up

      // bottom, right, left
      this.board.get(startCol + currCols - 1).get(startRow + currRows - 1).left = true; 
      // bottom, right, up
      this.board.get(startCol + currCols - 1).get(startRow + currRows - 1).up = true; 

      // connect middle pieces on both sides of subgrid
      for (int i = startRow + 1; i < startRow + currRows - 1; i++) { 
        this.board.get(startCol).get(i).up = true;
        this.board.get(startCol).get(i).down = true;
        this.board.get(startCol + currCols - 1).get(i).up = true;
        this.board.get(startCol + currCols - 1).get(i).down = true;
      }
      // connect middle pieces on bottom of subgrid
      for (int i = startCol + 1; i < startCol + currCols - 1; i++) { 
        this.board.get(i).get(startRow + currRows - 1).left = true;
        this.board.get(i).get(startRow + currRows - 1).right = true;
      }
    }

    // grid base cases
    if (this.height == 1) {
      for (ArrayList<GamePiece> p : this.board) {
        if (p.get(0).row == 0) {
          p.get(0).right = true;
        }
        else if (p.get(0).row == this.width - 1) {
          p.get(0).left = true;
        }
        else {
          p.get(0).right = true;
          p.get(0).left = true;
        }
      }
    }
    else if (this.width == 1) {
      for (GamePiece p : this.board.get(0)) {
        if (p.col == 0) {
          p.down = true;
        }
        else if (p.col == this.height - 1) {
          p.up = true;
        }
        else {
          p.down = true;
          p.up = true;
        }
      }
    }
    else if (currRows == 2) {
      for (int i = startCol + 1; i < startCol + currCols - 1; i++) {
        this.board.get(i).get(startRow).down = true;
        this.board.get(i).get(startRow + 1).up = true;
      }
    }
    else if (currRows == 3 && currCols == 3) { // base case for odd grid
      this.board.get(startCol).get(startRow + 1).right = true;
      this.board.get(startCol + 1).get(startRow + 1).left = true;
      this.board.get(startCol + 1).get(startRow + 1).up = true;
      this.board.get(startCol + 1).get(startRow).down = true;

    }
    // recur if the grid can be divided into 4 non-overlapping quadrants
    else if (currRows > 3 || currCols > 3) { 
      this.splitBoard(new Posn(startRow, startCol), (int) Math.ceil(currRows / 2.0),
          (int) Math.ceil(currCols / 2.0));
      this.splitBoard(new Posn(startRow + (int) Math.ceil(currRows / 2.0), startCol), currRows / 2,
          (int) Math.ceil(currCols / 2.0));
      this.splitBoard(new Posn(startRow + (int) Math.ceil(currRows / 2.0),
          startCol + (int) Math.ceil(currCols / 2.0)), currRows / 2, currCols / 2);
      this.splitBoard(new Posn(startRow, startCol + (int) Math.ceil(currCols / 2.0)),
          (int) Math.ceil(currRows / 2.0), currCols / 2);
    }
  }

  // gives each GamePiece in the board its wires to create the initial board
  // EFFECT : Mutates the value of each GamePiece in the board to hold wire values
  void createWires() {
    this.splitBoard(new Posn(0, 0), this.height, this.width);
    int counter = 0;
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        nodes.set(counter, this.board.get(i).get(j));
        counter++;
      }
    }
  }

  // finds the appropriate radius for the given board
  // based on the distance of the farthest piece from the power station,
  // to its farthest piece
  int findRadius() {
    GraphUtils u = new GraphUtils();
    int diameter = u.furthestDist(
        u.furthestPiece(u.furthestPiece(this.board.get(powerRow).get(powerCol), this.nodes),
            this.nodes),
        this.nodes);
    return diameter / 2 + 1;
  }

  // links every GamePiece to their neighboring GamePiece
  // EFFECT: Makes every GamePiece know who their neighbors are
  public void linkGamePieces() {
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.board.get(i).get(j).linkGamePieceHelp(i, j, this.board);
      }
    }
  }

  // renders the world as an image
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width * GamePiece.GAMEPIECE_SIZE,
        this.height * GamePiece.GAMEPIECE_SIZE);
    WorldImage row = new EmptyImage();
    for (int i = 0; i < this.width; i++) {
      WorldImage column = new EmptyImage();
      for (int j = 0; j < this.height; j++) {
        column = new AboveImage(column, this.board.get(i).get(j)
            .drawPiece(this.board.get(powerRow).get(powerCol), this.radius));
      }
      row = new BesideImage(row, column);
    }
    scene.placeImageXY(row, (this.width * GamePiece.GAMEPIECE_SIZE) / 2,
        (this.height * GamePiece.GAMEPIECE_SIZE) / 2);
    return scene;
  }
}

class ExamplesLightEmAll {

  GamePiece gamePiece1;
  GamePiece gamePiece2;
  GamePiece gamePiece3;
  GamePiece gamePiece4;
  GamePiece gamePiece5;
  GamePiece gamePiece6;
  GamePiece gamePiece7;
  GamePiece gamePiece8;
  GamePiece gamePiece9;
  LightEmAll light1;
  LightEmAll light2;
  LightEmAll light3;
  LightEmAll light4;
  LightEmAll light5;
  LightEmAll light6;
  LightEmAll light7;
  LightEmAll light8;
  GraphUtils u;

  void initData() {
    gamePiece1 = new GamePiece(0, 0);
    gamePiece2 = new GamePiece(0, 1);
    gamePiece3 = new GamePiece(0, 2);
    gamePiece4 = new GamePiece(1, 0);
    gamePiece5 = new GamePiece(1, 1);
    gamePiece6 = new GamePiece(1, 2);
    gamePiece7 = new GamePiece(2, 0);
    gamePiece8 = new GamePiece(2, 1);
    gamePiece9 = new GamePiece(2, 2);

    light1 = new LightEmAll(8, 9);
    light2 = new LightEmAll(2, 2);
    light3 = new LightEmAll(3, 3);
    light4 = new LightEmAll(1, 2);
    light5 = new LightEmAll(2, 3);
    light6 = new LightEmAll(4, 6);
    light7 = new LightEmAll(8, 12);
    light8 = new LightEmAll(8, 8);

    u = new GraphUtils();
  }

  void testBigBang(Tester t) {
    LightEmAll wow = new LightEmAll(4, 4);
    int worldWidth = GamePiece.GAMEPIECE_SIZE * 4;
    int worldHeight = GamePiece.GAMEPIECE_SIZE * 4;
    double tickRate = 0.5;
    wow.bigBang(worldWidth, worldHeight, tickRate);
  }

  // GraphUtils Testers-----------------------------------

  // test furthestPiece
  void testFurthestPiece(Tester t) {
    initData();
    t.checkExpect(
        u.furthestPiece(light3.board.get(light3.powerRow).get(light3.powerCol), light3.nodes),
        light3.board.get(2).get(0));
    t.checkExpect(u.furthestPiece(light3.board.get(2).get(0), light3.nodes),
        light3.board.get(1).get(0));
    t.checkExpect(u.furthestPiece(light2.board.get(1).get(0), light2.nodes),
        light2.board.get(0).get(0));
  }

  // test furthestDist
  void testFurthestDist(Tester t) {
    initData();
    t.checkExpect(
        u.furthestDist(light3.board.get(light3.powerRow).get(light3.powerCol), light3.nodes), 7);
    t.checkExpect(u.furthestDist(light2.board.get(1).get(0), light2.nodes), 3);
    t.checkExpect(u.furthestDist(light8.board.get(4).get(0), light8.nodes), 28);
    t.checkExpect(u.furthestDist(light1.board.get(4).get(0), light1.nodes), 30);
  }

  // test distanceMap
  void testDistanceMap(Tester t) {
    initData();
    HashMap<GamePiece, Integer> co = u
        .distanceMap(light8.board.get(light8.powerRow).get(light8.powerCol));
    t.checkExpect(co.get(light8.board.get(0).get(0)), 24);
    t.checkExpect(co.get(light8.board.get(light8.powerRow).get(light8.powerCol)), 0);
    t.checkExpect(co.get(light8.board.get(4).get(1)), 1);
    t.checkExpect(co.get(light8.board.get(5).get(1)), 2);
    t.checkExpect(co.get(light8.board.get(6).get(3)), 5);
    t.checkExpect(co.get(light8.board.get(7).get(7)), 10);
    t.checkExpect(co.get(light8.board.get(4).get(7)), 13);
    t.checkExpect(co.get(light8.board.get(3).get(2)), 25);
  }

  // test bfs
  void testBFS(Tester t) {
    initData();
    HashMap<GamePiece, GamePiece> co = u
        .bfs(light8.board.get(light8.powerRow).get(light8.powerCol));
    t.checkExpect(co.size(), light8.width * light8.height);
    // test that checks that everything in the board is connected to the power,
    // as through the hash map, it'll go through and add everything that is connected
    // directly or indirectly to power
    // because of fractal, we know everything is initially connected
    boolean x = true;
    for (GamePiece p : light8.nodes) {
      if (co.containsKey(p) && x) {
        // if the hash map contains the key and its true don't do anything
      }
      else {
        x = false;
      }
    }
    t.checkExpect(x, true);

  }

  // GamePiece Testers -----------------------------------------------------

  //tests rotatePiece
  void testRotatePiece(Tester t) {
    initData();
    gamePiece1.left = true;
    gamePiece1.rotatePiece();
    t.checkExpect(gamePiece1.left, false);
    t.checkExpect(gamePiece1.up, true);
    gamePiece1.down = true;
    gamePiece1.rotatePiece();
    t.checkExpect(gamePiece1.up, false);
    t.checkExpect(gamePiece1.down, false);
    t.checkExpect(gamePiece1.left, true);
    t.checkExpect(gamePiece1.right, true);
    gamePiece2.right = true;
    gamePiece2.left = true;
    gamePiece2.up = true;
    gamePiece2.rotatePiece();
    t.checkExpect(gamePiece2.down, true);
    t.checkExpect(gamePiece2.up, true);
    t.checkExpect(gamePiece2.left, false);
    t.checkExpect(gamePiece2.right, true);
  }

  // tests hasNeighbor
  void testHasNeighbor(Tester t) {
    initData();
    gamePiece1.neighbors.put("right", gamePiece4);
    gamePiece1.neighbors.put("right", gamePiece5);
    gamePiece1.neighbors.put("down", new GamePiece(1, 0));
    gamePiece1.neighbors.put("hello", gamePiece3);
    t.checkExpect(gamePiece1.hasNeighbor("right"), true);
    t.checkExpect(gamePiece4.hasNeighbor("left"), false);
    t.checkExpect(gamePiece1.hasNeighbor("down"), true);
    t.checkExpect(gamePiece1.hasNeighbor("hello"), true);
  }

  // tests connected
  void testConnected(Tester t) {
    initData();
    gamePiece1.neighbors.put("right", gamePiece4);
    gamePiece1.right = true;
    gamePiece4.left = true;
    t.checkExpect(gamePiece1.connected("right"), true);
    gamePiece1.neighbors.put("up", gamePiece2);
    gamePiece1.up = true;
    gamePiece2.down = true;
    t.checkExpect(gamePiece1.connected("up"), true);
    gamePiece1.neighbors.put("down", gamePiece3);
    gamePiece1.down = true;
    gamePiece3.up = true;
    t.checkExpect(gamePiece1.connected("down"), true);
    gamePiece1.neighbors.put("left", gamePiece5);
    gamePiece1.left = true;
    gamePiece5.right = true;
    t.checkExpect(gamePiece1.connected("left"), true);
    gamePiece2.neighbors.put("up", gamePiece6);
    t.checkExpect(gamePiece2.connected("bottom"), false);
  }

  // tests drawPiece
  void testDrawPiece(Tester t) {
    initData();
    gamePiece1.left = true;
    gamePiece9.placePowerStation();
    t.checkExpect(gamePiece1.drawPiece(gamePiece9, 3),
        new OverlayOffsetAlign(
            AlignModeX.LEFT, AlignModeY.MIDDLE, new RectangleImage(50 / 2 + 2, 50 / 10,
                OutlineMode.SOLID, gamePiece1.chooseColor(gamePiece9, 3)),
            0, 0, gamePiece1.SQUARE));
    gamePiece2.right = true;
    t.checkExpect(gamePiece2.drawPiece(gamePiece9, 3),
        new OverlayOffsetAlign(
            AlignModeX.RIGHT, AlignModeY.MIDDLE, new RectangleImage(50 / 2 + 2, 50 / 10,
                OutlineMode.SOLID, gamePiece2.chooseColor(gamePiece9, 3)),
            0, 0, gamePiece2.SQUARE));
    gamePiece3.up = true;
    t.checkExpect(gamePiece3.drawPiece(gamePiece9, 3),
        new OverlayOffsetAlign(
            AlignModeX.CENTER, AlignModeY.TOP, new RectangleImage(50 / 10, 50 / 2 + 2,
                OutlineMode.SOLID, gamePiece3.chooseColor(gamePiece9, 3)),
            0, 0, gamePiece3.SQUARE));
    gamePiece4.down = true;
    t.checkExpect(gamePiece4.drawPiece(gamePiece9, 3),
        new OverlayOffsetAlign(
            AlignModeX.CENTER, AlignModeY.BOTTOM, new RectangleImage(50 / 10, 50 / 2 + 2,
                OutlineMode.SOLID, gamePiece4.chooseColor(gamePiece9, 3)),
            0, 0, gamePiece4.SQUARE));
    t.checkExpect(gamePiece5.drawPiece(gamePiece9, 3), gamePiece5.SQUARE);
    t.checkExpect(gamePiece9.drawPiece(gamePiece9, 3),
        new OverlayImage(gamePiece9.POWER_STATION, gamePiece9.SQUARE));
  }

  // tests chooseColor
  void testChooseColor(Tester t) {
    initData();
    t.checkExpect(gamePiece1.connectedToPower(gamePiece3), false);
    t.checkExpect(gamePiece1.chooseColor(gamePiece3, 10), gamePiece1.OFF_WIRE_COLOR);

    gamePiece1.placePowerStation();
    gamePiece1.down = true;
    gamePiece2.up = true;
    gamePiece2.right = true;
    gamePiece3.left = true;
    gamePiece1.neighbors.put("down", gamePiece2);
    gamePiece2.neighbors.put("up", gamePiece1);
    gamePiece2.neighbors.put("right", gamePiece3);
    gamePiece3.neighbors.put("left", gamePiece2);

    t.checkExpect(gamePiece2.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece3.chooseColor(gamePiece1, 9), gamePiece1.FULLPOWER_WIRE_COLOR);
    t.checkExpect(gamePiece3.chooseColor(gamePiece6, 3), gamePiece6.OFF_WIRE_COLOR);
  }

  // tests distanceFromPower
  void testDistanceFromPower(Tester t) {
    initData();
    gamePiece1.placePowerStation();
    gamePiece1.down = true;
    gamePiece1.left = true;
    gamePiece1.right = true;
    gamePiece1.up = true;
    gamePiece2.up = true;
    gamePiece2.right = true;
    gamePiece6.up = true;
    gamePiece3.left = true;
    gamePiece4.left = true;
    gamePiece5.down = true;
    gamePiece9.right = true;
    gamePiece1.neighbors.put("down", gamePiece2);
    gamePiece2.neighbors.put("up", gamePiece1);
    gamePiece2.neighbors.put("right", gamePiece3);
    gamePiece3.neighbors.put("left", gamePiece2);
    gamePiece1.neighbors.put("left", gamePiece9);
    gamePiece9.neighbors.put("right", gamePiece1);
    gamePiece1.neighbors.put("right", gamePiece4);
    gamePiece4.neighbors.put("left", gamePiece1);
    gamePiece1.neighbors.put("up", gamePiece5);
    gamePiece5.neighbors.put("down", gamePiece1);
    gamePiece2.neighbors.put("down", gamePiece6);
    gamePiece6.neighbors.put("up", gamePiece2);
    gamePiece7.neighbors.put("left", gamePiece1);

    t.checkExpect(gamePiece8.distanceFromPower(gamePiece1), -1);
    t.checkExpect(gamePiece1.distanceFromPower(gamePiece1), 0);
    t.checkExpect(gamePiece2.distanceFromPower(gamePiece1), 1);
    t.checkExpect(gamePiece3.distanceFromPower(gamePiece1), 2);
    t.checkExpect(gamePiece1.distanceFromPower(gamePiece3), 2);
    t.checkExpect(gamePiece5.distanceFromPower(gamePiece1), 1);
    t.checkExpect(gamePiece4.distanceFromPower(gamePiece1), 1);
    t.checkExpect(gamePiece9.distanceFromPower(gamePiece1), 1);
  }

  // tests connectedToPower
  void testConnectedToPower(Tester t) {
    initData();
    gamePiece1.placePowerStation();
    gamePiece1.down = true;
    gamePiece1.left = true;
    gamePiece1.right = true;
    gamePiece1.up = true;
    gamePiece2.up = true;
    gamePiece2.down = true;
    gamePiece6.up = true;
    gamePiece3.right = true;
    gamePiece4.left = true;
    gamePiece5.down = true;
    gamePiece1.neighbors.put("down", gamePiece2);
    gamePiece2.neighbors.put("up", gamePiece1);
    gamePiece1.neighbors.put("left", gamePiece3);
    gamePiece3.neighbors.put("right", gamePiece1);
    gamePiece1.neighbors.put("right", gamePiece4);
    gamePiece4.neighbors.put("left", gamePiece1);
    gamePiece1.neighbors.put("up", gamePiece5);
    gamePiece5.neighbors.put("down", gamePiece1);
    gamePiece2.neighbors.put("down", gamePiece6);
    gamePiece6.neighbors.put("up", gamePiece2);
    gamePiece7.neighbors.put("left", gamePiece1);

    t.checkExpect(gamePiece1.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece1.connectedToPower(gamePiece2), true);

    t.checkExpect(gamePiece2.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece3.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece4.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece5.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece6.connectedToPower(gamePiece1), true);
    t.checkExpect(gamePiece7.connectedToPower(gamePiece1), false);
    t.checkExpect(gamePiece7.connectedToPower(gamePiece1), false);
  }

  // tests placePowerStation
  void testPlacePowerStation(Tester t) {
    initData();
    t.checkExpect(gamePiece1.powerStation, false);
    gamePiece1.placePowerStation();
    t.checkExpect(gamePiece1.powerStation, true);
  }

  // tests coordOnBoard
  void testCoordOnBoard(Tester t) {
    initData();
    t.checkExpect(gamePiece1.coordOnBoard(0, 0, light1.board), true);
    t.checkExpect(gamePiece1.coordOnBoard(8, 9, light1.board), false);
    t.checkExpect(gamePiece1.coordOnBoard(3, 4, light1.board), true);
    t.checkExpect(gamePiece1.coordOnBoard(-1, 0, light1.board), false);
    t.checkExpect(gamePiece1.coordOnBoard(0, -1, light1.board), false);
    t.checkExpect(gamePiece1.coordOnBoard(7, 9, light1.board), false);
    t.checkExpect(gamePiece1.coordOnBoard(8, 8, light1.board), false);
    t.checkExpect(gamePiece1.coordOnBoard(8, 7, light1.board), false);
  }

  // tests linkGamePieceHelp
  void testLinkGamePieceHelp(Tester t) {
    initData();
    light2.board = light2.makeBoard();
    t.checkExpect(gamePiece1.neighbors.size(), 0);
    t.checkExpect(gamePiece2.neighbors.size(), 0);
    t.checkExpect(gamePiece4.neighbors.size(), 0);
    t.checkExpect(gamePiece5.neighbors.size(), 0);
    gamePiece1.linkGamePieceHelp(0, 0, light2.board);
    gamePiece2.linkGamePieceHelp(0, 1, light2.board);
    gamePiece4.linkGamePieceHelp(1, 0, light2.board);
    gamePiece5.linkGamePieceHelp(1, 1, light2.board);
    t.checkExpect(gamePiece1.neighbors.size(), 2);
    t.checkExpect(gamePiece2.neighbors.size(), 2);
    t.checkExpect(gamePiece4.neighbors.size(), 2);
    t.checkExpect(gamePiece5.neighbors.size(), 2);

    // checking the first corner GamePiece has its neighbors in its list
    t.checkExpect(gamePiece1.neighbors.get("up"), null);
    t.checkExpect(gamePiece1.neighbors.get("right"), light2.board.get(1).get(0));
    t.checkExpect(gamePiece1.neighbors.get("down"), light2.board.get(0).get(1));
    t.checkExpect(gamePiece1.neighbors.get("left"), null);

    // checking the second corner GamePiece has its neighbors in its list
    t.checkExpect(gamePiece2.neighbors.get("up"), light2.board.get(0).get(0));
    t.checkExpect(gamePiece2.neighbors.get("left"), null);
    t.checkExpect(gamePiece2.neighbors.get("down"), null);
    t.checkExpect(gamePiece2.neighbors.get("right"), light2.board.get(1).get(1));

    // checking the third corner GamePiece has its neighbors in its list
    t.checkExpect(gamePiece4.neighbors.get("up"), null);
    t.checkExpect(gamePiece4.neighbors.get("right"), null);
    t.checkExpect(gamePiece4.neighbors.get("down"), light2.board.get(1).get(1));
    t.checkExpect(gamePiece4.neighbors.get("left"), light2.board.get(0).get(0));

    // checking the fourth corner GamePiece has its neighbors in its list
    t.checkExpect(gamePiece5.neighbors.get("up"), light2.board.get(1).get(0));
    t.checkExpect(gamePiece5.neighbors.get("left"), light2.board.get(0).get(1));
    t.checkExpect(gamePiece5.neighbors.get("right"), null);
    t.checkExpect(gamePiece5.neighbors.get("down"), null);

    initData();
    light3.board = light3.makeBoard();
    t.checkExpect(gamePiece1.neighbors.size(), 0);
    t.checkExpect(gamePiece2.neighbors.size(), 0);
    t.checkExpect(gamePiece4.neighbors.size(), 0);
    t.checkExpect(gamePiece5.neighbors.size(), 0);
    t.checkExpect(gamePiece6.neighbors.size(), 0);
    gamePiece1.linkGamePieceHelp(0, 1, light3.board);
    gamePiece2.linkGamePieceHelp(1, 0, light3.board);
    gamePiece4.linkGamePieceHelp(1, 2, light3.board);
    gamePiece5.linkGamePieceHelp(2, 1, light3.board);
    gamePiece6.linkGamePieceHelp(1, 1, light3.board);
    t.checkExpect(gamePiece1.neighbors.size(), 3);
    t.checkExpect(gamePiece2.neighbors.size(), 3);
    t.checkExpect(gamePiece4.neighbors.size(), 3);
    t.checkExpect(gamePiece5.neighbors.size(), 3);
    t.checkExpect(gamePiece6.neighbors.size(), 4);

    // checking the first top center GamePiece has its neighbors in its list
    t.checkExpect(gamePiece1.neighbors.get("bottom"), null);
    t.checkExpect(gamePiece1.neighbors.get("right"), light3.board.get(1).get(1));
    t.checkExpect(gamePiece1.neighbors.get("left"), null);

    // checking the bottom center GamePiece has its neighbors in its list
    t.checkExpect(gamePiece4.neighbors.get("bottom"), null);
    t.checkExpect(gamePiece4.neighbors.get("top"), null);
    t.checkExpect(gamePiece4.neighbors.get("left"), light3.board.get(0).get(2));

    // checking the left center GamePiece has its neighbors in its list
    t.checkExpect(gamePiece5.neighbors.get("top"), null);
    t.checkExpect(gamePiece5.neighbors.get("right"), null);
    t.checkExpect(gamePiece5.neighbors.get("left"), light3.board.get(1).get(1));

    // checking the center GamePiece has its neighbors in its list
    t.checkExpect(gamePiece6.neighbors.get("bottom"), null);
    t.checkExpect(gamePiece6.neighbors.get("top"), null);
    t.checkExpect(gamePiece6.neighbors.get("right"), light3.board.get(2).get(1));
    t.checkExpect(gamePiece6.neighbors.get("left"), light3.board.get(0).get(1));
  }
  // LightEmAll testers ------------------------------

  // test constructor restrictions
  void testConstructor(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("The board must be atleast 2 x 1 in size"), "LightEmAll", 0,
        0);
    t.checkConstructorException(
        new IllegalArgumentException("The board must be atleast 2 x 1 in size"), "LightEmAll", 1,
        0);
    t.checkConstructorException(
        new IllegalArgumentException("The board must be atleast 2 x 1 in size"), "LightEmAll", 0,
        1);
    t.checkConstructorException(
        new IllegalArgumentException("The board must be atleast 2 x 1 in size"), "LightEmAll", 1,
        1);
    t.checkConstructorException(
        new IllegalArgumentException("The board must be atleast 2 x 1 in size"), "LightEmAll", -5,
        -10);
  }

  // test placePowerStation
  void testPlacePowerStation2(Tester t) {
    initData();
    t.checkExpect(light3.powerRow, 1);
    t.checkExpect(light3.powerCol, 0);
    light3.board.get(1).get(0).powerStation = false;
    light3.powerRow = 2;
    light3.powerCol = 2;
    light3.placePowerStation();
    t.checkExpect(light3.powerRow, 2);
    t.checkExpect(light3.powerCol, 2);
    t.checkExpect(light3.board.get(2).get(2).powerStation, true);

    t.checkExpect(light1.powerRow, 4);
    t.checkExpect(light1.powerCol, 0);
    light1.board.get(4).get(0).powerStation = false;
    light1.powerRow = 5;
    light1.powerCol = 2;
    light1.placePowerStation();
    t.checkExpect(light1.powerRow, 5);
    t.checkExpect(light1.powerCol, 2);
    t.checkExpect(light1.board.get(5).get(2).powerStation, true);
  }

  // test onMouseClicked
  void testOnMouseClicked(Tester t) {
    initData();

    // before mouse clicked at (1,2), GamePiece at (1,2) has a single wire set
    light3.board.get(1).get(2).left = true;
    t.checkExpect(light3.board.get(1).get(2).left, true);
    t.checkExpect(light3.board.get(1).get(2).right, true);
    t.checkExpect(light3.board.get(1).get(2).up, false);
    t.checkExpect(light3.board.get(1).get(2).down, false);

    // after mouse clicked, the single on wire is rotated clockwise
    light3.onMouseClicked(new Posn(50, 100));
    t.checkExpect(light3.board.get(1).get(2).left, false);
    t.checkExpect(light3.board.get(1).get(2).right, false);
    t.checkExpect(light3.board.get(1).get(2).up, true);
    t.checkExpect(light3.board.get(1).get(2).down, true);

    // before mouse clicked at (2,2), GamePiece has two wires set
    light3.board.get(2).get(2).left = true;
    light3.board.get(2).get(2).down = true;
    t.checkExpect(light3.board.get(2).get(2).left, true);
    t.checkExpect(light3.board.get(2).get(2).right, false);
    t.checkExpect(light3.board.get(2).get(2).up, true);
    t.checkExpect(light3.board.get(2).get(2).down, true);

    // after mouse clicked, those 2 wires are rotated clockwise
    light3.onMouseClicked(new Posn(100, 100));
    t.checkExpect(light3.board.get(2).get(2).left, true);
    t.checkExpect(light3.board.get(2).get(2).right, true);
    t.checkExpect(light3.board.get(2).get(2).up, true);
    t.checkExpect(light3.board.get(2).get(2).down, false);

    // before mouse clicked at (0,0), GamePiece has 3 wires set
    light3.board.get(0).get(0).down = true;
    light3.board.get(0).get(0).up = true;
    light3.board.get(0).get(0).right = true;
    t.checkExpect(light3.board.get(0).get(0).left, false);
    t.checkExpect(light3.board.get(0).get(0).right, true);
    t.checkExpect(light3.board.get(0).get(0).up, true);
    t.checkExpect(light3.board.get(0).get(0).down, true);

    // after mouse clicked, those 3 wires are rotated clockwise
    light3.onMouseClicked(new Posn(0, 0));
    t.checkExpect(light3.board.get(0).get(0).left, true);
    t.checkExpect(light3.board.get(0).get(0).right, true);
    t.checkExpect(light3.board.get(0).get(0).up, false);
    t.checkExpect(light3.board.get(0).get(0).down, true);

    // before mouse clicked at (1,1), GamePiece has 4 wires set
    light3.board.get(1).get(1).down = true;
    light3.board.get(1).get(1).up = true;
    light3.board.get(1).get(1).right = true;
    light3.board.get(1).get(1).left = true;
    t.checkExpect(light3.board.get(1).get(1).left, true);
    t.checkExpect(light3.board.get(1).get(1).right, true);
    t.checkExpect(light3.board.get(1).get(1).up, true);
    t.checkExpect(light3.board.get(1).get(1).down, true);

    // after mouse clicked, those 4 wires are rotated clockwise
    light3.onMouseClicked(new Posn(50, 50));
    t.checkExpect(light3.board.get(1).get(1).left, true);
    t.checkExpect(light3.board.get(1).get(1).right, true);
    t.checkExpect(light3.board.get(1).get(1).up, true);
    t.checkExpect(light3.board.get(1).get(1).down, true);
  }

  // test onKeyEvent
  void testOnKeyEvent(Tester t) {
    initData();
    t.checkExpect(light6.powerRow, 2);
    t.checkExpect(light6.powerCol, 0);
    light6.onKeyEvent("down");
    t.checkExpect(light6.powerRow, 2);
    t.checkExpect(light6.powerCol, 1);
    light6.onKeyEvent("left");
    t.checkExpect(light6.powerRow, 2);
    t.checkExpect(light6.powerCol, 1);
    light6.onKeyEvent("down");
    t.checkExpect(light6.powerRow, 2);
    t.checkExpect(light6.powerCol, 2);
    light6.onKeyEvent("right");
    t.checkExpect(light6.powerRow, 3);
    t.checkExpect(light6.powerCol, 2);
  }

  // test getGamePiece
  void testGetGamePiece(Tester t) {
    initData();
    t.checkExpect(light1.getGamePiece(new Posn(0, 0)), new Posn(0, 0));
    t.checkExpect(light1.getGamePiece(new Posn(40, 40)), new Posn(0, 0));
    t.checkExpect(light1.getGamePiece(new Posn(200, 20)), new Posn(4, 0));
    t.checkExpect(light1.getGamePiece(new Posn(30, 124)), new Posn(0, 2));
    t.checkExpect(light1.getGamePiece(new Posn(360, 440)), new Posn(7, 8));
    t.checkExpect(light1.getGamePiece(new Posn(346, 23)), new Posn(6, 0));
  }

  // test makeBoard
  void testMakeBoard(Tester t) {
    initData();
    t.checkExpect(light4.height, 2);
    t.checkExpect(light4.width, 1);
    ArrayList<GamePiece> list01 = new ArrayList<GamePiece>(
        Arrays.asList(new GamePiece(0, 0), new GamePiece(0, 1)));
    t.checkExpect(light4.makeBoard(), new ArrayList<ArrayList<GamePiece>>(Arrays.asList(list01)));
    t.checkExpect(light2.height, 2);
    t.checkExpect(light2.width, 2);
    ArrayList<GamePiece> list1 = new ArrayList<GamePiece>(
        Arrays.asList(new GamePiece(0, 0), new GamePiece(0, 1)));
    ArrayList<GamePiece> list2 = new ArrayList<GamePiece>(
        Arrays.asList(new GamePiece(1, 0), new GamePiece(1, 1)));
    t.checkExpect(light2.makeBoard(),
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(list1, list2)));
    t.checkExpect(light5.height, 3);
    t.checkExpect(light5.width, 2);
    ArrayList<GamePiece> list3 = new ArrayList<GamePiece>(
        Arrays.asList(new GamePiece(0, 0), new GamePiece(0, 1), new GamePiece(0, 2)));
    ArrayList<GamePiece> list4 = new ArrayList<GamePiece>(
        Arrays.asList(new GamePiece(1, 0), new GamePiece(1, 1), new GamePiece(1, 2)));
    t.checkExpect(light5.makeBoard(),
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(list3, list4)));
  }

  // test splitBoard
  void testSplitBoard(Tester t) {
    initData();
    // checking for width 1 base case
    light4.board.get(0).get(0).down = false;
    light4.board.get(0).get(1).up = false;
    light4.splitBoard(new Posn(0, 0), 1, 2);
    t.checkExpect(light4.board.get(0).get(0).down, true);
    t.checkExpect(light4.board.get(0).get(1).up, true);

    // checking for height 1 base case
    LightEmAll twoBy1 = new LightEmAll(2, 1);
    twoBy1.board.get(0).get(0).right = false;
    twoBy1.board.get(1).get(0).left = false;
    twoBy1.splitBoard(new Posn(0, 0), 2, 1);
    t.checkExpect(twoBy1.board.get(0).get(0).right, true);
    t.checkExpect(twoBy1.board.get(1).get(0).left, true);

    // checking for 3 x 3 base case
    light3.board.get(0).get(1).right = false;
    light3.board.get(1).get(1).left = false;
    light3.board.get(1).get(1).up = false;
    light3.board.get(1).get(0).down = false;
    light3.splitBoard(new Posn(0, 0), 3, 3);
    t.checkExpect(light3.board.get(0).get(1).right, true);
    t.checkExpect(light3.board.get(1).get(1).left, true);
    t.checkExpect(light3.board.get(1).get(1).up, true);
    t.checkExpect(light3.board.get(1).get(0).down, true);

    // checking 2 x 2 grid
    LightEmAll twoBy2 = new LightEmAll(2, 2);
    twoBy2.board.get(1).get(0).down = false;
    twoBy2.board.get(1).get(1).up = false;
    twoBy2.splitBoard(new Posn(0, 0), 2, 2);
    t.checkExpect(twoBy2.board.get(1).get(0).down, true);
    t.checkExpect(twoBy2.board.get(1).get(1).up, true);

    // checking large grid size
    LightEmAll fourBy4 = new LightEmAll(4, 4);
    // before split
    fourBy4.board.get(0).get(0).down = false;
    fourBy4.board.get(1).get(0).down = false;
    fourBy4.board.get(2).get(0).down = false;
    fourBy4.board.get(3).get(0).down = false;
    fourBy4.board.get(0).get(1).down = false;
    fourBy4.board.get(0).get(1).up = false;
    fourBy4.board.get(0).get(1).right = false;
    fourBy4.board.get(1).get(1).up = false;
    fourBy4.board.get(1).get(1).left = false;
    fourBy4.board.get(2).get(1).up = false;
    fourBy4.board.get(2).get(1).right = false;
    fourBy4.board.get(3).get(1).up = false;
    fourBy4.board.get(3).get(1).down = false;
    fourBy4.board.get(3).get(1).left = false;
    fourBy4.board.get(0).get(2).down = false;
    fourBy4.board.get(0).get(2).up = false;
    fourBy4.board.get(1).get(2).down = false;
    fourBy4.board.get(2).get(2).down = false;
    fourBy4.board.get(3).get(2).down = false;
    fourBy4.board.get(3).get(2).up = false;
    fourBy4.board.get(0).get(3).left = false;
    fourBy4.board.get(0).get(3).right = false;
    fourBy4.board.get(1).get(3).up = false;
    fourBy4.board.get(1).get(3).left = false;
    fourBy4.board.get(1).get(3).right = false;
    fourBy4.board.get(2).get(3).up = false;
    fourBy4.board.get(2).get(3).left = false;
    fourBy4.board.get(2).get(3).right = false;
    fourBy4.board.get(3).get(3).up = false;
    fourBy4.board.get(3).get(3).left = false;

    fourBy4.splitBoard(new Posn(0, 0), 4, 4);

    // after split
    t.checkExpect(fourBy4.board.get(0).get(0).down, true);
    t.checkExpect(fourBy4.board.get(1).get(0).down, true);
    t.checkExpect(fourBy4.board.get(2).get(0).down, true);
    t.checkExpect(fourBy4.board.get(3).get(0).down, true);
    t.checkExpect(fourBy4.board.get(0).get(1).down, true);
    t.checkExpect(fourBy4.board.get(0).get(1).up, true);
    t.checkExpect(fourBy4.board.get(0).get(1).right, true);
    t.checkExpect(fourBy4.board.get(1).get(1).up, true);
    t.checkExpect(fourBy4.board.get(1).get(1).left, true);
    t.checkExpect(fourBy4.board.get(2).get(1).up, true);
    t.checkExpect(fourBy4.board.get(2).get(1).right, true);
    t.checkExpect(fourBy4.board.get(3).get(1).up, true);
    t.checkExpect(fourBy4.board.get(3).get(1).down, true);
    t.checkExpect(fourBy4.board.get(3).get(1).left, true);
    t.checkExpect(fourBy4.board.get(0).get(2).down, true);
    t.checkExpect(fourBy4.board.get(0).get(2).up, true);
    t.checkExpect(fourBy4.board.get(1).get(2).down, true);
    t.checkExpect(fourBy4.board.get(2).get(2).down, true);
    t.checkExpect(fourBy4.board.get(3).get(2).down, true);
    t.checkExpect(fourBy4.board.get(3).get(2).up, true);
    t.checkExpect(fourBy4.board.get(0).get(3).right, true);
    t.checkExpect(fourBy4.board.get(1).get(3).up, true);
    t.checkExpect(fourBy4.board.get(1).get(3).left, true);
    t.checkExpect(fourBy4.board.get(1).get(3).right, true);
    t.checkExpect(fourBy4.board.get(2).get(3).up, true);
    t.checkExpect(fourBy4.board.get(2).get(3).left, true);
    t.checkExpect(fourBy4.board.get(2).get(3).right, true);
    t.checkExpect(fourBy4.board.get(3).get(3).up, true);
    t.checkExpect(fourBy4.board.get(3).get(3).left, true);
  }

  // tests createWires
  void testCreateWires(Tester t) {
    initData();
    t.checkExpect(light2.board.get(0).get(0).down, true);
    t.checkExpect(light2.board.get(0).get(1).up, true);
    t.checkExpect(light2.board.get(0).get(1).right, true);
    t.checkExpect(light2.board.get(0).get(1).down, false);
    t.checkExpect(light2.board.get(0).get(1).left, false);
    t.checkExpect(light2.board.get(1).get(0).down, true);
    t.checkExpect(light2.board.get(1).get(1).up, true);
    t.checkExpect(light2.board.get(1).get(1).left, true);
    t.checkExpect(light2.board.get(1).get(1).down, false);
    t.checkExpect(light2.board.get(1).get(1).right, false);
  }

  // test findRadius
  void testFindRadius(Tester t) {
    initData();
    t.checkExpect(light1.findRadius(), 16);
    t.checkExpect(light2.findRadius(), 2);
    t.checkExpect(light3.findRadius(), 4);
    t.checkExpect(light4.findRadius(), 1);
    t.checkExpect(light5.findRadius(), 3);
    t.checkExpect(light6.findRadius(), 8);
  }

  // test linkGamePieces
  void testLinkGamePieces(Tester t) {
    initData();
    t.checkExpect(light6.board.get(0).get(0).neighbors.size(), 2);
    t.checkExpect(light6.board.get(0).get(1).neighbors.size(), 3);
    t.checkExpect(light6.board.get(1).get(0).neighbors.size(), 3);
    t.checkExpect(light6.board.get(1).get(1).neighbors.size(), 4);
    t.checkExpect(light6.board.get(0).get(1).neighbors.size(), 3);
    t.checkExpect(light6.board.get(1).get(0).neighbors.size(), 3);
    t.checkExpect(light6.board.get(1).get(2).neighbors.size(), 4);
    t.checkExpect(light6.board.get(2).get(1).neighbors.size(), 4);
    t.checkExpect(light6.board.get(1).get(1).neighbors.size(), 4);

    // checking corner pieces in 2x2 have their neighbors
    t.checkExpect(light6.board.get(0).get(0).neighbors.get("down"), light6.board.get(0).get(1));
    t.checkExpect(light6.board.get(0).get(0).neighbors.get("right"), light6.board.get(1).get(0));

    // checking side pieces in 3x3 have their neighbors
    t.checkExpect(light3.board.get(1).get(0).neighbors.get("down"), light3.board.get(1).get(1));
    t.checkExpect(light3.board.get(1).get(0).neighbors.get("right"), light3.board.get(2).get(0));
    t.checkExpect(light3.board.get(1).get(0).neighbors.get("left"), light3.board.get(0).get(0));

    // checking middle piece in 3x3 has its neighbors
    t.checkExpect(light3.board.get(1).get(1).neighbors.get("down"), light3.board.get(1).get(2));
    t.checkExpect(light3.board.get(1).get(1).neighbors.get("up"), light3.board.get(1).get(0));
    t.checkExpect(light3.board.get(1).get(1).neighbors.get("right"), light3.board.get(2).get(1));
    t.checkExpect(light3.board.get(1).get(1).neighbors.get("left"), light3.board.get(0).get(1));
  }

  // tests makeScene
  void testMakeScene(Tester t) {
    initData();
    WorldScene s = new WorldScene(100, 100);
    light2.board = light2.makeBoard();
    light2.board.get(0).get(0).left = true;
    gamePiece1.left = true;
    light2.board.get(0).get(1).right = true;
    gamePiece2.right = true;
    gamePiece3.placePowerStation();

    s.placeImageXY(new BesideImage(
        new BesideImage(new EmptyImage(), new AboveImage(
            new AboveImage(new EmptyImage(),
                new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
                    new RectangleImage(27, 5, OutlineMode.SOLID, Color.GRAY), 0, 0,
                    new FrameImage(
                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.DARK_GRAY)))),
            new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE,
                new RectangleImage(27, 5, OutlineMode.SOLID, Color.GRAY), 0, 0,
                new FrameImage(new RectangleImage(50, 50, OutlineMode.SOLID, Color.DARK_GRAY))))),
        new AboveImage(new AboveImage(new EmptyImage(), gamePiece4.drawPiece(gamePiece3, 10)),
            gamePiece5.drawPiece(gamePiece3, 10))),
        50, 50);

    t.checkExpect(light2.makeScene(), s);
  }
}