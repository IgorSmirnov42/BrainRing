package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import ru.spbhse.brainring.R;

public class InfoActivity extends AppCompatActivity {

    private final static String LICENSE = "Все вопросы взяты из Базы Вопросов Интернет-клуба \"Что? Где? Когда?\"" +
            "(http://db.chgk.info)\n" +
            "Лицензия на использование вопросов из Базы Вопросов Интернет-клуба \"Что? Где? Когда?\"\n" +
            "07.10.2010 12:23\n" +
            "Интернет-клуб \"Что? Где? Когда?\" предоставляет пользователям Базы Вопросов право:\n" +
            "* использовать эти вопросы (1) для тренировок и проведения турниров " +
            "местного характера при условии, что указывается с полным URL (2) их " +
            "источник - База Воп    росов Интернет-клуба \"Что? Где? Когда?\";\n" +
            "* публиковать эти вопросы в традиционных и электронных средствах " +
            "массовой информации в некоммерческих целях при условии, что указывается с " +
            "полным URL их источник - База Вопросов Интернет-клуба \"Что? Где? Когда?\";\n" +
            "* распространять любой пакет вопросов из Базы Вопросов, в том числе и " +
            "случайно сгенерированный (3), по желанию пользователя при условии, что в " +
            "него не вносятся посторонние вопросы, указывается с полным URL их " +
            "источник - База Вопросов Интернет-клуба \"Что? Где? Когда?\" и прилагается " +
            "текст данной Лицензии.\n\n" +
            "Предупреждение:\n" +
            "подавляющее большинство вопросов Базы Вопросов были играны " +
            "или другим образом распространялись по многим городам как в СССР/СНГ, так и " +
            "за его пределами. Подавляющее большинство вопросов Базы Вопросов собрано из " +
            "общедоступных источников, не предохраненных авторским правом, или же с " +
            "разрешения держателей авторского права на распространение вопросов в сети " +
            "Интернет. В связи с этим Интернет-клуб \"Что? Где? Когда?\" не несет никакой " +
            "ответственности за:\n" +
            "\n" +
            "* использование вопросов Базы Вопросов в турнирах;\n" +
            "* корректность вопросов в Базе Вопросов;\n" +
            "* проблемы с авторскими правами, возникающие между пользователями Базы " +
            "Вопросов и авторами вопросов.\n\n" +
            "В соответствии с настоящей Лицензией НЕ ДОПУСКАЕТСЯ:\n" +
            "\n" +
            "* внесение любых изменений в содержание этих вопросов;\n" +
            "* использование этих вопросов для индивидуальных или командных взносов " +
            "пользователей в оргкомитеты турниров;\n" +
            "* любое использование этих вопросов без указания с полным URL их источника - " +
            "Базы Вопросов Интернет-клуба \"Что? Где? Когда?\";\n" +
            "* распространение любых пакетов из Базы Вопросов, в том числе и случайно " +
            "сгенерированных, без приложения текста данной Лицензии;\n" +
            "* любое использование этих вопросов с коммерческой целью;\n" +
            "* использование материалов этих вопросов для написания любых текстов с " +
            "коммерческой целью.\n" +
            "\n" +
            "Примечания\n" +
            "\n" +
            "1. Под словом \"Вопрос\" в данной Лицензии подразумевается полный текст " +
            "полей \"Вопрос\", \"Ответ\", \"Источник\" и \"Автор\" в Базе Вопросов.\n" +
            "2. http://db.chgk.info\n" +
            "3. Распространение вопросов из тренировочного пакета клуба \"Мозговорот\" " +
            "допускается только на основе Лицензии, прилагаемой к означенному пакету.";
    private final static String NET = "Два игрока с помощью автоматчинга объединяются в комнату. Если соперник не находится в течение долгого времени, игра переходит на предыдущий экран. После подключения игроки начинается игра из пяти вопросов. Если после пяти вопросов не выявлен победитель, вопросы задаются, пока победитель не будет выявлен. На прочтение вопроса даётся 10 секунд, затем 20 секунд на раздумье и ещё 20 в случае неправильного ответа одной из команд. Время на написание ответа 20 секунд.";
    private final static String LOCAL = "Этот режим предназначен для проведения реальных игр. В этом режиме вопросы читаются ведущим и не присутствуют в игре. Для игры нужно 3 телефона: телефон ведущего и два игровых. В начале игры устройства с помощью автоматчинга помещаются в комнату, где они распределены по ролям (красный стол, зелёный стол, ведущий). Роли указываются при создании игры. Это не сделано с помощью приглашений, так как предполагается, что этой функцией будут пользоваться довольно редко, поэтому вероятность того, что две игры в разных местах будут создаваться одновременно крайне мала, а так пользователям нужно нажать всего одну кнопку. В начале игры ведущий может указать первое время на раздумье и время на раздумье в случае неправильного ответа одной из команд. Телефоны команд выполняют роль кнопок. С телефона ведущего запускается время на чтение вопроса, запускается обратный отсчёт, останавливается время при необходимости (например, если ведущий случайно нажал неверную кнопку). На своём же телефоне ведущий принимает или не принимает ответы, данные командами. В случае какой-либо ошибки у ведущего есть возможность отредактировать счёт.";
    private final static String TRAINING = "В этом режиме пользователь играет вопросы без соперника. Время на раздумье регулируется при старте игры. Также при старте можно загрузить какой-то определённый пакет из базы вопросов db.chgk.info, указав на него ссылку в соответсвующем поле.";
    private View pressedView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        CardView cardLicense = findViewById(R.id.cardViewLicense);
        TextView infoText = findViewById(R.id.textInfo);
        infoText.setMovementMethod(new ScrollingMovementMethod());

        cardLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View licenseInfo = findViewById(R.id.viewLicenseInfo);
                highlight(licenseInfo);
                infoText.setText(LICENSE);
                infoText.scrollTo(0, 0);
            }
        });

        CardView cardLocal = findViewById(R.id.cardViewLocal);
        cardLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View localInfo = findViewById(R.id.viewLocalInfo);
                highlight(localInfo);
                infoText.setText(LOCAL);
                infoText.scrollTo(0, 0);
            }
        });

        CardView cardTraining = findViewById(R.id.cardViewTraining);
        cardTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View trainingInfo = findViewById(R.id.viewTrainingInfo);
                highlight(trainingInfo);
                infoText.setText(TRAINING);
                infoText.scrollTo(0, 0);
            }
        });

        CardView cardNet = findViewById(R.id.cardViewNet);
        cardNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View netInfo = findViewById(R.id.viewNetInfo);
                highlight(netInfo);
                infoText.setText(NET);
                infoText.scrollTo(0, 0);
            }
        });

        cardLocal.performClick();
    }

    private void setHeight(View view, double height) {
        if (view == null) {
            return;
        }
        view.getLayoutParams().height = (int)height;
        view.setLayoutParams(view.getLayoutParams());
    }

    private void highlight(View view) {
        if (view == pressedView) {
            return;
        }
        setHeight(pressedView, pixelsFromDp(3));
        setHeight(view, pixelsFromDp(10));
        pressedView = view;
    }

    private double pixelsFromDp(double dpValue) {
        return dpValue * this.getResources().getDisplayMetrics().density;
    }
}