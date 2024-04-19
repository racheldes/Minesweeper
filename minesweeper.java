import java.util.ArrayList;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;


// represents the minesweeper world 
class Minesweeper extends World {
  int rows;
  int cols;
  int numMines;
  int size; // of 1 cell
  Random rand;
  ArrayList<ArrayList<Cell>> board; //arraylist of rows 
  boolean gameOver;
  boolean win;

  // constructor for testing
  Minesweeper(int rows, int cols, int numMines, int size, Random r) {  
    if (numMines > rows * cols) {
      throw new IllegalArgumentException("More mines than squares. Check again.");
    }

    this.rows = rows;
    this.cols = cols;
    this.numMines = numMines;
    this.size = size;
    this.rand = r;
    this.board = this.makeBoard();
    this.gameOver = false;
    this.win = true;
  }

  // constructor for playing the real game
  Minesweeper(int rows, int cols, int numMines, int size) {
    ArrayList<Coord> positions = new ArrayList<Coord>();
    if (numMines > rows * cols) {
      throw new IllegalArgumentException("More mines than squares. Check again.");
    }
    this.rows = rows;
    this.cols = cols;
    this.numMines = numMines;
    this.board = this.makeBoard();
    this.size = size;
    this.gameOver = false;
    this.win = true;
    positions = new Utils().randomGen(numMines, rows,cols, new Random());
    setMine(positions);
  }

  /* MINESWEEPER TEMPLATE
   * FIELDS
   * this.rows           -int
   * this.cols           -int
   * this.numMines       -int
   * this.size           -int
   * this.rand           -Random
   * this.board          -ArrayList<ArrayList<Cell>>
   * 
   * METHODS
   * this.makeScene()                    -WorldScene
   * this.setMine(ArrayList<Coord>)      -void
   * this.makeBoard()                    -ArrayList<ArrayList<Cell>>
   * this.drawRow(ArrayList<Cell>, int)  -WorldImage
   * this.drawBoard(int)                 -WorldImage
   * 
   * this.worldEnds()                    -WorldEnd
   * this.onMouseClicked(Posn, String)   -void
   * this.clickedCell(Posn)              -Cell
   * this.onKeyEvent(key)                -void
   */

  // draws board onto worldscene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.cols * this.size, this.rows * this.size);
    scene.placeImageXY(this.drawBoard(size),
        this.cols * this.size / 2, this.rows * this.size / 2);

    return scene;
  }

  // EFFECT: randomly places mines in cells
  public void setMine(ArrayList<Coord> mineCoords) {
    for (Coord coord : mineCoords) {
      int row = coord.row;
      int col = coord.col;

      if (row >= 0 && row < rows && col >= 0 && col < cols) {
        Cell cell = board.get(row).get(col);
        cell.placeMine();
      }
    }
  }

  // constructs the board and adds neighbors
  public ArrayList<ArrayList<Cell>> makeBoard() {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < rows; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < cols; j++) {
        row.add(new Cell(false, false, false));

      }
      board.add(row);
    }

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++ ) {
        Cell curr = board.get(i).get(j);
        if (i < rows - 1) {
          Cell bottom = board.get(i + 1).get(j);
          curr.addNeighbors(bottom);
          if (j > 0) {
            Cell bottomLeft = board.get(i + 1).get(j - 1);
            curr.addNeighbors(bottomLeft);
          }
          if (j < cols - 1) {
            Cell bottomRight = board.get(i + 1).get(j + 1);
            curr.addNeighbors(bottomRight);
          }
        }
        if (j < cols - 1) {
          Cell right = board.get(i).get(j + 1);
          curr.addNeighbors(right);
        }
      }
    }
    return board;
  }

  //draw a list of cells, represents a row 
  public WorldImage drawRow(ArrayList<Cell> row, int size) {
    WorldImage rowImage = new EmptyImage();

    for (Cell cell : row) {
      rowImage = new BesideImage(rowImage, cell.drawCell(size));
    }
    return rowImage;
  }

  //draws the board by drawing each row and stacking them
  public WorldImage drawBoard(int size) {
    WorldImage boardImage = new EmptyImage();
    for (ArrayList<Cell> row : this.board) {
      boardImage = new AboveImage(boardImage, this.drawRow(row, size));
    }
    return boardImage;
  }

  //world end conditions 
  public WorldEnd worldEnds() {
    boolean winCond = new Utils().isAllRevealed(this);
    TextImage loseText = new TextImage("YOU LOSE", size, Color.red);
    TextImage winText = new TextImage("YOU WIN", size, Color.green);

    WorldScene scene = this.makeScene();

    //lose
    if (this.gameOver && !this.win) {
      scene.placeImageXY(loseText, this.cols * this.size / 2,
          this.rows * this.size / 2);
      return new WorldEnd(true, scene);
    }

    //win
    if (winCond) {
      scene.placeImageXY(winText, this.cols * this.size / 2,
          this.rows * this.size / 2);
      return new WorldEnd(true, scene);
    }
    else {
      return new WorldEnd(false, scene);
    }
  }

  // on click events 
  // EFFECT: changes occur to the game based on which cell is clicked 
  public void onMouseClicked(Posn pos, String buttonName) {
    Cell clickedCell = this.clickedCell(pos);

    // LEFT CLICK
    if (buttonName.equals("LeftButton")) {
      //player clicks on cell with mine: lose, reveal all mines 
      if (clickedCell.isMine() && !clickedCell.isFlagged()) {
        new Utils().revealBoard(this.board);
        this.gameOver = true;
        this.win = false;
      }
      // cell clicked is unrevealed and isn't a mine - floodfill
      if (!clickedCell.isMine() && !clickedCell.isFlagged()) {
        clickedCell.revealNeighbors();
      }
      // not revealed - nothing should happen
    }
    //RIGHT CLICK
    if (buttonName.equals("RightButton")) {
      // right and flagged - removes the flag
      if (clickedCell.isFlagged() && !clickedCell.isRevealed()) {
        clickedCell.removeFlag();
      }
      // right and not flagged - adds the flag
      else if (!clickedCell.isFlagged() && !clickedCell.isRevealed()) {
        clickedCell.placeFlag();
      }
      // right and revealed - nothing
    }
  }

  //given pos, returns which cell is within that pos
  Cell clickedCell(Posn pos) {   
    int matchingRow = 0;
    int matchingCol = 0;

    //if given x coord is within x range, update to that index
    //if given y coord is within y range, update to that index
    for (int i = 0; i < board.size(); i++)  { //for every row 
      int minY = i * size;
      int maxY = minY + size;
      if (pos.y < maxY && pos.y > minY) {
        matchingRow = i;
      }
      for (int x = 0; x < board.get(i).size(); x++) { // for every cell in row
        int minX = x * size;
        int maxX = minX + size;
        if (pos.x < maxX && pos.x > minX) {
          matchingCol = x;
        }
      }
    }
    Cell cellInRange = this.board.get(matchingRow).get(matchingCol);  
    return cellInRange;
  }

  
  public void onKeyEvent(String key) {
    ArrayList<Coord> positions = new ArrayList<Coord>();
    Minesweeper newGame = new Minesweeper(this.rows, this.cols, 
        this.numMines, this.size);
    if (key.equals("r")) {
      this.rows = newGame.rows;
      this.cols = newGame.cols;
      this.numMines = newGame.numMines;
      this.board = newGame.makeBoard();
      positions = new Utils().randomGen(numMines, rows, cols, new Random());
      setMine(positions);
      this.size = newGame.size;
      this.gameOver = false;
      // returning new Game
    }
  }

}

