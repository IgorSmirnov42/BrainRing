package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;

public class PlayerActivity extends AppCompatActivity {

    // TODO: возможно добавить счет

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        String color = getIntent().getStringExtra("color");
        Controller.setUI(this);


    }
}
