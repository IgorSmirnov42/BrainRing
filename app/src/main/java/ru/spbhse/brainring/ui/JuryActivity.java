package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;

public class JuryActivity extends AppCompatActivity {

    private TextView statusText;
    private Button mainButton;
    private TextView redTeamScore;
    private TextView greenTeamScore;
    private LocalGameLocation currentLocation = LocalGameLocation.GAME_WAITING_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jury);
        Controller.setUI(this);
        Controller.startLocalGameAsAdmin();
        statusText = findViewById(R.id.gameStatusInfo);
        mainButton = findViewById(R.id.mainButton);
        mainButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Controller.LocalAdminLogicController.toNextState();
                return true;
            }
        });
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(JuryActivity.this, "Надо нажимать дольше",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        greenTeamScore = findViewById(R.id.greenTeamScore);
        redTeamScore = findViewById(R.id.redTeamScore);

        redrawLocation();
    }

    public void redrawLocation() {
        greenTeamScore.setText(Controller.LocalAdminLogicController.getGreenScore());
        redTeamScore.setText(Controller.LocalAdminLogicController.getRedScore());
        if (currentLocation == LocalGameLocation.GAME_WAITING_START) {
            statusText.setText("Ожидаем подключения игроков");
            mainButton.setVisibility(View.GONE);
        }
        if (currentLocation == LocalGameLocation.NOT_STARTED) {
            statusText.setText("Оба игрока подключены. Можем начинать раунд.");
            mainButton.setText("Начать чтение вопроса");
            mainButton.setVisibility(View.VISIBLE);
        }
        if (currentLocation == LocalGameLocation.READING_QUESTION) {
            statusText.setText("Чтение вопроса. Как только вопрос будет дочитан, нужно нажать на кнопку");
            mainButton.setText("Запустить таймер");
            mainButton.setVisibility(View.VISIBLE);
        }
        if (currentLocation == LocalGameLocation.COUNTDOWN) {
            statusText.setText("Идёт обратный отсчёт (сюда надо запилить таймер)");
            mainButton.setText("Остановить таймер");
            mainButton.setVisibility(View.VISIBLE);
        }
        if (currentLocation == LocalGameLocation.ONE_IS_ANSWERING) {
            statusText.setText("Что-то пошло не так. Вы не должны видеть это меню.");
            mainButton.setText("Остановить таймер");
            mainButton.setVisibility(View.GONE);
        }
    }
}