//represents a cell in the game
class Cell {
  ArrayList<Cell> neighbors;
  Boolean revealed;
  Boolean mine;
  Boolean flagged;

  Cell(Boolean revealed, Boolean mine, Boolean flagged) {
    this.neighbors = new ArrayList<Cell>();
    this.revealed = revealed;
    this.mine = mine;
    this.flagged = flagged;
  }

  /* CELL TEMPLATE
   * FIELDS
   * this.neighbors  -ArrayList<Cell>
   * this.revealed   -Boolean
   * this.mine       -Boolean
   * this.flagged    -Boolean
   * 
   * METHODS
   * this.count()             -int
   * this.addNeighbors(Cell)  -void
   * this.hasNearbyMine()     -boolean
   * this.drawCell(int)       -WorldImage
   * this.revealNeighbors()   -void
   * 
   * this.placeMine()    -void
   * this.revealCell()   -void
   * this.placeFlag()    -void
   * this.removeFlag()   -void
   * this.isMine()       -boolean
   * this.isRevealed()   -boolean
   * this.isFlagged()    -boolean
   * 
   */

  // counts the number of mines in neighboring cells
  public int count() {
    int num = 0;

    for (Cell c: neighbors) {
      if (c.mine) {
        num++;
      }
    }
    return num;
  }

  // EFFECT: adds the given cell to this cell's list of neighbors 
  // and adds this cell to the given cell's list of neighbors
  public void addNeighbors(Cell c) {
    this.neighbors.add(c);
    c.neighbors.add(this);
  }

  // checks neighbors list to see if there are any neighboring mines 
  public boolean hasNearbyMine() {
    for (Cell neighbor : neighbors) {
      if (neighbor.mine) {
        return true;
      }
    }
    return false;
  }

  // draws the cell
  public WorldImage drawCell(int size) {
    // outline
    RectangleImage outline = new RectangleImage(size, size, OutlineMode.OUTLINE,
        Color.black);
    // gray square with outline
    OverlayImage grayCell = new OverlayImage(outline,
        new RectangleImage(size, size, OutlineMode.SOLID, Color.gray));
    // white square with outline
    OverlayImage whiteCell = new OverlayImage(outline,
        new RectangleImage(size, size, OutlineMode.SOLID, Color.white));

    // draw cell that is flagged 
    if (this.flagged) {
      return new OverlayImage(new EquilateralTriangleImage(size / 2, 
          OutlineMode.SOLID, Color.ORANGE), grayCell);
    }
    // draw cell that is revelead and has a mine
    if (this.revealed && this.mine) {
      return new OverlayImage(new CircleImage(size / 4, OutlineMode.SOLID, Color.BLACK),
          whiteCell);
    }
    // draw hidden cell 
    else if (!this.revealed && !this.flagged) {
      return grayCell;
    }
    // draw cell that is revealed and has neighboring mines, different colors for each num 
    else if (this.revealed && this.hasNearbyMine()) {
      if (this.count() == 1) {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.BLUE), whiteCell);
      }
      if (this.count() == 2) {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.GREEN), whiteCell);
      }
      if (this.count() == 3) {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.ORANGE), whiteCell);
      }
      if (this.count() == 4) {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.RED), whiteCell);
      }
      if (this.count() == 5) {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.MAGENTA), whiteCell);
      }
      else {
        return new OverlayImage(new TextImage(Integer.toString(this.count()), size / 2,
            Color.BLACK), whiteCell);
      }
    }
    // draw cell that is revealed and doesnt have neighboring mines 
    else { 
      return whiteCell;
    }
  }

  // EFFECT: loops through reveals all of the neighbors 
  // of the cell that aren't mines
  public void revealNeighbors() {
    if (!this.revealed && !this.flagged) {
      this.revealed = true;

      if (this.count() == 0 && !this.mine) {
        new Utils().floodfill(this.neighbors);
      }
    }
  }

  // Effect: places mines in cells
  public void placeMine() {
    this.mine = true;
  }

  // Effect: reveals cell
  public void revealCell() {
    this.revealed = true;
  }

  // Effect: flags cell
  public void placeFlag() {
    this.flagged = true;

  }

  // Effect: unflags cell 
  public void removeFlag() {
    this.flagged = false;
  }

  // returns whether the cell contains a mine
  public boolean isMine() {
    return this.mine;
  }

  // returns whether the cell is revealed
  public boolean isRevealed() {
    return this.revealed;
  }

  // returns whether the cell is revealed
  public boolean isFlagged() {
    return this.flagged;
  }

}

