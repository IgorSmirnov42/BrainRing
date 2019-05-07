package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;

public class JuryActivity extends AppCompatActivity {

    private TextView statusText;
    private Button mainButton;
    private TextView redTeamScore;
    private TextView greenTeamScore;
    private LocalGameLocation currentLocation = LocalGameLocation.GAME_WAITING_START;
    private static final int RC_SIGN_IN = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jury);
        Controller.setUI(this);
        Controller.initializeLocalGame();
        statusText = findViewById(R.id.gameStatusInfo);
        mainButton = findViewById(R.id.mainButton);
        mainButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!Controller.LocalAdminLogicController.toNextState()) {
                    Toast.makeText(JuryActivity.this, "Невозможно в данный момент переключиться.",
                            Toast.LENGTH_LONG).show();
                }
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

        Controller.LocalNetworkAdminController.createLocalGame();
    }

    public void setLocation(LocalGameLocation location) {
        currentLocation = location;
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
            //mainButton.setText("Остановить таймер");
            mainButton.setVisibility(View.GONE);
        }
    }

    public void onReceivingAnswer(String color) {
        Intent intent = new Intent(JuryActivity.this, Judging.class);
        intent.putExtra("color", color);
        startActivity(intent);
    }

    public void showTime(long time) {
        statusText.setText(String.valueOf(time));
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    public void signIn() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Controller.LocalNetworkController.loggedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "Неизвестная ошибка. Убедитесь, что у Вас установлены Google Play игры и выполнен вход в аккаунт.";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("BrainRing", "Destroying activity. Leaving room");
        super.onDestroy();
        Controller.LocalNetworkController.leaveRoom();
    }
}
