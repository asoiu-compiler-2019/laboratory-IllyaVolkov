import java.util.ArrayList;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;


enum BlockState {
    NoNextState(0),
    Initial(1),
    Letter(2);

    private final int number;

    BlockState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}


enum FunctionState {
    NoNextState(0),
    Initial(1),
    Letter(2),
    LeftParenthesis(3);

    private final int number;

    FunctionState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}


enum ParameterState {
    NoNextState(0),
    Initial(1),
    Letter(2),
    Assign(3);

    private final int number;

    ParameterState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}


enum ParameterValueState {
    NoNextState(0),
    Initial(1),
    Letter(2),
    Quote(3);

    private final int number;

    ParameterValueState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}


public class Lexer {
    private String input;
    private int position = 0;
    private int line = 0;
    private int column = 0;

    public Lexer(String input) {
        this.input = input;
    }

    public ArrayList<Token> allTokens() {
        Token token = this.nextToken();
        ArrayList<Token> tokens = new ArrayList<>();

        while (token.type != TokenType.EndOfInput) {
            tokens.add(token);
            token = this.nextToken();
        }

        return tokens;
    }

    public Token nextToken() {
        if (this.position >= this.input.length()) {
            return new Token(TokenType.EndOfInput);
        }

        this.skipWhitespacesAndNewLines();

        char character = this.input.charAt(this.position);
        Token t = null;

        if (CharUtils.isLetter(character)) {
            t = this.recognizeFunction();
            t = t == null ? this.recognizeParameter() : t;
        } else if (character == '"') {
            t = this.recognizeParameterValue();
        } else if (character == '!') {
            t = this.recognizeBlock();
        } else if (CharUtils.isPunctuation(character)) {
            t = this.recognizePunctuation();
        } else if (CharUtils.isParenthesis(character)) {
            t = this.recognizeParenthesis();
        }

        if (t != null) {
            return t;
        }

        throw new Error(MessageFormat.format("Unrecognized character {0} at line {1} and column {2}.", character, this.line, this.column));
    }

    private void skipWhitespacesAndNewLines() {
        while (this.position < this.input.length() && CharUtils.isWhitespaceOrNewLine(this.input.charAt(this.position))) {
            this.position += 1;

            if (CharUtils.isNewLine(this.input.charAt(this.position))) {
                this.line += 1;
                this.column = 0;
            } else {
                this.column += 1;
            }
        }
    }

    private Token recognizeParenthesis() {
        int position = this.position;
        int line = this.line;
        int column = this.column;
        char character = this.input.charAt(position);

        this.position += 1;
        this.column += 1;

        if (character == '(') {
            return new Token(TokenType.LeftParenthesis, "(", line, column);
        }

        return new Token(TokenType.RightParenthesis, ")", line, column);
    }

    private Token recognizePunctuation() {
        int position = this.position;
        int line = this.line;
        int column = this.column;
        char character = this.input.charAt(position);

        this.position += 1;
        this.column += 1;

        switch (character) {
            case ',': return new Token(TokenType.Comma, ",", line, column);
            case ':': return new Token(TokenType.Colon, ":", line, column);
            case ';': return new Token(TokenType.Semicolon, ";", line, column);
            case '=': return new Token(TokenType.Assign, "=", line, column);
            case '.': return new Token(TokenType.Dot, ".", line, column);
        }

        return null;
    }

    private Token recognizeFunction() {
        int line = this.line;
        int column = this.column;

        Set states = new HashSet<>(Arrays.asList(FunctionState.Initial.getNumber(), FunctionState.Letter.getNumber(), FunctionState.LeftParenthesis.getNumber(), FunctionState.NoNextState.getNumber()));
        Set acceptingStates = new HashSet<>(Arrays.asList(FunctionState.LeftParenthesis.getNumber()));
        int initialState = FunctionState.Initial.getNumber();
        BiFunction<Integer, Character, Integer> nextState = (currentState, character) -> {
            switch (FunctionState.values()[currentState]) {
                case Initial:
                case Letter:
                    if (CharUtils.isLetter(character)) {
                        return FunctionState.Letter.getNumber();
                    }
                    if (character == '(') {
                        return FunctionState.LeftParenthesis.getNumber();
                    }
                    break;

                case LeftParenthesis:
                    return FunctionState.NoNextState.getNumber();
            }

            return FunctionState.NoNextState.getNumber();
        };

        FSM fsm = new FSM(states, acceptingStates, initialState, nextState);
        String fsmInput = this.input.substring(this.position);
        String value = fsm.run(fsmInput);


        if (value != null) {
            value = value.substring(0, value.length() - 1);
            this.position += value.length();
            this.column += value.length();

            return new Token(TokenType.FunctionName, value, line, column);
        }

        return null;
    }