// represents a coordinate (row,col)
class Coord {
  int row;
  int col;

  Coord(int row, int col) {
    this.row = row;
    this.col = col;
  }

  /* COORD TEMPLATE
   * FIELDS
   * this.row    -int
   * this.col    -int
   */
}

//util class for random methods 
class Utils {
  Random rand = new Random();

  //produces a random list of n distinct coords
  ArrayList<Coord> randomGen(int n, int rows, int cols, Random r) {
    ArrayList<Coord> currentList = new ArrayList<Coord>();

    while (currentList.size() < n) {
      //generate a random coord
      Coord randCoord = randomCoord(rows, cols, r);
      if (!currentList.contains(randCoord)) {
        currentList.add(randCoord);
      }
      //if it doesnt exist in currentlist, add to currentlist
      //else generate another random coord 
    }
    return currentList; 
  }

  //randomCoord method for testing 
  Coord randomCoord(int rows, int cols, Random r) {
    return new Coord(r.nextInt(rows), r.nextInt(cols));

  }

  //EFFECT: given a board, reveals all cells
  void revealBoard(ArrayList<ArrayList<Cell>> board) {
    for (int i = 0; i < board.size(); i++) { //row 

      ArrayList<Cell> row = board.get(i);
      for (Cell c : row) {
        c.revealCell();
      }
    }
  }

  //given arraylist<cells> checks if all cells 
  //without mines are revealed
  boolean isAllRevealed(Minesweeper ms) {
    ArrayList<ArrayList<Cell>> board = ms.board;
    int count = 0;

    //if its not a mine and revealed add to result
    for (int i = 0; i < board.size(); i++) {
      for (int x = 0; x < board.get(i).size(); x++) {
        if (!board.get(i).get(x).mine 
            && board.get(i).get(x).revealed ) {
          count = count + 1;
        }
      }
    }
    // check if count is correct number  
    return count == ms.cols * ms.rows - ms.numMines; 
  }

  // EFFECT: checks its neighbors to floodfill
  public void floodfill(ArrayList<Cell> arr) {
    for (int i = 0; i < arr.size(); i++) {
      arr.get(i).revealNeighbors();
    }
  }
}

class ExampleMinesweeper {
  Minesweeper ms;
  Minesweeper ms2;
  Minesweeper ms3;
  Minesweeper ms4;
  Minesweeper ms5;

  Cell exampleCell1;
  Cell exampleCell3;
  Cell exampleCell4;
  Cell exampleCell5;
  Cell exampleCell6;
  ArrayList<Coord> randCoordList = new ArrayList<Coord>();
  ArrayList<Cell> cellList = new ArrayList<Cell>();
  ArrayList<ArrayList<Cell>> exampleBoard = new ArrayList<ArrayList<Cell>>();
  ArrayList<ArrayList<Cell>> exampleBoard2 = new ArrayList<ArrayList<Cell>>();
  ArrayList<ArrayList<Cell>> exampleBoard3 = new ArrayList<ArrayList<Cell>>();

  ArrayList<Cell> exampleRow0 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow1 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow2 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow3 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow4 = new ArrayList<Cell>();

  ArrayList<Cell> exampleRow5 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow6 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow7 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow8 = new ArrayList<Cell>();

  ArrayList<Cell> exampleRow9 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow10 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow11 = new ArrayList<Cell>();
  ArrayList<Cell> exampleRow12 = new ArrayList<Cell>();

  ArrayList<ArrayList<Cell>> board1;

  Random randTest;
  ArrayList<Coord> coordList;

  Cell cell1; //flagged
  Cell cell2; //red circle 
  Cell cell3; //number
  Cell cell4; //hidden
  Cell cell5; //empty

  Cell cell6; //hidden
  Cell cell7; //hidden
  Cell cell8; //hidden
  Cell cell9; //hidden
  Cell cell10; //hidden
  Cell cell11; //hidden
  Cell cell12; //hidden
  Cell cell13; //hidden

  Cell exampleCell = new Cell(false, false, false);
  Cell exampleCell2 = new Cell(true, true, false);


