
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import java.util.*;


class ListOfLists<T> implements Iterable<T> {
  ArrayList<ArrayList<T>> list;

  ListOfLists() {
    this.list = new ArrayList<ArrayList<T>>();
  }

  ListOfLists(int num) {
    this.list = new ArrayList<ArrayList<T>>();
    for (int i = 0; i < num; i++) {
      this.addNewList();
    }
  }

  //compute the size of this list of lists
  int size() {
    return this.list.size();
  }

  //EFFECT: adds a new list to the end of this list of lists
  void addNewList() {
    this.list.add(new ArrayList<T>());
  }

  //EFFECT: adds an item to the end of one of the lists in this list of lists
  void add(int index,T object) {
    if (this.size() < index) {
      throw new IndexOutOfBoundsException("Invalid index");
    }
    this.list.get(index).add(object);
  }

  //get a list at a given position
  ArrayList<T> get(int index) {
    if (this.size() < index) {
      throw new IndexOutOfBoundsException("Invalid index");
    }
    return this.list.get(index);
  }

  //creates an Iterator for this list of lists
  public Iterator<T> iterator() {
    return new ListOfListIter<T>(this);
  }
}

class ListOfListIter<T> implements Iterator<T> {
  ListOfLists<T> lol;
  int currentIndex;
  Iterator<T> currentIter;

  // constructor
  ListOfListIter(ListOfLists<T> lol) {
    this.lol = lol;
    this.currentIndex = 0;

    if (this.currentIndex < this.lol.size()) {
      this.currentIter = this.lol.get(currentIndex).iterator();
    }
    else {
      this.currentIter = new ArrayList<T>().iterator();
    }
  }

  //are there any more items to produce?
  public boolean hasNext() {
    return this.currentIter.hasNext() || this.currentIndex < this.lol.size() - 1;
  }

  //produces the next item from the current iterator
  //EFFECT: increments the current index if needed and advances the currentIter 
  public T next() {
    if (!this.currentIter.hasNext()) {
      this.currentIndex += 1;
      if (this.currentIndex < this.lol.size()) {
        this.currentIter = this.lol.get(currentIndex).iterator();
      }
      else {
        this.currentIter = new ArrayList<T>().iterator();
      }
    }
    if (!this.currentIter.hasNext()) {
      throw new NoSuchElementException("no more items!");
    }
    return this.currentIter.next();
  }

}

//Represents an individual tile
class Tile {
  // The number on the tile.  Use 0 to represent the hole
  int value;

  // x and y coordinates of the tile on the board 
  int x;
  int y;

  // if the tile is solved, that means it is in the correct position according to its value
  boolean solved;

  // if the tile is empty, that means it is the empty space
  boolean empty;

  // color of tile; orange if solved, green if not solved
  Color color;

  static int DEFAULT_TILE_SIZE = 100;

  // constructor to make a tile
  Tile(int value, int x, int y, boolean solved, boolean empty, Color color) {
    this.value = value;
    this.x = x;
    this.y = y;
    this.solved = solved;
    this.empty = empty;
    this.color = color;
  }

  //makes the board
  Boolean solve(int x, int y) {
    this.x = x;
    this.y = y;
    if (value == ((x + 1) + (y * 4)) || value == 0) {
      this.color = Color.orange;
      this.solved = true;
    }

    if (!(value == ((x + 1) + (y * 4))) && value != 0) {
      this.color = Color.cyan;
      this.solved = false;
    }

    return this.solved;
  }



