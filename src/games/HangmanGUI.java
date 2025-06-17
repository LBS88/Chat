package games;

import javax.swing.*;
import java.awt.*;

public class HangmanGUI extends JFrame {

    private final HangmanGame game;
    private final JLabel wordLabel;
    private final JTextField inputField;
    private final JLabel attemptsLabel;
    private final JLabel wrongGuessesLabel;

    public HangmanGUI(){
        game = new HangmanGame("bite");
        setTitle("LE JEU DU PENDU");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // allow closing without affecting main window
        setLayout(new BorderLayout());

        // Word display
        wordLabel = new JLabel(game.getMaskedWord(), SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        add(wordLabel, BorderLayout.NORTH);

        // Input
        JPanel centerPanel = new JPanel();
        inputField = new JTextField(1);
        JButton guessButton = new JButton("Guess");
        centerPanel.add(new JLabel("Letter:"));
        centerPanel.add(inputField);
        centerPanel.add(guessButton);
        add(centerPanel, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        attemptsLabel = new JLabel("Attempts left: " + game.getRemainingGuesses(), SwingConstants.CENTER);
        wrongGuessesLabel = new JLabel("Wrong guesses: " + game.getIncorrectGuesses(), SwingConstants.CENTER);
        infoPanel.add(attemptsLabel);
        infoPanel.add(wrongGuessesLabel);
        add(infoPanel, BorderLayout.SOUTH);

        guessButton.addActionListener(e -> handleGuess());
        inputField.addActionListener(e -> handleGuess());

        setVisible(true);

    }

    private void handleGuess(){
        String text = inputField.getText();
        if (text.length() != 1 || !Character.isLetter(text.charAt(0))) {
            JOptionPane.showMessageDialog(this, "Enter a valid single letter.");
            return;
        }

        char guess = Character.toLowerCase(text.charAt(0));
        inputField.setText("");

        game.guessLetter(guess);
        wordLabel.setText(game.getMaskedWord());
        attemptsLabel.setText("Attempts left: " + game.getRemainingGuesses());
        wrongGuessesLabel.setText("Wrong guesses: " + game.getIncorrectGuesses());

        if (game.isWin()){
            JOptionPane.showMessageDialog(this, "You won! The word was: " + game.getWordToGuess());
            dispose();
        } else if (game.getRemainingGuesses() <= 0){
            JOptionPane.showMessageDialog(this, "You lost! The word was: " + game.getWordToGuess());
            dispose();
        }
    }
}