  void initConditions() {
    this.ms = new Minesweeper(4, 3, 3, 50, this.randTest);
    this.ms2 = new Minesweeper(4, 3, 3, 50, this.randTest);
    this.ms3 = new Minesweeper(4, 2, 3, 50, this.randTest);
    this.ms4 = new Minesweeper(4, 2, 3, 50, this.randTest);
    this.ms5 = new Minesweeper(30, 19, 50, 40);
    this.randTest = new Random(5); 

    this.exampleCell1 = new Cell(false, true, false);
    this.coordList = new ArrayList<Coord>();

    this.cell6 = new Cell(false, false, false);
    this.exampleRow9.add(this.cell6);
    this.cell7 = new Cell(false, false, false);
    this.exampleRow9.add(this.cell7);
    this.cell8 = new Cell(false, false, false);
    this.exampleRow10.add(this.cell8);
    this.cell9 = new Cell(false, false, false);
    this.exampleRow10.add(this.cell9);
    this.cell10 = new Cell(false, false, false);
    this.exampleRow11.add(this.cell10);
    this.cell11 = new Cell(false, false, false);
    this.exampleRow11.add(this.cell11);
    this.cell12 = new Cell(false, false, false);
    this.exampleRow12.add(this.cell12);
    this.cell13 = new Cell(false, false, false);
    this.exampleRow12.add(this.cell13);

    this.cell6.addNeighbors(this.cell7);
    this.cell6.addNeighbors(this.cell8);
    this.cell6.addNeighbors(this.cell9);
    this.cell7.addNeighbors(this.cell8);
    this.cell7.addNeighbors(this.cell9);
    this.cell8.addNeighbors(this.cell9);
    this.cell8.addNeighbors(this.cell10);
    this.cell8.addNeighbors(this.cell11);
    this.cell9.addNeighbors(this.cell10);
    this.cell9.addNeighbors(this.cell11);
    this.cell10.addNeighbors(this.cell11);
    this.cell10.addNeighbors(this.cell12);
    this.cell10.addNeighbors(this.cell13);
    this.cell11.addNeighbors(this.cell12);
    this.cell11.addNeighbors(this.cell13);
    this.cell12.addNeighbors(this.cell13);

    this.exampleBoard3.add(this.exampleRow9);
    this.exampleBoard3.add(this.exampleRow10);
    this.exampleBoard3.add(this.exampleRow11);
    this.exampleBoard3.add(this.exampleRow12);


    this.exampleCell1 = new Cell(false, true, false);
    Cell neighbor1 = new Cell(true, false, false);
    Cell neighbor2 = new Cell(true, true, false);
    Cell neighbor3 = new Cell(false, false, false);
    Cell neighbor4 = new Cell(true, true, false);
    this.exampleCell1.addNeighbors(neighbor1);
    this.exampleCell1.addNeighbors(neighbor2);
    this.exampleCell1.addNeighbors(neighbor3);
    this.exampleCell1.addNeighbors(neighbor4);

    this.exampleCell4 = new Cell(true, true, false);
    Cell neighbor5 = new Cell(true, false, false);
    Cell neighbor6 = new Cell(true, true, false);
    Cell neighbor7 = new Cell(true, false, false);
    Cell neighbor8 = new Cell(true, true, false);
    this.exampleCell4.addNeighbors(neighbor5);
    this.exampleCell4.addNeighbors(neighbor6);
    this.exampleCell4.addNeighbors(neighbor7);
    this.exampleCell4.addNeighbors(neighbor8);

    this.exampleCell5 = new Cell(false, false, false);
    Cell neighbor9 = new Cell(true, false, false);
    Cell neighbor10 = new Cell(true, false, false);
    Cell neighbor11 = new Cell(false, false, false);
    this.exampleCell5.addNeighbors(neighbor9);
    this.exampleCell5.addNeighbors(neighbor10);
    this.exampleCell5.addNeighbors(neighbor11);

    this.exampleRow0 = new ArrayList<>();
    this.exampleRow0.add(new Cell(false, false, false));
    this.exampleRow0.add(new Cell(false, false, false));
    this.exampleRow0.add(new Cell(false, false, false));

    this.exampleRow1 = new ArrayList<>();
    this.exampleRow1.add(new Cell(false, true, false));
    this.exampleRow1.add(new Cell(false, true, false));
    this.exampleRow1.add(new Cell(false, false, false));

    this.exampleRow2 = new ArrayList<>();
    this.exampleRow2.add(new Cell(true, true, false));
    this.exampleRow2.add(new Cell(true, false, false));
    this.exampleRow2.add(new Cell(true, false, false));

    this.exampleRow3 = new ArrayList<>();
    this.exampleRow3.add(new Cell(false, false, false));
    this.exampleRow3.add(new Cell(true, false, false));
    this.exampleRow3.add(new Cell(true, false, false));

    this.exampleRow4 = new ArrayList<>();
    this.exampleRow4.add(new Cell(true, false, false));
    this.exampleRow4.add(new Cell(true, false, false));
    this.exampleRow4.add(new Cell(true, false, false));

    this.exampleRow5 = new ArrayList<>();
    this.exampleRow5.add(new Cell(false, false, false));
    this.exampleRow5.add(new Cell(false, false, false));
    this.exampleRow5.add(new Cell(false, false, false));

    this.exampleRow6 = new ArrayList<>();
    this.exampleRow6.add(new Cell(false, false, false));
    this.exampleRow6.add(new Cell(false, false, false));
    this.exampleRow6.add(new Cell(false, false, false));

    this.exampleRow7 = new ArrayList<>();
    this.exampleRow7.add(new Cell(false, false, false));
    this.exampleRow7.add(new Cell(false, false, false));
    this.exampleRow7.add(new Cell(false, false, false));

    this.exampleRow8 = new ArrayList<>();
    this.exampleRow8.add(new Cell(false, false, false));
    this.exampleRow8.add(new Cell(false, false, false));
    this.exampleRow8.add(new Cell(false, false, false));

    this.exampleBoard2 = new ArrayList<ArrayList<Cell>>();
    this.exampleBoard2.add(this.exampleRow5);
    this.exampleBoard2.add(this.exampleRow6);
    this.exampleBoard2.add(this.exampleRow7);
    this.exampleBoard2.add(this.exampleRow8);

    this.exampleBoard = new ArrayList<ArrayList<Cell>>();
    this.exampleBoard.add(this.exampleRow0);
    this.exampleBoard.add(this.exampleRow1);
    this.exampleBoard.add(this.exampleRow2);
    this.exampleBoard.add(this.exampleRow3);

    this.coordList.add(new Coord(1, 1));
    this.coordList.add(new Coord(2, 0));
    this.coordList.add(new Coord(1, 0));

    this.ms.setMine(this.coordList);
    this.ms.board.get(2).get(0).revealed = true;
    this.ms.board.get(2).get(1).revealed = true;
    this.ms.board.get(2).get(2).revealed = true;
    this.ms.board.get(3).get(1).revealed = true;
    this.ms.board.get(3).get(2).revealed = true;
    this.ms.board.get(1).get(0).flagged = true;
    this.ms.board.get(1).get(1).flagged = true;

    this.exampleCell6 = new Cell(false, true, false);
    Cell neighbor12 = new Cell(false, true, false);
    Cell neighbor13 = new Cell(false, false, false);
    Cell neighbor14 = new Cell(false, false, false);
    this.exampleCell6.addNeighbors(neighbor12);
    this.exampleCell6.addNeighbors(neighbor13);
    this.exampleCell6.addNeighbors(neighbor14);

    this.exampleCell3 = new Cell(false, true, true);
  }

