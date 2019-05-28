package ru.spbhse.brainring.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestionTest {
    private int id = 1;

    @Test
    public void questionConstructorTest() {
        Question question = new Question("aa", "bb", "00", "gg", id);
        assertEquals("aa", question.getQuestion());
        assertEquals("gg", question.getComment());

        question = new Question("aa", "bb", null, null, id);
        assertEquals("aa", question.getQuestion());
        assertEquals("", question.getComment());

        new Question("aa", "bb", "aa/bb/dd", null, id);
    }

    @Test
    public void checkAnswerIncorrectAnswer() {
        Question question = new Question("aa", "bb", "00/cc", "gg", id);
        assertFalse(question.checkAnswer("Ab"));
        assertFalse(question.checkAnswer("dd"));
    }

    @Test
    public void mainAnswerTest() {
        Question question = new Question("aa", "bb", "00/cc", "gg", id);
        assertEquals("bb", question.getMainAnswer());
    }

    @Test
    public void checkTotallyCorrectAnswer() {
        Question question = new Question("aa", "bb", "00/cc/АВа", "gg", id);
        assertTrue(question.checkAnswer("bb"));
        assertTrue(question.checkAnswer("00"));
        assertTrue(question.checkAnswer("cc"));
        assertTrue(question.checkAnswer("Ава"));
    }

    @Test
    public void checkAnswersThatCloseToCorrect() {
        Question question = new Question("aa", "bb", "00/cc/АВа/абракадабра", "gg", id);
        assertFalse(question.checkAnswer("ba"));
        assertTrue(question.checkAnswer("авракадабра"));
        assertTrue(question.checkAnswer("авакадабра"));
        assertFalse(question.checkAnswer("авадакедавра"));
        assertTrue(question.checkAnswer("АБРАКАДАБРА"));
    }

    @Test
    public void checkPointsCommasSpacesHaveNoEffect() {
        Question question = new Question("aa", "о, к, р.", null, null, id);
        assertTrue(question.checkAnswer("окр"));
        assertTrue(question.checkAnswer("о к р"));
        assertTrue(question.checkAnswer("о, к, р"));
        assertFalse(question.checkAnswer("о, к, и."));
    }

    @Test
    public void checkSquareBrackets() {
        Question question = new Question("", "[Внебрачным] сыном Петра I", null, null, id);
        assertTrue(question.checkAnswer("Внебрачным сыном петра"));
        assertTrue(question.checkAnswer("сыном петра"));
        assertTrue(question.checkAnswer("внибрачным сынам питра"));
        assertTrue(question.checkAnswer("сыном питра"));
        assertFalse(question.checkAnswer("вне сыном петра I"));
        assertFalse(question.checkAnswer("внебрачным"));
        assertFalse(question.checkAnswer("внебрачным сыном"));
    }
}
