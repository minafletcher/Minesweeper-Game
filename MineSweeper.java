import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;

import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

// representing a cell in the game MineSweeper
class Cell {
  public static final int CELL_SIZE = 20;
  public final WorldImage MINE = new CircleImage(5, OutlineMode.SOLID, Color.RED);
  public final WorldImage FLAG = new CircleImage(5, OutlineMode.SOLID, Color.GREEN);
  public final WorldImage CLICKED_CELL = new FrameImage(
      new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.DARK_GRAY));
  public final WorldImage UNCLICKED_CELL = new FrameImage(
      new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.GRAY));
  public final ArrayList<Color> COLORS = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.GREEN,
      Color.RED, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.BLACK, Color.GRAY));

  ArrayList<Cell> neighbors;
  boolean isMine;
  boolean isRightClicked;
  boolean isLeftClicked;
  WorldImage cell;

  // basic constructor for creating a new Cell
  Cell() {
    this.neighbors = new ArrayList<Cell>();
    this.isMine = false;
    this.isRightClicked = false;
    this.isLeftClicked = false;
  }

  // Cell convenience constructor for testing
  Cell(ArrayList<Cell> neighbors, boolean isMine, boolean isRightClicked, boolean isLeftClicked) {
    this.neighbors = neighbors;
    this.isMine = isMine;
    this.isRightClicked = isRightClicked;
    this.isLeftClicked = isLeftClicked;
  }

  // sets the mine status to true of this Cell
  void updateMine() {
    this.isMine = true;
  }

  // counts number of mines that neighbor a cell
  int countMines() {
    int count = 0;
    for (Cell c : this.neighbors) {
      if (c.isMine) {
        count++;
      }
    }
    return count;
  }

  // renders this Cell as an image
  public WorldImage drawCell() {
    if (this.isRightClicked) {
      return this.drawClickedCell(FLAG, UNCLICKED_CELL);
    }
    else if (this.isLeftClicked && !this.isMine && this.countMines() == 0) {
      return CLICKED_CELL;
    }
    else if (this.isLeftClicked && !this.isMine && this.countMines() != 0) {
      return this.drawClickedCell(
          new TextImage(Integer.toString(this.countMines()), COLORS.get(this.countMines() - 1)),
          CLICKED_CELL);
    }
    else if (this.isLeftClicked && this.isMine) {
      return this.drawClickedCell(MINE, CLICKED_CELL);
    }
    else {
      return UNCLICKED_CELL;
    }
  }

  // renders a clicked Cell as an image
  public WorldImage drawClickedCell(WorldImage im, WorldImage cell) {
    return new OverlayImage(im, cell);
  }

  // adds the neighbors of this Cell if they are in the bounds of the field
  void linkCellsHelp(int yPos, int xPos, ArrayList<ArrayList<Cell>> field) {
    for (int i = (yPos - 1); i <= (yPos + 1); i++) {
      for (int j = (xPos - 1); j <= (xPos + 1); j++) {
        if (coordOnBoard(i, j, field) && !(i == yPos && j == xPos)) {
          this.neighbors.add(field.get(i).get(j));
        }
      }
    }
  }

  // returns whether the given coordinates are within the given field's boundaries
  public boolean coordOnBoard(int row, int col, ArrayList<ArrayList<Cell>> field) {
    return (0 <= row && row < field.size()) && (0 <= col && col < field.get(0).size());
  }

  // is this Cell ending the game?
  public boolean worldEndCell() {
    return this.isMine && this.isLeftClicked;
  }

  // is this Cell already clicked?
  public boolean alreadyClicked() {
    return this.isRightClicked || this.isLeftClicked;
  }

  // make this Cell's right clicked field equal to given boolean
  // EFFECT: Makes this cell right clicked
  public void changeRightClicked(boolean changeTo) {
    this.isRightClicked = changeTo;
  }

  // make this Cell left clicked, and the Cells around it if it doesn't have
  // surrounding mines
  // EFFECT: Makes this Cell left clicked, and implements flood fill behavior
  public int makeLeftClicked() {
    this.isLeftClicked = true;
    if (this.countMines() == 0 && !this.isMine) {
      ArrayList<Cell> seen = new ArrayList<Cell>();
      ArrayList<Cell> added = new ArrayList<Cell>();
      seen.add(this);
      added.add(this);
      return this.floodFill(seen, added);
    }
    else {
      return 1;
    }
  }

  // implements floodfill behavior by making all Cells with no mines neighboring
  // left-clicked
  // EFFECT: Makes all neighboring cells with no neighboring mines left-clicked
  public int floodFill(ArrayList<Cell> seen, ArrayList<Cell> added) {
    for (Cell c : this.neighbors) {
      c.isLeftClicked = true;
      if (!added.contains(c)) {
        added.add(c);
      }
      if (c.countMines() == 0 && !seen.contains(c)) {
        seen.add(c);
        c.floodFill(seen, added);

      }
    }
    return added.size();
  }
}

// representing the world state for the game MineSweeper
class MineSweeper extends World {

  ArrayList<ArrayList<Cell>> field;
  int numCellsX;
  int numCellsY;
  int numInitMines;
  int numMines;
  int worldWidth;
  int worldHeight;
  int cellsClicked;
  int ticks;
  Random rand;