  // MINESWEEPER METHODS
  // test makeScene 
  boolean testMakeScene(Tester t) {
    this.initConditions();
    WorldScene ws = new WorldScene(150, 200);
    ws.placeImageXY( this.ms.drawBoard(50), 75, 100);

    return t.checkExpect(this.ms.makeScene(), ws);
  }

  // test set mine
  void testSetMine(Tester t) {
    this.initConditions();
    //this.ms.setMine(this.coordList);

    // mines in: (row 1 col 1), (row 2 col 0), (row 1 col 0)
    t.checkExpect(this.ms.board.get(1).get(1).isMine(), true);
    t.checkExpect(this.ms.board.get(2).get(0).isMine(), true);
    t.checkExpect(this.ms.board.get(1).get(0).isMine(), true);  
  }

  //test makeBoard
  boolean testMakeBoard(Tester t) {
    this.initConditions();

    // testing sameness of makeBoard on the same instance of a 4x2 with 3 mines
    return t.checkExpect(this.ms3.makeBoard(), this.ms4.makeBoard())
        // checking each cell for sameness
        && t.checkExpect(this.ms3.makeBoard().get(0).get(0), 
            this.ms4.makeBoard().get(0).get(0))
        && t.checkExpect(this.ms3.makeBoard().get(0).get(1), 
            this.ms4.makeBoard().get(0).get(1))
        && t.checkExpect(this.ms3.makeBoard().get(1).get(0), 
            this.ms4.makeBoard().get(1).get(0))
        && t.checkExpect(this.ms3.makeBoard().get(1).get(1), 
            this.ms4.makeBoard().get(1).get(1))
        && t.checkExpect(this.ms3.makeBoard().get(2).get(0), 
            this.ms4.makeBoard().get(2).get(0))
        && t.checkExpect(this.ms3.makeBoard().get(2).get(1), 
            this.ms4.makeBoard().get(2).get(1))
        && t.checkExpect(this.ms3.makeBoard().get(3).get(0), 
            this.ms4.makeBoard().get(3).get(0))
        && t.checkExpect(this.ms3.makeBoard().get(3).get(1), 
            this.ms4.makeBoard().get(3).get(1));
  }

  // test drawRow
  boolean testDrawRow(Tester t) {
    this.initConditions();

    // test drawRow on a row of hidden cells
    return t.checkExpect(this.ms.drawRow(this.exampleRow0, 50), 
        new BesideImage(new BesideImage(new BesideImage(new EmptyImage(), 
            this.exampleRow0.get(0).drawCell(50)),
            this.exampleRow0.get(1).drawCell(50)),
            this.exampleRow0.get(2).drawCell(50)))
        // test drawRow on a row of revealed cells
        && t.checkExpect(this.ms.drawRow(this.exampleRow4, 50), 
            new BesideImage(new BesideImage(new BesideImage(new EmptyImage(), 
                this.exampleRow4.get(0).drawCell(50)),
                this.exampleRow4.get(1).drawCell(50)),
                this.exampleRow4.get(2).drawCell(50)));
  }

