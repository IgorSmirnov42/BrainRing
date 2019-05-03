package ru.spbhse.brainring.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;

public class Judging extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judging);

        Button acceptButton = findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.LocalAdminLogicController.onAcceptAnswer();
                Judging.this.finish();
            }
        });

        Button rejectButton = findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.LocalAdminLogicController.onRejectAnswer();
                Judging.this.finish();
            }
        });
    }
}
