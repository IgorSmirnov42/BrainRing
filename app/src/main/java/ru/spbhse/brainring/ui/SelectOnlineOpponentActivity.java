package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import ru.spbhse.brainring.R;

/** This activity is for selecting opponent in online game */
public class SelectOnlineOpponentActivity extends AppCompatActivity {
    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_online_opponent);

        Button searchOpponentButton = findViewById(R.id.searchOpponentButton);
        searchOpponentButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectOnlineOpponentActivity.this,
                    OnlineGameActivity.class);
            startActivity(intent);
        });
    }
}