  // test drawBoard
  boolean testDrawBoard(Tester t) {
    this.initConditions();

    // test drawBoard on a board of hidden cells           
    return t.checkExpect(this.ms2.drawBoard(50), new AboveImage(new AboveImage(
        new AboveImage(new AboveImage(new EmptyImage(), new BesideImage(
            new BesideImage(new BesideImage(new EmptyImage(), 
                this.exampleRow5.get(0).drawCell(50)),
                this.exampleRow5.get(1).drawCell(50)), 
            this.exampleRow5.get(2).drawCell(50))), new BesideImage(new BesideImage(
                new BesideImage(new EmptyImage(), this.exampleRow6.get(0).drawCell(50)), 
                this.exampleRow6.get(1).drawCell(50)),
                this.exampleRow6.get(2).drawCell(50))),
        new BesideImage(new BesideImage(new BesideImage(new EmptyImage(), 
            this.exampleRow7.get(0).drawCell(50)),
            this.exampleRow7.get(1).drawCell(50)),
            this.exampleRow7.get(2).drawCell(50))), new BesideImage(new BesideImage(
                new BesideImage(new EmptyImage(), this.exampleRow8.get(0).drawCell(50)),
                this.exampleRow8.get(1).drawCell(50)), 
                this.exampleRow8.get(2).drawCell(50))));

  }

  //test worldEnds
  boolean testWorldEnds(Tester t) {
    this.initConditions();
    TextImage loseText = new TextImage("YOU LOSE", 50, Color.red);
    TextImage winText = new TextImage("YOU WIN", 50, Color.green);

    //setup for losing worldEnd
    this.ms.gameOver = true;
    this.ms.win = false;
    WorldScene scene1 = ms.makeScene();
    scene1.placeImageXY(loseText, 75, 100);

    //setup for winning worldEnd
    this.ms2.setMine(this.coordList);
    this.ms2.board.get(2).get(0).revealed = true;
    this.ms2.board.get(2).get(1).revealed = true;
    this.ms2.board.get(2).get(2).revealed = true;
    this.ms2.board.get(3).get(1).revealed = true;
    this.ms2.board.get(3).get(2).revealed = true;
    this.ms2.board.get(1).get(0).flagged = true;
    this.ms2.board.get(1).get(1).flagged = true;
    this.ms2.board.get(0).get(0).revealed = true;
    this.ms2.board.get(0).get(1).revealed = true;
    this.ms2.board.get(0).get(2).revealed = true;
    this.ms2.board.get(1).get(2).revealed = true;
    this.ms2.board.get(3).get(0).revealed = true;
    WorldScene scene2 = ms2.makeScene();
    scene2.placeImageXY(winText, 75, 100);

    //setup for game hasnt ended yet
    WorldScene scene3 = ms3.makeScene();

    //player loses
    return t.checkExpect(this.ms.worldEnds(), new WorldEnd(true, scene1))
        //player wins
        && t.checkExpect(this.ms2.worldEnds(), new WorldEnd(true, scene2))
        //game hasnt ended yet 
        && t.checkExpect(this.ms3.worldEnds(), new WorldEnd(false, scene3));
  }

  // test mouseClicked 
  void testOnMouseClicked(Tester t) {
    this.initConditions();
    // left click and isn't a mine 
    t.checkExpect(this.ms.board.get(0).get(0).isRevealed(), false);
    this.ms.onMouseClicked(new Posn(100, 100), "LeftButton");
    t.checkExpect(this.ms.board.get(0).get(0).isRevealed(), true);

    // left click and is a mine - game over and lose & reveal all mines
    this.initConditions();

    t.checkExpect(this.ms.board.get(0).get(0).isRevealed(), false);
    this.ms.onMouseClicked(new Posn(100, 100), "LeftButton");
    t.checkExpect(this.ms.board.get(0).get(0).isRevealed(), true);

    // right and not flagged - places the flag
    this.initConditions();

    t.checkExpect(this.ms.board.get(0).get(0).isFlagged(), false);
    this.ms.onMouseClicked(new Posn(100, 100), "RightButton");
    t.checkExpect(this.ms.board.get(0).get(0).isFlagged(), true);

    // right and flagged - removes the flag
    this.initConditions();

    t.checkExpect(this.ms.board.get(1).get(1).isFlagged(), true);
    this.ms.onMouseClicked(new Posn(55, 55), "RightButton");
    t.checkExpect(this.ms.board.get(1).get(1).isFlagged(), false);

  }

  //test clickedCell
  boolean testClickedCell(Tester t) {
    this.initConditions();
    Posn pos1 = new Posn(60, 140);
    Posn pos2 = new Posn(25, 25);
    Posn pos3 = new Posn(140, 55);

    return t.checkExpect(this.ms.clickedCell(pos1),
        this.ms.board.get(2).get(1))
        && t.checkExpect(this.ms.clickedCell(pos2),
            this.ms.board.get(0).get(0))
        && t.checkExpect(this.ms.clickedCell(pos3),
            this.ms.board.get(1).get(2));
  }



  // Effect: restarts the game if "r" is pressed
  void testOnKeyEvent(Tester t) {
    // initial conditions
    this.initConditions();

    //wrong key pressed 
    this.ms.onKeyEvent("a");
    t.checkExpect(this.ms, this.ms);

    //right key pressed and the game restarts
    this.ms.onKeyEvent("r");
    Minesweeper game2 = new Minesweeper(30,19,50,40, this.randTest);
    // checking that required fields reset correctly
    t.checkExpect(this.ms5.rows, game2.rows);
    t.checkExpect(this.ms5.cols, game2.cols);
    t.checkExpect(this.ms5.numMines, game2.numMines);
    t.checkExpect(this.ms5.size, game2.size);
  }