  // basic constructor for starting the MineSweeper game
  // certain restrictions put on the inputs for the game-
  // the player can't make the number of cells in the x direction larger than 90
  // or else the game would go off the screen of the computer
  // same with the number of cells in the y direction
  // and the player can't input an initial number of mines that is greater
  // than or equal to the total number of cells, else the player would not be able
  // to win or there would not be enough cells for the mines to go into
  MineSweeper(int numCellsX, int numCellsY, int numInitMines) {
    if (numCellsX > 90) {
      throw new IllegalArgumentException("Minefield is too wide for the screen");
    }
    if (numCellsY > 40) {
      throw new IllegalArgumentException("Minefield is too high for the screen");
    }
    if (numCellsX < 2 || numCellsY < 2) {
      throw new IllegalArgumentException("Field is too small");
    }
    if (numInitMines >= numCellsX * numCellsY) {
      throw new IllegalArgumentException("There are too many mines in the field");
    }
    this.numCellsX = numCellsX;
    this.numCellsY = numCellsY;
    this.numInitMines = numInitMines;
    this.worldWidth = numCellsX * Cell.CELL_SIZE;
    this.worldHeight = numCellsY * Cell.CELL_SIZE + Cell.CELL_SIZE * 2;
    this.numMines = numInitMines;
    this.cellsClicked = 0;
    this.ticks = 0;
    rand = new Random();
    this.field = this.makeField();
    this.addMines();
    this.linkCells();
  }

  // convenience constructor for testing
  // same restrictions as described in the above constructor
  MineSweeper(int numCellsX, int numCellsY, int numInitMines, int seed) {
    if (numCellsX > 90) {
      throw new IllegalArgumentException("Minefield is too wide for the screen");
    }
    if (numCellsY > 40) {
      throw new IllegalArgumentException("Minefield is too high for the screen");
    }
    if (numCellsX < 2 || numCellsY < 2) {
      throw new IllegalArgumentException("Field is too small");
    }
    if (numInitMines >= numCellsX * numCellsY) {
      throw new IllegalArgumentException("There are too many mines in the field");
    }
    this.numCellsX = numCellsX;
    this.numCellsY = numCellsY;
    this.numInitMines = numInitMines;
    this.worldWidth = numCellsX * Cell.CELL_SIZE;
    this.worldHeight = numCellsY * Cell.CELL_SIZE + Cell.CELL_SIZE * 2;
    this.numMines = numInitMines;
    this.cellsClicked = 0;
    rand = new Random(seed);
    this.field = new ArrayList<ArrayList<Cell>>();
  }