  //Draws this tile onto the background at the specified logical coordinates
  WorldImage drawAt(int col, int row, WorldImage background) {
    if (this.value > 0) {
      WorldImage val = new TextImage(String.valueOf(this.value), 18, Color.BLACK);
      WorldImage r = new OverlayImage(val, 
          new OverlayImage(new RectangleImage(DEFAULT_TILE_SIZE - 2, 
          DEFAULT_TILE_SIZE - 2, OutlineMode.SOLID, this.color), 
          new RectangleImage(100, 100, OutlineMode.SOLID, Color.black)));
      return r.movePinholeTo(new Posn(-DEFAULT_TILE_SIZE / 2, -DEFAULT_TILE_SIZE / 2));
    }
    else {
      WorldImage r = new RectangleImage(100, 100, OutlineMode.SOLID, Color.gray);
      return r.movePinholeTo(new Posn(-DEFAULT_TILE_SIZE / 2, -DEFAULT_TILE_SIZE / 2));
    }
  }

}

// class for rendering the game and all its operations
class FifteenGame extends World {

  // no. of tiles in game
  static int DEFAULT_GAME_SIZE = 4;
  int size;

  // represents the rows of tiles
  ListOfLists<Tile> tiles;

  // size of tile squares
  static int DEFAULT_TILE_SIZE = 100;

  // keeps track of the no. of moves made
  int moves;

  // position of empty square
  int empty_x;
  int empty_y;
  Tile empty_t;

  ArrayList<String> key_history = new ArrayList<String>();

  // empty constructor
  FifteenGame() {
    this.size = DEFAULT_GAME_SIZE;
    this.moves = 0;
    this.makeBoard();
  }


  // makes the board
  void makeBoard() {
    this.tiles = new ListOfLists<Tile>(this.size);
    int v = 0;

    // to create random positions for the tiles
    ArrayList<Integer> listx = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3));
    ArrayList<Integer> listy = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3));
    Collections.shuffle(listx);
    Collections.shuffle(listy);

    for (int i = 0; i < this.size; i++) { // vertical position

      for (int j = 0; j < this.size; j++) { // horizontal position
        v++;
        int xpos = listx.get(j);
        int ypos = listy.get(i);
        if (v == 16) {
          Tile t = new Tile(0, xpos, ypos, true, true, Color.gray);
          t.solve(xpos, ypos);
          tiles.add(i, t);
          empty_x = xpos;
          empty_y = ypos;
          empty_t = t;
        }
        else {
          Tile t = new Tile(v, xpos, ypos, false, false, Color.green);
          t.solve(xpos, ypos);
          tiles.add(i, t);
        }
      }
    }
  }


  // given the x and y position, finds the corresponding tile
  Tile findTile(int x, int y) {
    for (int i = 0; i < this.size; i++) { // vertical position
      for (int j = 0; j < this.size; j++) { // horizontal position
        Tile t = this.tiles.get(i).get(j);
        if (t.x == x && t.y == y) {
          return t;
        }
      }
    }
    return null;
  }

  // if all the tiles are solved, returns true, use the example of initGameWon
  // and test should be true, example of initGamePlay and test should be false
  boolean gameOver() {
    for (Tile t: this.tiles) {
      t.solve(t.x, t.y);
      if (!t.solved) {
        return false;
      }
    }
    return true;
  }

  // swaps the positions of 2 tiles
  void swap(Tile tile, Tile empty) {
    empty_x = tile.x;
    empty_y = tile.y;
    tile.solve(empty.x, empty.y);
    empty.solve(empty_x, empty_y);
    empty_t = empty;
  }


  // When you click a direction (example: right), the empty space will swap with its neighbor tile 
  // in that direction, if there is one
  public void onKeyEvent(String k) {
    // needs to handle up, down, left, right to move the hole
    // extra: handle "u" to undo moves
    Tile t;    
    if (k.equals("up")) {
      if (empty_y - 1 >= 0) {
        t = findTile(empty_x, empty_y - 1);
        this.swap(t, this.empty_t);
        this.moves++;
        this.key_history.add(0, k);
      }
    }
    else if (k.equals("down")) {
      if (empty_y + 1 <= 3) {
        t = findTile(empty_x, empty_y + 1);
        this.swap(t, this.empty_t);
        this.moves++;
        this.key_history.add(0, k);
      }
    }
    else if (k.equals("left")) {
      if (empty_x - 1 >= 0) {
        t = findTile(empty_x - 1, empty_y);
        this.swap(t, this.empty_t);
        this.moves++;
        this.key_history.add(0, k);
      }
    }
    else if (k.equals("right")) {
      if (empty_x + 1 <= 3) {
        t = findTile(empty_x + 1, empty_y);
        this.swap(t, this.empty_t);
        this.moves++;
        this.key_history.add(0, k);
      }
    }
    else if (k.equals("u")) {
      if (key_history.size() > 0) {
        k = key_history.get(0);
        if (k.equals("up")) {
          t = findTile(empty_x, empty_y + 1);
          this.swap(t, this.empty_t);
          this.moves--;
        }
        if (k.equals("down")) {
          t = findTile(empty_x, empty_y - 1);
          this.swap(t, this.empty_t);
          this.moves--;
        }
        if (k.equals("left")) {
          t = findTile(empty_x + 1, empty_y);
          this.swap(t, this.empty_t);
          this.moves--;
        }
        if (k.equals("right")) {
          t = findTile(empty_x - 1, empty_y);
          this.swap(t, this.empty_t);
          this.moves--;
        }
        key_history.remove(0);
      }
    }
  }

  // renders a visual  screen of the game
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(600, 600);
    if (!this.gameOver()) {
      for (int i = 0; i < this.size; i++) { // vertical position
        for (int j = 0; j < this.size; j++) { // horizontal position
          Tile t = this.tiles.get(i).get(j);
          scene.placeImageXY(t.drawAt(i, j, new EmptyImage()), t.x * DEFAULT_TILE_SIZE + 50, 
              t.y * DEFAULT_TILE_SIZE + 50);
        }
      }
      scene.placeImageXY(new TextImage("Fifteen Game", 20 ,Color.black), 250, 25);
      scene.placeImageXY(new TextImage("Moves: " + String.valueOf(this.moves), 
          20 ,Color.black), 250, 475);
    }
    else {
      scene.placeImageXY(new TextImage("You Win!", Color.black), 250, 25);
    }
    return scene;
  }

}

