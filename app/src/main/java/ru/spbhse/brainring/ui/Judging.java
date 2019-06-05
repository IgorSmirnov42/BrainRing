package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.LocalController;

public class Judging extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judging);

        String color = getIntent().getStringExtra("color");
        TextView answering = findViewById(R.id.answeringId);
        answering.setText(color);
        //TODO: цвет текста показывает игрока (чтобы вызывать getColor(), нужен API 23)

        Button acceptButton = findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(v -> {
            LocalController.LocalAdminLogicController.onAcceptAnswer();
            Judging.this.finish();
        });

        Button rejectButton = findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(v -> {
            LocalController.LocalAdminLogicController.onRejectAnswer();
            Judging.this.finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Disabled
    }
}