  // test constructor exception
  boolean testConstructorException(Tester t) {
    // test to make sure there aren't more mines than mines
    return t.checkConstructorException(new IllegalArgumentException(
        "More mines than squares. Check again."), 
        "Minesweeper", 4, 3, 15, 10);
  }

  // CELL METHODS
  // test count neighboring mines 
  boolean testCount(Tester t) {
    this.initConditions();

    return t.checkExpect(this.exampleCell1.count(), 2)
        && t.checkExpect(this.ms.board.get(3).get(2).count(), 0);
  }

  //test to add neighbors
  void testAddNeighbors(Tester t) {
    this.initConditions();

    ArrayList<Cell> cellList1 = new ArrayList<Cell>();
    ArrayList<Cell> cellList2 = new ArrayList<Cell>();
    // before conditions
    t.checkExpect(this.exampleCell.neighbors, cellList2);
    t.checkExpect(this.exampleCell2.neighbors, cellList1); 

    this.exampleCell.addNeighbors(exampleCell2);
    cellList1.add(exampleCell);
    cellList2.add(exampleCell2);

    // after conditions
    t.checkExpect(this.exampleCell.neighbors, cellList2);
    t.checkExpect(this.exampleCell2.neighbors, cellList1);      
  }

  //test to check if there's neighboring mine 
  boolean testHasNearbyMine(Tester t) {
    this.initConditions();

    return t.checkExpect(this.exampleCell1.hasNearbyMine(), true);
  }

  //boolean testDrawCell(Tester t) {
  boolean testDrawCell(Tester t) {
    initConditions();

    int size = 50;
    // outline
    RectangleImage outline = new RectangleImage(size, size,
        OutlineMode.OUTLINE, Color.black);
    // gray square with outline
    OverlayImage grayCell = new OverlayImage(outline,
        new RectangleImage(size, size, OutlineMode.SOLID, Color.gray));
    // white square with outline
    OverlayImage whiteCell = new OverlayImage(outline,
        new RectangleImage(size, size, OutlineMode.SOLID, Color.white));


    this.cell1 = this.ms.board.get(1).get(0); //flagged
    this.cell2 = this.ms.board.get(2).get(0); //mine
    this.cell3 = this.ms.board.get(2).get(1); //number
    this.cell4 = this.ms.board.get(3).get(0); //hidden
    this.cell5 = this.ms.board.get(3).get(2); //empty

    //draw hidden cell that is flagged
    return t.checkExpect(this.cell1.drawCell(size),
        new OverlayImage(new EquilateralTriangleImage(size / 2, 
            OutlineMode.SOLID, Color.ORANGE), grayCell))
        //draw revealed cell that has mine 
        && t.checkExpect(this.cell2.drawCell(size),
            new OverlayImage(new CircleImage(size / 4, OutlineMode.SOLID, Color.BLACK),
                whiteCell))
        //draw revealed empty cell that has neighboring mines 
        && t.checkExpect(this.cell3.drawCell(size),
            new OverlayImage(new TextImage(Integer.toString(3), size / 2, Color.ORANGE),
                whiteCell))
        //draw hidden cell 
        && t.checkExpect(this.cell4.drawCell(size), grayCell)  
        //draw revealed empty cell that has no neighboring mines 
        && t.checkExpect(this.cell5.drawCell(size), whiteCell); 
  }

  // test revealNeighbors
  void testRevealNeighbors(Tester t) {
    // initial conditions
    this.initConditions();

    // calling reveal on the first cell on a board without mines and hidden
    this.ms.board.get(0).get(0).revealNeighbors();

    ArrayList<Cell> cellList2 = new ArrayList<Cell>();
    cellList2.add(this.ms.board.get(0).get(1));
    cellList2.add(this.ms.board.get(1).get(1));
    cellList2.add(this.ms.board.get(1).get(0));

    // checking that the cells are still the same 
    t.checkExpect(this.ms.board.get(0).get(0).neighbors.containsAll(cellList2), true);   

    // calling reveal on a cell that has neighbors with mines, hidden, and is a mine
    this.exampleCell6.revealNeighbors();
    ArrayList<Cell> cellList3 = new ArrayList<Cell>();
    cellList3.add(this.exampleCell6.neighbors.get(0));
    cellList3.add(this.exampleCell6.neighbors.get(1));
    cellList3.add(this.exampleCell6.neighbors.get(2));

    // checking that the cells are the same because it won't loop through the neighbors
    t.checkExpect(this.exampleCell6.neighbors.containsAll(cellList3), true);
  }

  //test place mine
  void testPlaceMine(Tester t) {
    this.initConditions();

    // before conditions
    t.checkExpect(this.exampleCell.mine, false);
    t.checkExpect(this.exampleCell2.mine, true);

    this.exampleCell.placeMine();
    this.exampleCell2.placeMine();

    // after method has been called
    t.checkExpect(this.exampleCell.mine, true);
    t.checkExpect(this.exampleCell2.mine, true);
  }

  // test reveal cell
  void testRevealCell(Tester t) {
    this.initConditions();

    // before conditions
    t.checkExpect(this.exampleCell.revealed, false);
    t.checkExpect(this.exampleCell2.revealed, true);

    this.exampleCell.revealCell();
    this.exampleCell2.revealCell();

    // after method has been called
    t.checkExpect(this.exampleCell.revealed, true);
    t.checkExpect(this.exampleCell2.revealed, true);
  }

