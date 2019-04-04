import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;


enum ASTNodeType {
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

    // Abstract nodes
    Program("Program"),
    Block("Block"),
    Statement("Statement"),
    Function("Function"),
    ParameterList("ParameterList"),
    NamedParameter("NamedParameter");

    private final String name;

    ASTNodeType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}



public class ASTNode {
    public ASTNodeType type;
    public String value;
    public ArrayList<ASTNode> children;
    public Integer line;
    public Integer column;

    // Leaf node constructor
    public ASTNode(ASTNodeType type, String value, Integer line, Integer column) {
        this.type = type;
        this.value = value;
        this.children = null;
        this.line = line;
        this.column = column;
    }

    // Abstract node constructor
    public ASTNode(ASTNodeType type, ArrayList<ASTNode> children, Integer line, Integer column) {
        this.type = type;
        this.value = null;
        this.children = children;
        this.line = line;
        this.column = column;
    }

    public String toString() {
        int level = 0;
        if (this.children == null) {
            return MessageFormat.format("\nLeaf node '{' type: {0}, value: {1}'}'", this.type, this.value);
        } else {
            return MessageFormat.format("\nAbstract node '{' type: {0}, children: {1}'}'", this.type, this.children.stream().map((ASTNode obj) -> obj.toString(level + 1)).collect(Collectors.toList()));
        }
    }

    public String toString(int level) {
        String formatChars = "\n";

        for (int n = 0; n < level; n++) {
            formatChars += "\t";
        }
        if (this.children == null) {
            return MessageFormat.format("{2}Leaf node '{' type: {0}, value: {1}'}'", this.type, this.value, formatChars);
        } else {
            return MessageFormat.format("{2}Abstract node '{' type: {0}, children: {1}'}'", this.type, this.children.stream().map((ASTNode obj) -> obj.toString(level + 1)).collect(Collectors.toList()), formatChars);
        }
    }
}
