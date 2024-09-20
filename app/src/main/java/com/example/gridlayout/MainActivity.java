package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int ROWS = 12;
    private static final int COLS = 10;
    private static final int TOTAL_MINES = 20;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private boolean[][] mineGrid = new boolean[ROWS][COLS];
    private boolean[][] revealedGrid = new boolean[ROWS][COLS];  // track revealed cells
    private boolean[][] flaggedGrid = new boolean[ROWS][COLS];  // track flagged cells
    private boolean playerMode = true;  // true for pickaxe, false for flag
    private boolean firstClick = true;  // track if first click has been made
    private boolean gameOver = false;  // track if the game is over

    private TextView modeSwitch;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i < ROWS; i ++) {
            for (int j = 0; j < COLS; j ++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(32));
                tv.setWidth(dpToPixel(32));
                tv.setTextSize(16);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }

        // pickaxe and flag switch setup
        modeSwitch = findViewById(R.id.modeSwitch);
        modeSwitch.setText(getString(R.string.pick));
        modeSwitch.setTextSize(32);
        modeSwitch.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        modeSwitch.setOnClickListener(v -> switchMode());
    }

    // change player mode
    private void switchMode() {
        playerMode = !playerMode;

        if(playerMode)
            modeSwitch.setText(getString(R.string.pick));

        else
            modeSwitch.setText(getString(R.string.flag));
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        // if game is over, show game over screen
        if(gameOver) {
            gameOverScreen();
            return;
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n / COLS;
        int j = n % COLS;

        // if it's the player's first move, initialize all mines
        if(firstClick) {
            placeMines(i, j);
            firstClick = false;
        }

        // if pickaxe, reveal cell
        if(playerMode) {
            if(!revealedGrid[i][j] && !flaggedGrid[i][j]) {
                revealCell(i, j);

                if(mineGrid[i][j])
                    gameOver = true;
            }
        }

        // else flag cell
        else {
            // only flag if cell isn't revealed yet
            if(!revealedGrid[i][j]) {
                // if cell is already flagged, unflag it
                if(flaggedGrid[i][j]) {
                    flaggedGrid[i][j] = false;
                    tv.setText("");
                }

                // else flag the cell
                else {
                    flaggedGrid[i][j] = true;
                    tv.setText(getString(R.string.flag));
                }
            }
        }
    }

    // reveal cell and appropriate surrounding cells
    private void revealCell(int row, int col) {
        // do nothing if cell is already revealed
        if(revealedGrid[row][col])
            return;

        revealedGrid[row][col] = true;
        TextView tv = cell_tvs.get(row * COLS + col);

        // if cell is a mine, reveal it
        if(mineGrid[row][col]) {
            tv.setText(getString(R.string.mine));
            tv.setBackgroundColor(Color.RED);
        }

        // else reveal cell normally
        else {
            int surroundingMines = countSurroundingMines(row, col);
            tv.setText(String.valueOf(surroundingMines));
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);

            // if there are 0 surrounding mines, reveal surrounding cells as well
            if(surroundingMines == 0) {
                tv.setText("");
                revealAdjacentCells(row, col);
            }
        }
    }

    // helper func. to reveal surrounding cells
    private void revealAdjacentCells(int row, int col) {
        for(int i = row - 1; i <= row + 1; i ++) {
            for(int j = col - 1; j <= col + 1; j ++) {
                if(i >= 0 && i < ROWS && j >= 0 && j < COLS && !(i == row && j == col)) {
                    if(!revealedGrid[i][j])
                        revealCell(i, j);  // recursive call
                }
            }
        }
    }

    // randomly place mines at start, avoid cell that user clicked
    private void placeMines(int excludeRow, int excludeCol) {
        Random random = new Random();
        int count = 0;

        while(count < TOTAL_MINES) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);
            boolean inFirstClickArea = Math.abs(row - excludeRow) <= 1 && Math.abs(col - excludeCol) <= 1;

            if(!mineGrid[row][col] && !inFirstClickArea) {
                mineGrid[row][col] = true;
                count ++;
            }
        }
    }

    // detect surrounding mines around a cell
    private int countSurroundingMines(int row, int col) {
        int count = 0;

        for(int i = row - 1; i <= row + 1; i ++) {
            for(int j = col - 1; j <= col + 1; j ++) {
                if(i >= 0 && i < ROWS && j >= 0 && j < COLS) {
                    if(mineGrid[i][j])
                        count ++;
                }
            }
        }

        return count;
    }

    // show game over screen
    private void gameOverScreen() {
        Intent intent = new Intent(this, GameOverActivity.class);
        startActivity(intent);
    }
}