// tests
class ExampleFifteenGame {
  Tile tile1;
  Tile tile2;
  Tile tile3;
  Tile tile4;
  Tile tile5;
  Tile tile6;
  Tile tile7;
  Tile tile8;
  Tile tile9;
  Tile tile10;
  Tile tile11;
  Tile tile12;
  Tile tile13;
  Tile tile14;
  Tile tile15;
  Tile tile0;
  // x and y position of the tiles randomly shuffled to generate an unordered board
  ArrayList<Integer> listx = new ArrayList<Integer>(Arrays.asList(1, 0, 2, 3));
  ArrayList<Integer> listy = new ArrayList<Integer>(Arrays.asList(0 , 3, 2, 1));

  ListOfLists<Tile> exlist;

  FifteenGame FifteenGameEx;

  void testGame(Tester t) {
    FifteenGame g = new FifteenGame();
    g.bigBang(600, 600);
  }

  // example of game when tiles are in correct order
  void initGameWon() {      
    FifteenGameEx = new FifteenGame();
    tile1 = new Tile(1, 0, 0, false, false, Color.cyan);
    tile2 = new Tile(2, 1, 0, false, true, Color.cyan);
    tile3 = new Tile(3, 2, 0, false, false, Color.cyan);
    tile4 = new Tile(4, 3, 0, false, false, Color.cyan);
    tile5 = new Tile(5, 0, 1, false, false, Color.cyan);
    tile6 = new Tile(6, 1, 1, false, false, Color.cyan);
    tile7 = new Tile(7, 2, 1, false, false, Color.cyan);
    tile8 = new Tile(8, 3, 1, false, false, Color.cyan);
    tile9 = new Tile(9, 0, 2, false, false, Color.cyan);
    tile10 = new Tile(10, 1, 2, false, false, Color.cyan);
    tile11 = new Tile(11, 2, 2, false, false, Color.cyan);
    tile12 = new Tile(12, 3, 2, false, false, Color.cyan);
    tile13 = new Tile(13, 0, 3, false, false, Color.cyan);
    tile14 = new Tile(14, 1, 3, false, false, Color.cyan);
    tile15 = new Tile(15, 2, 3, false, false, Color.cyan);
    tile0 = new Tile(0, 3, 3, true, true, Color.orange);


    exlist = new ListOfLists<Tile>();

    //add 3 lists
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();

    // add first row of tiles
    exlist.add(0,tile1);
    exlist.add(0,tile2);
    exlist.add(0,tile3);
    exlist.add(0,tile4);
    // add second row of tiles
    exlist.add(1,tile5);
    exlist.add(1,tile6);
    exlist.add(1,tile7);
    exlist.add(1,tile8);
    // add third row of tiles
    exlist.add(2,tile9);
    exlist.add(2,tile10);
    exlist.add(2,tile11);
    exlist.add(2,tile12);
    // add fourth row of tiles
    exlist.add(3,tile13);
    exlist.add(3,tile14);
    exlist.add(3,tile15);      
    //add empty tile
    exlist.add(3,tile0);


    FifteenGameEx.tiles = exlist;
  }