  // making the field with the specified number of mines
  ArrayList<ArrayList<Cell>> makeField() {
    ArrayList<ArrayList<Cell>> row = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < this.numCellsY; i++) {
      ArrayList<Cell> column = new ArrayList<Cell>();
      for (int j = 0; j < this.numCellsX; j++) {
        column.add(new Cell());
      }
      row.add(column);
    }
    return row;
  }

  // adds mines at random positions on the field
  void addMines() {
    ArrayList<Posn> positions = new ArrayList<Posn>();
    for (int i = 0; i < numCellsY; i++) {
      for (int j = 0; j < this.numCellsX; j++) {
        positions.add(new Posn(i, j));
      }
    }

    int temp = positions.size();
    for (int i = 0; i < numMines; i++) {
      int num = rand.nextInt(temp);
      Posn thisPos = positions.get(num);
      field.get(thisPos.x).get(thisPos.y).updateMine();
      positions.remove(num);
      temp--;
    }
  }

  // links each Cell in the field to its neighbor Cells
  void linkCells() {
    for (int i = 0; i < numCellsY; i++) {
      for (int j = 0; j < numCellsX; j++) {
        this.field.get(i).get(j).linkCellsHelp(i, j, this.field);
      }
    }
  }

  // keeps track of how much time has passed since the game started
  public void onTick() {
    this.ticks++;
  }

  // implements the correct behavior based on what cell is pressed with which
  // mouse button
  // EFFECT: Changes the state of the board by making the cell clicked change
  public void onMouseClicked(Posn pos, String buttonName) {
    if (buttonName.equals("LeftButton")) {
      this.handleLeftClick(pos);
    }
    else if (buttonName.equals("RightButton")) {
      this.handleRightClick(pos);
    }
    else {
      return;
    }
  }

  // handles the case that a Cell is right-clicked
  // EFFECT: Makes the appropriate Cell right-clicked
  public void handleRightClick(Posn pos) {
    Posn temp = this.getCell(pos);
    int wantI = temp.x;
    int wantJ = temp.y;
    for (int i = 0; i < this.numCellsY; i++) {
      for (int j = 0; j < this.numCellsX; j++) {
        if (wantI == i && wantJ == j) {
          if (field.get(i).get(j).alreadyClicked()) {
            if (field.get(i).get(j).isRightClicked) {
              this.numMines++;
            }
            field.get(i).get(j).changeRightClicked(false);
          }
          else {
            field.get(i).get(j).changeRightClicked(true);
            this.numMines--;
          }
        }
      }
    }
  }

  // handle the case that a Cell is left-clicked
  // EFFECT: Makes the clicked Cell and surrounding Cells possibly left-clicked
  public void handleLeftClick(Posn pos) {
    Posn temp = this.getCell(pos);
    int wantI = temp.x;
    int wantJ = temp.y;
    for (int i = 0; i < this.numCellsY; i++) {
      for (int j = 0; j < this.numCellsX; j++) {
        if (wantI == i && wantJ == j) {
          if (field.get(i).get(j).alreadyClicked()) {
            return;
          }
          else {
            this.cellsClicked += this.field.get(i).get(j).makeLeftClicked();
          }
        }
      }
    }
  }

  // turns the given position into the appropriate Cell in the field
  public Posn getCell(Posn p) {
    return new Posn((p.y - Cell.CELL_SIZE * 2) / Cell.CELL_SIZE,
        (this.worldWidth - p.x) / Cell.CELL_SIZE);
  }

  // are anything in the field left-clicked and mines?
  public boolean anyMinesClicked() {
    boolean result = false;
    for (int i = 0; i < this.numCellsY; i++) {
      for (int j = 0; j < this.numCellsX; j++) {
        if (!result) {
          result = field.get(i).get(j).worldEndCell();
        }
      }
    }
    return result;
  }

  // determines if the game has been won
  public boolean wonGame() {
    return cellsClicked == this.numCellsX * this.numCellsY - this.numInitMines;
  }

  // determines whether the world is ending or not
  public WorldEnd worldEnds() {
    if (this.anyMinesClicked()) {
      return new WorldEnd(true, this.makeFinalScene("You clicked a mine, you lost!"));
    }
    else if (this.wonGame()) {
      return new WorldEnd(true, this.makeFinalScene("You cleared the field, you win!"));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // renders an image of the game-end scene, with the appropriate message
  public WorldScene makeFinalScene(String msg) {
    WorldScene scene = new WorldScene(this.worldWidth, this.worldHeight);
    scene.placeImageXY(new TextImage(msg, this.worldWidth / 15, Color.BLACK), this.worldWidth / 2,
        this.worldHeight / 2);
    return scene;
  }

  // renders an image of the game
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.worldWidth, this.worldHeight);
    WorldImage row = new EmptyImage();
    for (int i = 0; i < this.numCellsY; i++) {
      WorldImage column = new EmptyImage();
      for (int j = 0; j < this.numCellsX; j++) {
        column = new BesideImage(this.field.get(i).get(j).drawCell(), column);
      }
      row = new AboveImage(row, column);
    }
    row = new AboveImage(this.drawHeader(), row);
    scene.placeImageXY(row, this.worldWidth / 2, this.worldHeight / 2);
    return scene;
  }

  // draws the header of the game as a WorldImage
  public WorldImage drawHeader() {
    return new BesideImage(this.drawGameNum(this.numMines),
        new BesideImage(this.drawTitle(), this.drawGameNum(this.ticks)));
  }

  // renders the given number as an image
  public WorldImage drawGameNum(int num) {
    return new OverlayImage(new TextImage(Integer.toString(num), Cell.CELL_SIZE / 2, Color.RED),
        new RectangleImage(this.worldWidth / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID,
            Color.BLACK));
  }

  // renders the minesweeper title as an image
  // if statement is so that when minefield gets below a threshold size
  // we can still format the game (due to the way font width/height is made)
  public WorldImage drawTitle() {
    if (numCellsX < 10) {
      return new OverlayImage(new TextImage("MINESWEEPER", numCellsX, FontStyle.BOLD, Color.BLACK),
          new RectangleImage(this.worldWidth / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID,
              Color.GRAY));
    }
    else {
      return new OverlayImage(
          new TextImage("MINESWEEPER", Cell.CELL_SIZE / 1.5, FontStyle.BOLD, Color.BLACK),
          new RectangleImage(this.worldWidth / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID,
              Color.GRAY));
    }
  }
}

class ExamplesMineSweeper {

  /*
   * void testBigBang(Tester t) {
   * MineSweeper sweep = new MineSweeper(30, 16, 99);
   * int worldWidth = Cell.CELL_SIZE * 30;
   * int worldHeight = Cell.CELL_SIZE * 16 + 2 * Cell.CELL_SIZE;
   * double tickRate = 1;
   * sweep.bigBang(worldWidth, worldHeight, tickRate);
   * }
   */

  MineSweeper game1;
  MineSweeper game2;
  MineSweeper game3;
  MineSweeper game4;
  MineSweeper game5;
  MineSweeper game6;

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;
  Cell cell10;
  Cell cell11;
  Cell cell12;
  Cell cell13;
  Cell cell15;

