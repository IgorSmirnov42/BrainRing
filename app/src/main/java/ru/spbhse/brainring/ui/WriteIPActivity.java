package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.LocalGameRoles;

public class WriteIPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_ip);

        Button readyButton = findViewById(R.id.readyButton);
        readyButton.setOnClickListener(v -> {
            EditText server = findViewById(R.id.serverIP);
            String serverIp = server.getText().toString();

            Intent intent = new Intent(WriteIPActivity.this, PlayerActivity.class);
            intent.putExtra("serverIp", serverIp);

            LocalGameRoles colorName = (LocalGameRoles) getIntent().getSerializableExtra("color");
            intent.putExtra("color", colorName);
            startActivity(intent);
        });
    }
}