    private Token recognizeParameter() {
        int line = this.line;
        int column = this.column;

        Set states = new HashSet<>(Arrays.asList(ParameterState.Initial.getNumber(), ParameterState.Letter.getNumber(), ParameterState.Assign.getNumber(), ParameterState.NoNextState.getNumber()));
        Set acceptingStates = new HashSet<>(Arrays.asList(ParameterState.Assign.getNumber()));
        int initialState = ParameterState.Initial.getNumber();
        BiFunction<Integer, Character, Integer> nextState = (currentState, character) -> {
            switch (ParameterState.values()[currentState]) {
                case Initial:
                case Letter:
                    if (CharUtils.isLetter(character) || character == '_') {
                        return ParameterState.Letter.getNumber();
                    }
                    if (character == '=') {
                        return ParameterState.Assign.getNumber();
                    }
                    break;

                case Assign:
                    return ParameterState.NoNextState.getNumber();
            }

            return ParameterState.NoNextState.getNumber();
        };

        FSM fsm = new FSM(states, acceptingStates, initialState, nextState);
        String fsmInput = this.input.substring(this.position);
        String value = fsm.run(fsmInput);


        if (value != null) {
            value = value.substring(0, value.length() - 1);
            this.position += value.length();
            this.column += value.length();

            return new Token(TokenType.Parameter, value, line, column);
        }

        return null;
    }

    private Token recognizeParameterValue() {
        int line = this.line;
        int column = this.column;

        Set states = new HashSet<>(Arrays.asList(ParameterValueState.Initial.getNumber(), ParameterValueState.Letter.getNumber(), ParameterValueState.Quote.getNumber(), ParameterValueState.NoNextState.getNumber()));
        Set acceptingStates = new HashSet<>(Arrays.asList(ParameterValueState.Quote.getNumber()));
        int initialState = ParameterState.Initial.getNumber();
        BiFunction<Integer, Character, Integer> nextState = (currentState, character) -> {
            switch (ParameterValueState.values()[currentState]) {
                case Initial:
                case Letter:
                    if (character != '"') {
                        return ParameterValueState.Letter.getNumber();
                    } else {
                        return ParameterValueState.Quote.getNumber();
                    }

                case Quote:
                    return ParameterValueState.NoNextState.getNumber();
            }

            return ParameterValueState.NoNextState.getNumber();
        };

        FSM fsm = new FSM(states, acceptingStates, initialState, nextState);
        String fsmInput = this.input.substring(this.position);
        String value = fsm.run(fsmInput);


        if (value != null) {
            this.position += value.length();
            this.column += value.length();

            return new Token(TokenType.ParameterValue, value, line, column);
        }

        return null;
    }

    private Token recognizeBlock() {
        int line = this.line;
        int column = this.column;

        Set states = new HashSet<>(Arrays.asList(BlockState.Initial.getNumber(), BlockState.Letter.getNumber(), BlockState.NoNextState.getNumber()));
        Set acceptingStates = new HashSet<>(Arrays.asList(BlockState.Letter.getNumber()));
        int initialState = BlockState.Initial.getNumber();
        BiFunction<Integer, Character, Integer> nextState = (currentState, character) -> {
            switch (BlockState.values()[currentState]) {
                case Initial:
                case Letter:
                    if (CharUtils.isLetter(character)) {
                        return BlockState.Letter.getNumber();
                    }
                    break;
            }

            return BlockState.NoNextState.getNumber();
        };

        FSM fsm = new FSM(states, acceptingStates, initialState, nextState);
        String fsmInput = this.input.substring(this.position);
        String value = fsm.run(fsmInput);


        if (value != null) {
            this.position += value.length();
            this.column += value.length();

            return new Token(TokenType.BlockName, value, line, column);
        }

        return null;
    }
}