  // when called, sets the examples to their original implementations
  // in order to get rid of any mutation done on them
  void reset() {
    game1 = new MineSweeper(3, 3, 2, 50);
    game2 = new MineSweeper(5, 5, 3);
    game3 = new MineSweeper(2, 2, 2, 50);
    game4 = new MineSweeper(2, 2, 2, 1);
    game5 = new MineSweeper(2, 2, 2);
    game6 = new MineSweeper(15, 15, 99);
    cell1 = new Cell();
    cell2 = new Cell(new ArrayList<Cell>(), true, true, false);
    cell3 = new Cell(new ArrayList<Cell>(), false, true, false);
    cell4 = new Cell(new ArrayList<Cell>(), false, false, false);
    cell5 = new Cell(new ArrayList<Cell>(), false, false, true);
    cell6 = new Cell(new ArrayList<Cell>(Arrays.asList(cell1, cell2, cell3)), false, false, true);
    cell7 = new Cell(new ArrayList<Cell>(), true, false, true);
    cell15 = new Cell(new ArrayList<Cell>(Arrays.asList(cell2, cell7)), false, false, true);
    cell8 = new Cell(new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7)), false, false, true);
    cell9 = new Cell(new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7, cell2)), false, false,
        true);
    cell10 = new Cell(new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7, cell2, cell7)), false,
        false, true);
    cell11 = new Cell(new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7, cell2, cell2, cell7)),
        false, false, true);
    cell12 = new Cell(
        new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7, cell2, cell2, cell2, cell7)), false,
        false, true);
    cell13 = new Cell(
        new ArrayList<Cell>(Arrays.asList(cell2, cell7, cell7, cell2, cell2, cell2, cell2, cell2)),
        false, false, true);
  }

  // CELL ----------------------------------------------------------------

  // tests updateMine
  void testUpdateMine(Tester t) {
    reset();
    t.checkExpect(cell1.isMine, false);
    cell1.updateMine();
    t.checkExpect(cell1.isMine, true);
    cell1.updateMine();
    t.checkExpect(cell1.isMine, true);
  }

  // test countMines
  void testCountMines(Tester t) {
    reset();
    game4.field = game4.makeField();
    t.checkExpect(game4.field.get(0).get(0).countMines(), 0);
    game4.addMines();
    t.checkExpect(game4.field.get(0).get(0).countMines(), 0);
    game4.linkCells();
    t.checkExpect(game4.field.get(0).get(0).countMines(), 2);
    t.checkExpect(game4.field.get(0).get(1).countMines(), 1);
    t.checkExpect(game4.field.get(1).get(0).countMines(), 1);
    t.checkExpect(game4.field.get(1).get(1).countMines(), 2);

    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).countMines(), 1);
    t.checkExpect(game1.field.get(1).get(1).countMines(), 1);
    t.checkExpect(game1.field.get(2).get(2).countMines(), 2);
  }

  // tests drawCell
  void testDrawCell(Tester t) {
    reset();
    t.checkExpect(cell1.drawCell(), new FrameImage(
        new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY)));
    t.checkExpect(cell3.drawCell(),
        new OverlayImage(new CircleImage(5, OutlineMode.SOLID, Color.GREEN), new FrameImage(
            new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))));
    t.checkExpect(cell5.drawCell(), new FrameImage(
        new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.DARK_GRAY)));
    t.checkExpect(cell6.countMines(), 1);
    t.checkExpect(cell6.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(1), Color.BLUE),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell7.drawCell(),
        new OverlayImage(new CircleImage(5, OutlineMode.SOLID, Color.RED),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell15.countMines(), 2);
    t.checkExpect(cell15.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(2), Color.GREEN),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell8.countMines(), 3);
    t.checkExpect(cell8.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(3), Color.RED),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell9.countMines(), 4);
    t.checkExpect(cell9.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(4), Color.MAGENTA),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell10.countMines(), 5);
    t.checkExpect(cell10.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(5), Color.ORANGE),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell11.countMines(), 6);
    t.checkExpect(cell11.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(6), Color.CYAN),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell12.countMines(), 7);
    t.checkExpect(cell12.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(7), Color.BLACK),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(cell13.countMines(), 8);
    t.checkExpect(cell13.drawCell(),
        new OverlayImage(new TextImage(Integer.toString(8), Color.GRAY),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));

  }

  // test drawClickedCell
  void testDrawClickedCell(Tester t) {
    reset();
    t.checkExpect(
        cell2.drawClickedCell(new CircleImage(5, OutlineMode.SOLID, Color.RED),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))),
        new OverlayImage(new CircleImage(5, OutlineMode.SOLID, Color.RED),
            new FrameImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                Color.DARK_GRAY))));
    t.checkExpect(
        cell2.drawClickedCell(new CircleImage(5, OutlineMode.SOLID, Color.GREEN),
            new FrameImage(
                new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))),
        new OverlayImage(new CircleImage(5, OutlineMode.SOLID, Color.GREEN), new FrameImage(
            new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))));
    t.checkExpect(
        cell2.drawClickedCell(new TextImage(Integer.toString(1), Color.BLUE),
            new FrameImage(
                new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))),
        new OverlayImage(new TextImage(Integer.toString(1), Color.BLUE), new FrameImage(
            new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))));
    t.checkExpect(
        cell2.drawClickedCell(new TextImage(Integer.toString(3), Color.RED),
            new FrameImage(
                new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))),
        new OverlayImage(new TextImage(Integer.toString(3), Color.RED), new FrameImage(
            new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID, Color.GRAY))));
  }

  // tests linkCellsHelp
  void testLinkCellsHelp(Tester t) {
    reset();
    reset();
    game1.field = game1.makeField();
    t.checkExpect(cell1.neighbors.size(), 0);
    t.checkExpect(cell2.neighbors.size(), 0);
    t.checkExpect(cell3.neighbors.size(), 0);
    cell1.linkCellsHelp(0, 0, game1.field);
    cell2.linkCellsHelp(1, 1, game1.field);
    cell3.linkCellsHelp(0, 1, game1.field);
    t.checkExpect(cell1.neighbors.size(), 3);
    t.checkExpect(cell2.neighbors.size(), 8);
    t.checkExpect(cell3.neighbors.size(), 5);

    // checking the first corner cell has its neighbors in its list
    t.checkExpect(cell1.neighbors.contains(game1.field.get(0).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(0)), true);
    // checking the first edge cell has all its neighbors
    t.checkExpect(cell3.neighbors.contains(game1.field.get(0).get(0)), true);
    t.checkExpect(cell3.neighbors.contains(game1.field.get(0).get(2)), true);
    t.checkExpect(cell3.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell3.neighbors.contains(game1.field.get(1).get(2)), true);
    t.checkExpect(cell3.neighbors.contains(game1.field.get(1).get(0)), true);
    // checking the middle cell has all 8 neighbors
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(0)), true);

    reset();

    game1.field = game1.makeField();
    cell1.linkCellsHelp(2, 0, game1.field);
    cell2.linkCellsHelp(1, 0, game1.field);

    // checking the second corner cell has its neighbors in its list
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(0)), true);
    // checking the second edge cell has all its neighbors
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(1)), true);

    reset();

    game1.field = game1.makeField();
    cell1.linkCellsHelp(0, 2, game1.field);
    cell2.linkCellsHelp(2, 1, game1.field);

    // checking the third corner cell has its neighbors in its list
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(0).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(2)), true);
    // checking the third edge cell has all its neighbors
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(0)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(0)), true);

    reset();

    game1.field = game1.makeField();
    cell1.linkCellsHelp(2, 2, game1.field);
    cell2.linkCellsHelp(1, 2, game1.field);

    // checking the fourth corner cell has its neighbors in its list
    t.checkExpect(cell1.neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell1.neighbors.contains(game1.field.get(1).get(2)), true);
    // checking the fourth edge cell has all its neighbors
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(2)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(cell2.neighbors.contains(game1.field.get(0).get(1)), true);
  }

  // tests coordOnBoard
  void testCoordOnBoard(Tester t) {
    reset();
    game1.field = game1.makeField();
    t.checkExpect(game1.field.size(), 3);
    t.checkExpect(game1.field.get(0).size(), 3);
    t.checkExpect(cell1.coordOnBoard(0, 1, game1.field), true);
    t.checkExpect(cell1.coordOnBoard(1, 0, game1.field), true);
    t.checkExpect(cell1.coordOnBoard(-1, 0, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(0, -1, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(4, 2, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(2, 4, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(3, 2, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(2, 3, game1.field), false);
    t.checkExpect(cell1.coordOnBoard(2, 2, game1.field), true);
    t.checkExpect(cell1.coordOnBoard(2, 2, game1.field), true);
  }

  // test worldEndCell
  void testWorldEndCell(Tester t) {
    reset();
    t.checkExpect(cell1.worldEndCell(), false);
    t.checkExpect(cell2.worldEndCell(), false);
    t.checkExpect(cell3.worldEndCell(), false);
    t.checkExpect(cell4.worldEndCell(), false);
    t.checkExpect(cell5.worldEndCell(), false);
  }

  // test alreadyClicked
  void testAlreadyClicked(Tester t) {
    reset();
    t.checkExpect(cell1.alreadyClicked(), false);
    cell1.changeRightClicked(true);
    t.checkExpect(cell1.alreadyClicked(), true);
    t.checkExpect(cell3.alreadyClicked(), true);
    t.checkExpect(cell4.alreadyClicked(), false);
    cell4.makeLeftClicked();
    t.checkExpect(cell4.alreadyClicked(), true);
    t.checkExpect(cell5.alreadyClicked(), true);
    t.checkExpect(cell7.alreadyClicked(), true);
    t.checkExpect(cell12.alreadyClicked(), true);
  }

  // test changeRightClicked
  void testchangeRightClicked(Tester t) {
    reset();
    t.checkExpect(cell4.isRightClicked, false);
    cell4.changeRightClicked(true);
    t.checkExpect(cell4.isRightClicked, true);
    t.checkExpect(cell2.isRightClicked, true);
    cell2.changeRightClicked(true);
    t.checkExpect(cell2.isRightClicked, true);
    t.checkExpect(cell1.isRightClicked, false);
    cell1.changeRightClicked(true);
    t.checkExpect(cell1.isRightClicked, true);
    cell1.changeRightClicked(false);
    t.checkExpect(cell1.isRightClicked, false);
  }

  // test makeLeftClicked
  void tesMakeLeftClicked(Tester t) {
    reset();
    t.checkExpect(cell4.isLeftClicked, false);
    cell4.makeLeftClicked();
    t.checkExpect(cell4.isLeftClicked, true);
    reset();
    cell4.neighbors.add(cell6);
    cell4.neighbors.add(cell8);
    cell6.neighbors.add(cell7);
    t.checkExpect(cell4.makeLeftClicked(), 3);
    t.checkExpect(cell4.isLeftClicked, true);
    t.checkExpect(cell8.isLeftClicked, false);
    t.checkExpect(cell6.isLeftClicked, true);
    t.checkExpect(cell7.isLeftClicked, true);
  }

  // test floodFill
  void testFloodFill(Tester t) {
    reset();
    t.checkExpect(cell4.isLeftClicked, false);
    cell4.makeLeftClicked();
    cell4.neighbors.add(cell6);
    cell6.neighbors.add(cell7);
    cell6.neighbors.add(cell8);
    t.checkExpect(cell4.floodFill(new ArrayList<Cell>(Arrays.asList(cell4)),
        new ArrayList<Cell>(Arrays.asList(cell4))), 2);
    t.checkExpect(cell4.isLeftClicked, true);
    t.checkExpect(cell8.isLeftClicked, true);
    t.checkExpect(cell6.isLeftClicked, true);
    t.checkExpect(cell7.isLeftClicked, true);
    t.checkExpect(cell1.isLeftClicked, false);
    cell1.neighbors.add(cell6);
    t.checkExpect(cell1.floodFill(new ArrayList<Cell>(Arrays.asList(cell1, cell6)),
        new ArrayList<Cell>(Arrays.asList(cell1, cell6))), 2);
    t.checkExpect(cell1.isLeftClicked, false);
    t.checkExpect(cell6.isLeftClicked, true);
    t.checkExpect(cell7.isLeftClicked, true);
    t.checkExpect(cell8.isLeftClicked, true);
  }

  // MINESWEEP ------------------------------------------------------

  // test constructor restrictions
  void testConstructor(Tester t) {
    // for constructor without random seed
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too wide for the screen"), "MineSweeper", 91, 5,
        0);
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too high for the screen"), "MineSweeper", 5, 41,
        0);
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too high for the screen"), "MineSweeper", 1, 41,
        0);
    t.checkConstructorException(new IllegalArgumentException("Field is too small"), "MineSweeper",
        23, 1, 0);
    t.checkConstructorException(new IllegalArgumentException("Field is too small"), "MineSweeper",
        5, 1, 0);
    t.checkConstructorException(
        new IllegalArgumentException("There are too many mines in the field"), "MineSweeper", 5, 5,
        25);
    t.checkConstructorException(
        new IllegalArgumentException("There are too many mines in the field"), "MineSweeper", 5, 5,
        30);

    // for constructor with random seed
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too wide for the screen"), "MineSweeper", 91, 5,
        0, 10);
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too high for the screen"), "MineSweeper", 5, 41,
        0, 10);
    t.checkConstructorException(
        new IllegalArgumentException("Minefield is too high for the screen"), "MineSweeper", 1, 41,
        0, 10);
    t.checkConstructorException(new IllegalArgumentException("Field is too small"), "MineSweeper",
        23, 1, 0, 10);
    t.checkConstructorException(new IllegalArgumentException("Field is too small"), "MineSweeper",
        5, 1, 0, 10);
    t.checkConstructorException(
        new IllegalArgumentException("There are too many mines in the field"), "MineSweeper", 5, 5,
        25, 10);
    t.checkConstructorException(
        new IllegalArgumentException("There are too many mines in the field"), "MineSweeper", 5, 5,
        30, 10);
  }

  // tests makeField
  void testMakeField(Tester t) {
    reset();
    t.checkExpect(game1.field.size(), 0);
    game1.field = game1.makeField();
    t.checkExpect(game1.field.size(), 3);
    t.checkExpect(game1.field.get(0).size(), 3);
    t.checkExpect(game1.field.get(1).size(), 3);
    t.checkExpect(game1.field.get(2).size(), 3);
    t.checkExpect(game1.field.get(0).get(0), new Cell());
    t.checkExpect(game1.field.get(1).get(1), new Cell());
    t.checkExpect(game1.field.get(2).get(2), new Cell());
    t.checkExpect(game1.field.get(1).get(2), new Cell());
    t.checkExpect(game2.field.size(), 5);
    t.checkExpect(game2.field.get(1).size(), 5);
  }

  // tests addMines
  void testAddMines(Tester t) {
    reset();
    game3.field = game3.makeField();
    t.checkExpect(game3.field.get(0).get(0).isMine, false);
    t.checkExpect(game3.field.get(1).get(0).isMine, false);
    t.checkExpect(game3.field.get(0).get(1).isMine, false);
    t.checkExpect(game3.field.get(1).get(1).isMine, false);
    game3.addMines();
    t.checkExpect(game3.field.get(0).get(0).isMine, true);
    t.checkExpect(game3.field.get(1).get(0).isMine, true);
    t.checkExpect(game3.field.get(0).get(1).isMine, false);
    t.checkExpect(game3.field.get(1).get(1).isMine, false);

    game4.field = game4.makeField();
    t.checkExpect(game4.field.get(0).get(0).isMine, false);
    t.checkExpect(game4.field.get(1).get(0).isMine, false);
    t.checkExpect(game4.field.get(0).get(1).isMine, false);
    t.checkExpect(game4.field.get(1).get(1).isMine, false);
    game4.addMines();
    t.checkExpect(game4.field.get(0).get(0).isMine, false);
    t.checkExpect(game4.field.get(1).get(0).isMine, true);
    t.checkExpect(game4.field.get(0).get(1).isMine, true);
    t.checkExpect(game4.field.get(1).get(1).isMine, false);
  }

  // testsLinkCells for MineSweeper
  void testLinkCells(Tester t) {
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    t.checkExpect(game1.field.get(0).get(0).neighbors.size(), 0);
    t.checkExpect(game1.field.get(0).get(1).neighbors.size(), 0);
    t.checkExpect(game1.field.get(1).get(1).neighbors.size(), 0);
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).neighbors.size(), 3);
    t.checkExpect(game1.field.get(0).get(1).neighbors.size(), 5);
    t.checkExpect(game1.field.get(1).get(1).neighbors.size(), 8);
    // checking the corner cells has its neighbors in its list
    t.checkExpect(game1.field.get(0).get(0).neighbors.contains(game1.field.get(0).get(1)), true);
    t.checkExpect(game1.field.get(0).get(0).neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(game1.field.get(0).get(0).neighbors.contains(game1.field.get(1).get(0)), true);
    // checking this cell has all its neighbors
    t.checkExpect(game1.field.get(0).get(1).neighbors.contains(game1.field.get(0).get(0)), true);
    t.checkExpect(game1.field.get(0).get(1).neighbors.contains(game1.field.get(0).get(2)), true);
    t.checkExpect(game1.field.get(0).get(1).neighbors.contains(game1.field.get(1).get(1)), true);
    t.checkExpect(game1.field.get(0).get(1).neighbors.contains(game1.field.get(1).get(2)), true);
    t.checkExpect(game1.field.get(0).get(1).neighbors.contains(game1.field.get(1).get(0)), true);
    // checking middle cell has all 8 neighbors
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(0).get(0)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(2).get(2)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(1).get(2)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(2).get(1)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(1).get(0)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(0).get(1)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(0).get(2)), true);
    t.checkExpect(game1.field.get(1).get(1).neighbors.contains(game1.field.get(2).get(0)), true);
  }

  // test onTick
  void testOnTick(Tester t) {
    reset();
    t.checkExpect(game2.ticks, 0);
    game2.onTick();
    t.checkExpect(game2.ticks, 1);
    game2.onTick();
    t.checkExpect(game2.ticks, 2);
    game1.field = game1.makeField();
    t.checkExpect(game1.ticks, 0);
    game1.onTick();
    t.checkExpect(game1.ticks, 1);

  }

  // test onMouseClicked
  void testOnMouseClicked(Tester t) {
    // tests when the mouse right clicked
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, false);
    game1.onMouseClicked(new Posn(45, 45), "RightButton");
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, true);
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, false);
    game1.onMouseClicked(new Posn(40, 75), "RightButton");
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, true);
    game1.onMouseClicked(new Posn(40, 75), "RightButton");
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, false);
    // tests the else

    game1.onMouseClicked(new Posn(45, 45), "RightButton");
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, false);
    game1.onMouseClicked(new Posn(45, 45), "Hehe!");
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, false);

    game1.onMouseClicked(new Posn(45, 45), "LeftButton");
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, true);
    game1.onMouseClicked(new Posn(45, 45), "Hehe!");
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, true);

    game1.onMouseClicked(new Posn(45, 45), "LeftButton");
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, false);
    game1.onMouseClicked(new Posn(45, 45), "Hehe!");
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, false);

    // tests when the mouse left clicked
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, false);
    game1.onMouseClicked(new Posn(45, 45), "LeftButton");
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, true);
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, false);
    game1.onMouseClicked(new Posn(40, 75), "LeftButton");
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, true);
  }

  // test handleRightClick
  void testHandleRight(Tester t) {
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, false);
    game1.handleRightClick(new Posn(45, 45));
    t.checkExpect(game1.field.get(0).get(0).isRightClicked, true);
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, false);
    game1.handleRightClick(new Posn(40, 75));
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, true);
    game1.handleRightClick(new Posn(40, 75));
    t.checkExpect(game1.field.get(1).get(1).isRightClicked, false);
  }

  // test handleLeftClick
  void testHandleLeft(Tester t) {
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, false);
    game1.handleLeftClick(new Posn(45, 45));
    t.checkExpect(game1.field.get(0).get(0).isLeftClicked, true);
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, false);
    game1.handleLeftClick(new Posn(40, 75));
    t.checkExpect(game1.field.get(1).get(1).isLeftClicked, true);

  }

  // test getCell
  void testGetCell(Tester t) {
    reset();
    t.checkExpect(this.game1.getCell(new Posn(43, 1)), new Posn(-1, 0));
    t.checkExpect(this.game5.getCell(new Posn(1, 11)), new Posn(-1, 1));
    t.checkExpect(this.game5.getCell(new Posn(31, 39)), new Posn(0, 0));
  }

  // test anyMinesClicked
  void testAnyMinesClicked(Tester t) {
    reset();
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    t.checkExpect(game1.anyMinesClicked(), false);
    game1.field.get(1).get(1).makeLeftClicked();
    t.checkExpect(game1.anyMinesClicked(), true);
    game4.field = game4.makeField();
    game4.addMines();
    game4.linkCells();
    game4.field.get(0).get(0).makeLeftClicked();
    t.checkExpect(game4.anyMinesClicked(), false);
  }

  // test wonGame
  void testWonGame(Tester t) {
    reset();
    game2.cellsClicked = 22;
    t.checkExpect(game2.wonGame(), true);
    game2.cellsClicked = 21;
    t.checkExpect(game2.wonGame(), false);
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    game1.cellsClicked++;
    game1.cellsClicked++;
    game1.cellsClicked++;
    game1.cellsClicked++;
    game1.cellsClicked++;
    game1.cellsClicked++;
    game1.cellsClicked++;
    t.checkExpect(game1.wonGame(), true);
    game1.cellsClicked--;
    t.checkExpect(game1.wonGame(), false);

  }

  // test worldEnds
  void testWorldEnd(Tester t) {
    reset();
    game3.field = game3.makeField();
    game3.addMines();
    game3.linkCells();
    t.checkExpect(game3.worldEnds(), new WorldEnd(false, game3.makeScene()));
    game3.field.get(0).get(0).changeRightClicked(true);
    t.checkExpect(game3.worldEnds(), new WorldEnd(false, game3.makeScene()));
    game3.field.get(0).get(0).makeLeftClicked();
    t.checkExpect(game3.worldEnds(),
        new WorldEnd(true, game3.makeFinalScene("You clicked a mine, you lost!")));
    game1.field = game1.makeField();
    game1.addMines();
    game1.linkCells();
    game1.cellsClicked = 6;
    t.checkExpect(game1.worldEnds(), new WorldEnd(false, game1.makeScene()));
    game1.cellsClicked = 7;
    t.checkExpect(game1.worldEnds(),
        new WorldEnd(true, game1.makeFinalScene("You cleared the field, you win!")));
  }

  // test makeFinalScene
  void testMakeFinalScene(Tester t) {
    reset();
    WorldScene scene1 = new WorldScene(100, 140);
    String msg = "Hello";
    scene1.placeImageXY(new TextImage("Hello", 6, Color.BLACK), 50, 70);
    t.checkExpect(game2.makeFinalScene(msg), scene1);
    WorldScene scene2 = new WorldScene(60, 100);
    String msg2 = "You Lost!";
    scene2.placeImageXY(new TextImage("You Lost!", 4, Color.BLACK), 30, 50);
    t.checkExpect(game1.makeFinalScene(msg2), scene2);
  }

  // tests makeScene
  void testMakeScene(Tester t) {
    reset();
    WorldImage unclicked = new FrameImage(
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY));
    WorldScene scene = new WorldScene(40, 80);
    WorldScene scene2 = new WorldScene(60, 100);
    scene.placeImageXY(
        new AboveImage(game5.drawHeader(), new AboveImage(new EmptyImage(),
            new AboveImage(new BesideImage(unclicked, new BesideImage(unclicked, new EmptyImage())),
                new BesideImage(unclicked, new BesideImage(unclicked, new EmptyImage()))))),
        20, 40);
    t.checkExpect(game5.makeScene(), scene);
    game1.field = game1.makeField();
    scene2.placeImageXY(new AboveImage(game1.drawHeader(),
        new AboveImage(new EmptyImage(), new AboveImage(
            new BesideImage(unclicked,
                new BesideImage(unclicked, new BesideImage(unclicked, new EmptyImage()))),
            new AboveImage(
                new BesideImage(unclicked,
                    new BesideImage(unclicked, new BesideImage(unclicked, new EmptyImage()))),
                new BesideImage(unclicked,
                    new BesideImage(unclicked, new BesideImage(unclicked, new EmptyImage()))))))),
        30, 50);
    t.checkExpect(game1.makeScene(), scene2);
  }

  // tests drawHeader
  void testDrawHeader(Tester t) {
    reset();
    t.checkExpect(game1.drawHeader(), new BesideImage(
        new OverlayImage(new TextImage(Integer.toString(2), Cell.CELL_SIZE / 2, Color.RED),
            new RectangleImage(60 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)),
        new BesideImage(
            new OverlayImage(new TextImage("MINESWEEPER", 3, FontStyle.BOLD, Color.BLACK),
                new RectangleImage(60 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)),
            new OverlayImage(new TextImage(Integer.toString(0), Cell.CELL_SIZE / 2, Color.RED),
                new RectangleImage(60 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)))));
    t.checkExpect(game2.drawHeader(), new BesideImage(
        new OverlayImage(new TextImage(Integer.toString(3), Cell.CELL_SIZE / 2, Color.RED),
            new RectangleImage(100 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)),
        new BesideImage(
            new OverlayImage(new TextImage("MINESWEEPER", 5, FontStyle.BOLD, Color.BLACK),
                new RectangleImage(100 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)),
            new OverlayImage(new TextImage(Integer.toString(0), Cell.CELL_SIZE / 2, Color.RED),
                new RectangleImage(100 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)))));
    t.checkExpect(game6.drawHeader(), new BesideImage(
        new OverlayImage(new TextImage(Integer.toString(99), Cell.CELL_SIZE / 2, Color.RED),
            new RectangleImage(300 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)),
        new BesideImage(
            new OverlayImage(
                new TextImage("MINESWEEPER", Cell.CELL_SIZE / 1.5, FontStyle.BOLD, Color.BLACK),
                new RectangleImage(300 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)),
            new OverlayImage(new TextImage(Integer.toString(0), Cell.CELL_SIZE / 2, Color.RED),
                new RectangleImage(300 / 4, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.BLACK)))));
  }

  // tests drawGameNum
  void testDrawGameNum(Tester t) {
    reset();
    t.checkExpect(game2.drawGameNum(3), new OverlayImage(new TextImage("3", 10, Color.RED),
        new RectangleImage(25, 40, OutlineMode.SOLID, Color.BLACK)));
    t.checkExpect(game2.drawGameNum(10), new OverlayImage(new TextImage("10", 10, Color.RED),
        new RectangleImage(25, 40, OutlineMode.SOLID, Color.BLACK)));
    t.checkExpect(game2.drawGameNum(3), new OverlayImage(new TextImage("3", 10, Color.RED),
        new RectangleImage(25, 40, OutlineMode.SOLID, Color.BLACK)));
    t.checkExpect(game1.drawGameNum(10), new OverlayImage(new TextImage("10", 10, Color.RED),
        new RectangleImage(15, 40, OutlineMode.SOLID, Color.BLACK)));
  }

  // tests drawTitle
  void testDrawTitle(Tester t) {
    reset();
    t.checkExpect(game1.drawTitle(),
        new OverlayImage(new TextImage("MINESWEEPER", 3, FontStyle.BOLD, Color.BLACK),
            new RectangleImage(60 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)));
    t.checkExpect(game2.drawTitle(),
        new OverlayImage(new TextImage("MINESWEEPER", 5, FontStyle.BOLD, Color.BLACK),
            new RectangleImage(100 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)));
    t.checkExpect(game6.drawTitle(),
        new OverlayImage(
            new TextImage("MINESWEEPER", Cell.CELL_SIZE / 1.5, FontStyle.BOLD, Color.BLACK),
            new RectangleImage(300 / 2, Cell.CELL_SIZE * 2, OutlineMode.SOLID, Color.GRAY)));
  }
}