  // example of game when tiles are randomly shuffled
  void initGamePlay() {
    FifteenGameEx = new FifteenGame();

    this.tile1 = new Tile(1, listx.get(0), listy.get(0), false, false, Color.cyan);
    tile2 = new Tile(2, listx.get(1), listy.get(0), false, false, Color.cyan);
    tile3 = new Tile(3, listx.get(2), listy.get(0), false, false, Color.cyan);
    tile4 = new Tile(4, listx.get(3), listy.get(0), false, false, Color.cyan);
    tile5 = new Tile(5, listx.get(0), listy.get(1), false, false, Color.cyan);
    tile6 = new Tile(6, listx.get(1), listy.get(1), false, false, Color.cyan);
    tile7 = new Tile(7, listx.get(2), listy.get(1), false, false, Color.cyan);
    tile8 = new Tile(8, listx.get(3), listy.get(1), false, false, Color.cyan);
    tile9 = new Tile(9, listx.get(0), listy.get(2), false, false, Color.cyan);
    tile10 = new Tile(10, listx.get(1), listy.get(2), false, false, Color.cyan);
    tile11 = new Tile(11, listx.get(2), listy.get(2), false, false, Color.cyan);
    tile12 = new Tile(12, listx.get(3), listy.get(2), false, false, Color.cyan);
    tile13 = new Tile(13, listx.get(0), listy.get(3), false, false, Color.cyan);
    tile14 = new Tile(14, listx.get(1), listy.get(3), false, false, Color.cyan);
    tile15 = new Tile(15, listx.get(2), listy.get(3), false, false, Color.cyan);
    tile0 = new Tile(0, listx.get(3), listy.get(3), true, true, Color.orange);

    exlist = new ListOfLists<Tile>();

    //add 3 lists
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();

    // add first row of tiles
    exlist.add(0,tile1);
    exlist.add(0,tile2);
    exlist.add(0,tile3);
    exlist.add(0,tile4);
    // add second row of tiles
    exlist.add(1,tile5);
    exlist.add(1,tile6);
    exlist.add(1,tile7);
    exlist.add(1,tile8);
    // add third row of tiles
    exlist.add(2,tile9);
    exlist.add(2,tile10);
    exlist.add(2,tile11);
    exlist.add(2,tile12);
    // add fourth row of tiles
    exlist.add(3,tile13);
    exlist.add(3,tile14);
    exlist.add(3,tile15);      
    //add empty tile
    exlist.add(3,tile0);

    FifteenGameEx.tiles = exlist;
  }