  // test place flag
  void testPlaceFlag(Tester t) {
    this.initConditions();
    // before conditions
    t.checkExpect(this.exampleCell.flagged, false);
    t.checkExpect(this.exampleCell2.flagged, false);

    this.exampleCell.placeFlag();
    this.exampleCell2.placeFlag();

    // after method has been called
    t.checkExpect(this.exampleCell.flagged, true);
    t.checkExpect(this.exampleCell2.flagged, true);
  }

  // test isMine
  boolean testIsMine(Tester t) {
    // test on a cell that is a mine
    return t.checkExpect(this.exampleCell1.isMine(), true)
        // test on a cell that isn't a mine
        && t.checkExpect(this.exampleCell5.isMine(), false);
  }

  //test removeFlag
  void testRemoveFlag(Tester t) {
    // initial conditions
    this.initConditions();

    // calls remove flag on a cell that was flagged
    this.exampleCell3.removeFlag();
    t.checkExpect(this.exampleCell3, new Cell(false, true, false));

    // calls remove flag on a cell that wasn't flagged
    this.exampleCell5.removeFlag();
    t.checkExpect(this.exampleCell5, this.exampleCell5);
  }


  // test isFlagged
  boolean testIsFlagged(Tester t) {
    this.initConditions();

    return t.checkExpect(this.exampleCell3.isFlagged(), true);
  }

  // test isRevealed
  boolean testIsRevealed(Tester t) {
    this.initConditions();

    // test on a cell that is hidden
    return t.checkExpect(this.exampleCell1.isRevealed(), false)
        // test on a cell that isn't hidden
        && t.checkExpect(this.exampleCell2.isRevealed(), true)
        // test on a cell that is hidden
        && t.checkExpect(this.exampleCell3.isRevealed(), false);
  }



  //UTILS METHODS
  //test to generate a random ArrayList<Coord>
  boolean testRandomGen(Tester t) {
    this.initConditions();
    this.randCoordList.add(new Coord(7, 2));
    this.randCoordList.add(new Coord(4, 4));
    this.randCoordList.add(new Coord(6, 5));

    return t.checkExpect(new Utils().randomGen(3, 10, 10, this.randTest),
        this.randCoordList);
  }

  //test to generate a random coord
  boolean testRandomCoord(Tester t) {
    this.initConditions();
    return t.checkExpect(new Utils().randomCoord(10, 10, this.randTest),
        new Coord(7, 2));
  }

  //test revealBoard
  void testRevealBoard(Tester t) {
    this.initConditions();
    ArrayList<ArrayList<Cell>> board = this.ms.makeBoard();
    new Utils().revealBoard(board);

    t.checkExpect(board.get(0).get(0).revealed, true);
    t.checkExpect(board.get(0).get(1).revealed, true);
    t.checkExpect(board.get(0).get(2).revealed, true);
    t.checkExpect(board.get(1).get(0).revealed, true);
    t.checkExpect(board.get(1).get(1).revealed, true);
    t.checkExpect(board.get(1).get(2).revealed, true);
    t.checkExpect(board.get(2).get(0).revealed, true);
    t.checkExpect(board.get(2).get(1).revealed, true);
    t.checkExpect(board.get(2).get(2).revealed, true);
    t.checkExpect(board.get(3).get(0).revealed, true);
    t.checkExpect(board.get(3).get(1).revealed, true);
    t.checkExpect(board.get(3).get(2).revealed, true);
  }

  //test isAllRevealed
  boolean testIsAllRevealed(Tester t) {
    this.initConditions();
    this.ms.board.get(0).get(0).revealed = true;
    this.ms.board.get(0).get(1).revealed = true;
    this.ms.board.get(0).get(2).revealed = true;
    this.ms.board.get(1).get(2).revealed = true;
    this.ms.board.get(3).get(0).revealed = true;

    return t.checkExpect(new Utils().isAllRevealed(this.ms), true)
        && t.checkExpect(new Utils().isAllRevealed(this.ms2), false);
  }

  //test floodfill
  void testFloodfill(Tester t) {
    this.initConditions();

    t.checkExpect(this.exampleCell1.neighbors.get(0).revealed, true);
    t.checkExpect(this.exampleCell1.neighbors.get(1).revealed, true);
    t.checkExpect(this.exampleCell1.neighbors.get(2).revealed, false);
    t.checkExpect(this.exampleCell1.neighbors.get(3).revealed, true);

    new Utils().floodfill(this.exampleCell1.neighbors);

    t.checkExpect(this.exampleCell1.neighbors.get(0).revealed, true);
    t.checkExpect(this.exampleCell1.neighbors.get(1).revealed, true);
    t.checkExpect(this.exampleCell1.neighbors.get(2).revealed, true);
    t.checkExpect(this.exampleCell1.neighbors.get(3).revealed, true);
    // revealed, mine, flag
  }

  //GAME EXAMPLE 

  /*
  //draws a 4x3 board that displays all types of cells 
  void testBigBang(Tester t) {
    int size = this.ms.size;
    int width = this.ms.cols * size;
    int height = this.ms.rows * size;
    double tickRate = 1.0;

    this.ms.bigBang(width, height, tickRate);
  }
   */

  //ACTUAL GAME
  void testBigBang(Tester t) {
    int size = this.ms5.size;
    int width = this.ms5.cols * size;
    int height = this.ms5.rows * size;
    double tickRate = 1.0;

    this.ms5.bigBang(width, height, tickRate);
  }
}