import java.text.MessageFormat;


enum TokenType {
    /// Functions
    FunctionName("FunctionName"),
    LeftParenthesis("LeftParenthesis"),            // (
    RightParenthesis("RightParenthesis"),          // )

    /// Function params
    Parameter("Parameter"),
    ParameterValue("ParameterValue"),
    Assign("Assign"),                               // =
    Comma("Comma"),                                 // ,

    //Blocks
    BlockName("BlockName"),
    Dot("Dot"),                                     // .
    Colon("Colon"),                                 // :
    Semicolon("Semicolon"),                         // ;

    /// Special tokens
    EndOfInput("EndOfInput");

    private final String name;

    TokenType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}


public class Token {
    public TokenType type;
    public String value;
    public Integer line;
    public Integer column;

    public Token(TokenType type, String value, Integer line, Integer column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public Token(TokenType type) {
        this.type = type;
        this.value = "";
        this.line = 0;
        this.column = 0;
    }

    public String toString() {
        return MessageFormat.format("\nToken '{' type: {0}, value: {1}, line: {2}, column: {3} '}'", this.type, this.value, this.line, this.column);
    }
}
