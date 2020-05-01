package com.kodilla;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;


public class Game extends Application {

    private static final int TILE_SIZE = 40;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int NO_OF_TILES_IN_A_ROW = WIDTH / TILE_SIZE;
    private static final int NO_OF_TILES_IN_A_COLUMN = HEIGHT / TILE_SIZE;

    private Tile[][] grid = new Tile[NO_OF_TILES_IN_A_ROW][NO_OF_TILES_IN_A_COLUMN];
    private Scene scene;
    private int countFlags;
    private int countBombs = 0;
    private int numbersOfBombsFlagged = 0;

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);

        //fill the board
        for (int y = 0; y < NO_OF_TILES_IN_A_COLUMN; y++) {
            for (int x = 0; x < NO_OF_TILES_IN_A_ROW; x++) {
                Tile tile = new Tile(x, y, Math.random() < 0.1);
                if (tile.hasBomb) countBombs++;

                grid[x][y] = tile;
                root.getChildren().add(tile);
            }
        }
        countFlags = countBombs;
        // set bombs
        for (int y = 0; y < NO_OF_TILES_IN_A_COLUMN; y++) {
            for (int x = 0; x < NO_OF_TILES_IN_A_ROW; x++) {
                Tile tile = grid[x][y];

                if (tile.hasBomb)
                    continue;

                long bombs = listOfNeighbourTiles(tile).stream().filter(t -> t.hasBomb).count();

                if (bombs > 0)
                    tile.text.setText(String.valueOf(bombs));
            }
        }
        return root;
    }

    private List<Tile> listOfNeighbourTiles(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        List<Coords> coords = allPossibleCoords();

        for (Coords coord : coords) {
            addNeighbourIfExist(tile, neighbors, coord);
        }
        return neighbors;
    }

    private void addNeighbourIfExist(Tile tile, List<Tile> neighbors, Coords possibleCoord) {
        int newX = tile.x + possibleCoord.getX();
        int newY = tile.y + possibleCoord.getY();

        if (newX >= 0 && newX < NO_OF_TILES_IN_A_ROW && newY >= 0 && newY < NO_OF_TILES_IN_A_COLUMN)
            neighbors.add(grid[newX][newY]);
    }

    private List<Coords> allPossibleCoords() {
        return Arrays.asList(
                new Coords(-1, -1), new Coords(-1, 0), new Coords(-1, 1), new Coords(0, 1),
                new Coords(0, -1), new Coords(1, -1), new Coords(1, 0), new Coords(1, 1)
        );
    }

    private class Tile extends StackPane {
        private int x, y;
        private boolean hasBomb;
        private boolean isOpened = false;
        private boolean isFlagged = false;

        private Rectangle border = new Rectangle(TILE_SIZE - 2, TILE_SIZE - 2);
        private Text text = new Text();

        public Tile(int x, int y, boolean hasBomb) {
            this.x = x;
            this.y = y;
            this.hasBomb = hasBomb;

            border.setStroke(Color.LIGHTGRAY);

            text.setFont(Font.font(18));
            text.setText(hasBomb ? "X" : "");
            text.setVisible(false);

            getChildren().addAll(border, text);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);

            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    flagTile();
                }
                if (event.getButton() == MouseButton.PRIMARY) {
                    openTile();
                }
            });

        }

        public void openTile() {
            if (isOpened)
                return;

            if (hasBomb) {
                Alert gameOver = new Alert(WARNING);
                gameOver.setTitle("Game Over!");
                gameOver.setHeaderText("Bomb Exploded!");
                gameOver.setContentText(
                        "Oh no! You clicked on a bomb and caused all the bombs to explode! Better luck next time.");
                gameOver.showAndWait();
                scene.setRoot(createContent());
                return;
            }

            isOpened = true;
            text.setVisible(true);
            border.setFill(null);

            if (text.getText().isEmpty()) {
                listOfNeighbourTiles(this).forEach(Tile::openTile);
            }
        }

        private void flagTile() {
                if (isOpened)
                    return;
                if (countFlags == 0 && isFlagged)
                    return;
                if (!isFlagged) {
                    isFlagged = true;
                    countFlags--;
                    border.setFill(Color.RED);
                    text.setVisible(false);
                    if (hasBomb) numbersOfBombsFlagged++;
                } else {
                    isFlagged = false;
                    countFlags++;
                    border.setFill(Color.BLACK);
                }
                if (countBombs == numbersOfBombsFlagged) {
                    Alert win = new Alert(INFORMATION);
                    win.setTitle("You won!");
                    win.setHeaderText("Congratulations!");
                    win.setContentText(
                            "Found all the bombs, haven't ya?");
                    win.showAndWait();
                    scene.setRoot(createContent());
                    return;
                }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        scene = new Scene(createContent());

        primaryStage.setTitle("MINESWEEPER");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
