package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        boolean hasWon = getIntent().getBooleanExtra("HAS_WON", false);
        int elapsedTime = getIntent().getIntExtra("ELAPSED_TIME", 0);
        TextView gameOverText = findViewById(R.id.gameOverText);

        if(hasWon)
            gameOverText.setText("Used " + elapsedTime + " second(s).\nYou won.\nGood job!");

        else
            gameOverText.setText("Used " + elapsedTime + " second(s).\nYou lost.\nBetter luck next time!");

        Button playAgainButton = findViewById(R.id.playAgainButton);

        playAgainButton.setOnClickListener(view -> {
            // restart main activity; clear/reset everything
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