  // tests the makeBoard method which creates the titles when the game starts
  void testMakeBoard(Tester t) {
    initGameWon();
    FifteenGameEx = new FifteenGame();
    FifteenGameEx.tiles = exlist;

    int number = 1;
    for (Tile a: FifteenGameEx.tiles) {
      t.checkExpect(a.value, number);
      number = number + 1;
      if (number == 16) {
        number = 0;
      }
    }

    initGamePlay();
    t.checkExpect(FifteenGameEx.tiles.get(0).get(0), new Tile(1, 1, 0, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(0), new Tile(5, 1, 3, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(2).get(0), new Tile(9, 1, 2, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(3).get(0), new Tile(13, 1, 1, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(0).get(1), new Tile(2, 0, 0, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(1), new Tile(6, 0, 3, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(2).get(1), new Tile(10, 0, 2, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(3).get(1), new Tile(14, 0, 1, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(0).get(2), new Tile(3, 2, 0, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(2), new Tile(7, 2, 3, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(2).get(2), new Tile(11, 2, 2, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(3).get(2), new Tile(15, 2, 1, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(0).get(3), new Tile(4, 3, 0, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(3), new Tile(8, 3, 3, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(3), new Tile(8, 3, 3, false, false, Color.cyan));
    t.checkExpect(FifteenGameEx.tiles.get(3).get(3), new Tile(0, 3, 1, true, true, Color.orange));
  }

  // tests for the drawAt method that draws a tile
  void testDrawAt(Tester t) {
    initGameWon();
    t.checkExpect(FifteenGameEx.tiles.get(0).get(0).drawAt(0, 0, new EmptyImage()),
        new OverlayImage(new TextImage(String.valueOf(1), 18, Color.BLACK), 
            new OverlayImage(new RectangleImage(100 - 2, 100 - 2, OutlineMode.SOLID, Color.cyan), 
            new RectangleImage(100, 100, OutlineMode.SOLID, 
                Color.black))).movePinholeTo(new Posn(-100 / 2, -100 / 2)));
    t.checkExpect(FifteenGameEx.tiles.get(0).get(1).drawAt(0, 1, new EmptyImage()),
        new OverlayImage(new TextImage(String.valueOf(2), 18, Color.BLACK), 
            new OverlayImage(new RectangleImage(100 - 2, 100 - 2, OutlineMode.SOLID, Color.cyan), 
            new RectangleImage(100, 100, OutlineMode.SOLID, 
                Color.black))).movePinholeTo(new Posn(-100 / 2, -100 / 2)));
    t.checkExpect(FifteenGameEx.tiles.get(1).get(2).drawAt(0, 1, new EmptyImage()),
        new OverlayImage(new TextImage(String.valueOf(7), 18, Color.BLACK), 
            new OverlayImage(new RectangleImage(100 - 2, 100 - 2, OutlineMode.SOLID, Color.cyan), 
            new RectangleImage(100, 100, OutlineMode.SOLID, 
                Color.black))).movePinholeTo(new Posn(-100 / 2, -100 / 2)));
  }

  // tests for the onkeyevent method to check if the arrow and "u" keys are working correctly
  void testOnKeyEvent(Tester t) {
    initGameWon();
    t.checkExpect(FifteenGameEx.gameOver(), true);
    FifteenGameEx.onKeyEvent("up");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);
    FifteenGameEx.onKeyEvent("u");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);

    initGameWon();
    t.checkExpect(FifteenGameEx.gameOver(), true);
    FifteenGameEx.onKeyEvent("down");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);
    FifteenGameEx.onKeyEvent("u");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);

    initGameWon();
    t.checkExpect(FifteenGameEx.gameOver(), true);
    FifteenGameEx.onKeyEvent("right");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);
    FifteenGameEx.onKeyEvent("u");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);

    initGameWon();
    t.checkExpect(FifteenGameEx.gameOver(), true);
    FifteenGameEx.onKeyEvent("left");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);
    FifteenGameEx.onKeyEvent("u");
    t.checkExpect(FifteenGameEx.empty_t.solved, true);

    //      randomized

    //      t.checkExpect(FifteenGameEx.game_Over(), true);
    //      FifteenGameEx.onKeyEvent("up");
    //      t.checkExpect(FifteenGameEx.game_Over(), false);
    //      t.checkExpect(FifteenGameEx.findTile(FifteenGameEx.empty_x, 
    //                   FifteenGameEx.empty_y+1).color, Color.cyan);
    //      
    //      t.checkExpect(FifteenGameEx.game_Over(), false);
    //      FifteenGameEx.onKeyEvent("down");
    //      t.checkExpect(FifteenGameEx.game_Over(), true);
    //      t.checkExpect(FifteenGameEx.findTile(FifteenGameEx.empty_x, 
    //                   FifteenGameEx.empty_y-1).color, Color.orange);
    //      
    //      t.checkExpect(FifteenGameEx.game_Over(), true);
    //      FifteenGameEx.onKeyEvent("left");
    //      t.checkExpect(FifteenGameEx.game_Over(), false);
    //      t.checkExpect(FifteenGameEx.findTile(FifteenGameEx.empty_x+1, 
    //                   FifteenGameEx.empty_y).color, Color.cyan);
    //      
    //      FifteenGameEx.onKeyEvent("right");
    //      t.checkExpect(FifteenGameEx.findTile(FifteenGameEx.empty_x-1, 
    //                   FifteenGameEx.empty_y).color, Color.cyan);

  }

  // tests the makScene method that visualized the game screen
  void testMakeScene(Tester t) {

    // winning screen
    initGameWon();
    WorldScene scene = new WorldScene(600, 600);
    scene.placeImageXY(new TextImage("You Win!", Color.black), 250, 25);
    t.checkExpect(this.FifteenGameEx.makeScene(), scene);


    // game screen when you are still playing
    FifteenGame FifteenGameEx2 = new FifteenGame();
    tile1 = new Tile(1, 0, 0, false, false, Color.cyan);
    tile2 = new Tile(2, 1, 0, false, true, Color.cyan);
    tile3 = new Tile(3, 2, 0, false, false, Color.cyan);
    tile4 = new Tile(4, 3, 0, false, false, Color.cyan);
    tile5 = new Tile(5, 0, 1, false, false, Color.cyan);
    tile6 = new Tile(6, 1, 1, false, false, Color.cyan);
    tile7 = new Tile(7, 2, 1, false, false, Color.cyan);
    tile8 = new Tile(8, 3, 1, false, false, Color.cyan);
    tile9 = new Tile(9, 0, 2, false, false, Color.cyan);
    tile10 = new Tile(10, 1, 2, false, false, Color.cyan);
    tile11 = new Tile(11, 2, 2, false, false, Color.cyan);
    tile12 = new Tile(12, 3, 2, false, false, Color.cyan);
    tile13 = new Tile(13, 0, 3, false, false, Color.cyan);
    tile14 = new Tile(14, 1, 3, false, false, Color.cyan);
    tile15 = new Tile(0, 2, 3, false, false, Color.orange);
    tile0 = new Tile(15, 3, 3, true, true, Color.cyan);


    exlist = new ListOfLists<Tile>();

    //add 3 lists
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();
    exlist.addNewList();

    // add first row of tiles
    exlist.add(0,tile1);
    exlist.add(0,tile2);
    exlist.add(0,tile3);
    exlist.add(0,tile4);
    // add second row of tiles
    exlist.add(1,tile5);
    exlist.add(1,tile6);
    exlist.add(1,tile7);
    exlist.add(1,tile8);
    // add third row of tiles
    exlist.add(2,tile9);
    exlist.add(2,tile10);
    exlist.add(2,tile11);
    exlist.add(2,tile12);
    // add fourth row of tiles
    exlist.add(3,tile13);
    exlist.add(3,tile14);
    exlist.add(3,tile15);      
    //add empty tile
    exlist.add(3,tile0);


    FifteenGameEx2.tiles = exlist;
    scene = new WorldScene(600, 600);
    FifteenGameEx2.gameOver();
    for (Tile a: FifteenGameEx2.tiles) {
      scene.placeImageXY(a.drawAt(0, 0, new EmptyImage()), a.x * 100  + 50, a.y * 100 + 50);
    }
    scene.placeImageXY(new TextImage("Fifteen Game", 20 ,Color.black), 250, 25);
    scene.placeImageXY(new TextImage("Moves: " + String.valueOf(0), 20 ,Color.black), 250, 475);
    t.checkExpect(FifteenGameEx2.makeScene(), scene);
  }
  
  // tests for the solve methods with  correct order of tiles
  boolean testSolveWin(Tester t) {
    initGameWon();
    return t.checkExpect(tile1.solve(0, 0), true)
        && t.checkExpect(tile1.color, Color.orange)
        && t.checkExpect(tile10.solve(1, 2), true)
        && t.checkExpect(tile10.color, Color.orange)
        && t.checkExpect(tile3.solve(1, 1), false)
        && t.checkExpect(tile10.color, Color.orange);
  }
  
  // tests for the solve methods with incorrect order of tiles
  boolean testSolvePlay(Tester t) {
    initGamePlay();
    return t.checkExpect(tile1.solve(1, 0), false)
        && t.checkExpect(tile1.color, Color.cyan);
  }

  // tests for the solve methods with incorrect and correct order of tiles
  void testGameOverWin(Tester t) {
    initGameWon();
    t.checkExpect(FifteenGameEx.gameOver(), true);
    initGamePlay();
    t.checkExpect(FifteenGameEx.gameOver(), false);
  }

  // test the method findTile
  void testFindTile(Tester t) { 
    initGameWon();
    t.checkExpect(FifteenGameEx.findTile(0, 0), tile1);
    t.checkExpect(FifteenGameEx.findTile(3, 0), tile4);
    t.checkExpect(FifteenGameEx.findTile(3, 2), tile12);  
  }

  // test the method Swap that swaps a tile with an empty tile
  void testSwap(Tester t) { 
    initGameWon();
    FifteenGameEx.swap(tile15, tile0);
    t.checkExpect(tile15.solved, false);
    t.checkExpect(tile15.x, 3);
    t.checkExpect(tile15.y, 3); 
    t.checkExpect(tile0.x, 2);
    t.checkExpect(tile0.y, 3); 

    FifteenGameEx.swap(tile11, tile0);
    t.checkExpect(tile11.solved, false);
    t.checkExpect(tile11.x, 2);
    t.checkExpect(tile11.y, 3); 
    t.checkExpect(tile0.x, 2);
    t.checkExpect(tile0.y, 2); 
  }

  // testing the iterator for the arraylist of arraylists
  void testListOfLists(Tester t) {

    initGameWon();
    FifteenGameEx = new FifteenGame();
    FifteenGameEx.tiles = exlist;

    int number = 1;
    for (Tile a: FifteenGameEx.tiles) {
      t.checkExpect(a.value, number);
      number = number + 1;
      if (number == 16) {
        number = 0;
      }
    }

    ListOfLists<Integer> lol = new ListOfLists<Integer>();
    //add 3 lists
    lol.addNewList();
    lol.addNewList();
    lol.addNewList();
    //add elements 1,2,3 in first list
    lol.add(0,1);
    lol.add(0,2);
    lol.add(0,3);
    //add elements 4,5,6 in second list
    lol.add(1,4);
    lol.add(1,5);
    lol.add(1,6);
    //add elements 7,8,9 in third list
    lol.add(2,7);
    lol.add(2,8);
    lol.add(2,9);
    //iterator should return elements in order 1,2,3,4,5,6,7,8,9
    number = 1;
    for (Integer num:lol) {
      t.checkExpect(num,number);
      number = number + 1;
    }
  }
}