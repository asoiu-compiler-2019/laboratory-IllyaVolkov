import java.text.MessageFormat;
import java.util.ArrayList;

public class Parser {
    private Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = this.lexer.nextToken();
    }

    private ASTNode leafNode(ASTNodeType type) {
        if (this.currentToken.type.equalsName(type.toString())) {
            ASTNode leaf = new ASTNode(type, this.currentToken.value, this.currentToken.line, this.currentToken.column);
            this.currentToken = this.lexer.nextToken();
            return leaf;
        } else {
            throw new Error(MessageFormat.format("{0} should be of type {1}", this.currentToken, type));
        }
    }

    private ASTNode namedParameter() {
        // program: Parameter, Assign, ParameterValue
        ArrayList<ASTNode> nodes = new ArrayList<>();

        nodes.add(this.leafNode(ASTNodeType.Parameter));
        nodes.add(this.leafNode(ASTNodeType.Assign));
        nodes.add(this.leafNode(ASTNodeType.ParameterValue));

        return new ASTNode(ASTNodeType.NamedParameter, nodes, this.currentToken.line, this.currentToken.column);
    }

    private ASTNode parameterList() {
        // program: namedParameter, Comma, namedParameter, Comma, namedParameter...
        ArrayList<ASTNode> nodes = new ArrayList<>();

        if (this.currentToken.type == TokenType.Parameter) {
            nodes.add(this.namedParameter());
            while (this.currentToken.type == TokenType.Comma) {
                nodes.add(this.leafNode(ASTNodeType.Comma));
                nodes.add(this.namedParameter());
            }
        }

        return new ASTNode(ASTNodeType.ParameterList, nodes, this.currentToken.line, this.currentToken.column);
    }

    private ASTNode function() {
        // program: FunctionName, LeftParenthesis, parameterList | ParameterValue | empty, RightParenthesis
        ArrayList<ASTNode> nodes = new ArrayList<>();

        nodes.add(this.leafNode(ASTNodeType.FunctionName));
        nodes.add(this.leafNode(ASTNodeType.LeftParenthesis));

        if (this.currentToken.type == TokenType.ParameterValue) {
            nodes.add(this.leafNode(ASTNodeType.ParameterValue));
        } else {
            nodes.add(this.parameterList());
        }

        nodes.add(this.leafNode(ASTNodeType.RightParenthesis));

        return new ASTNode(ASTNodeType.Function, nodes, this.currentToken.line, this.currentToken.column);
    }

    private ASTNode statement() {
        // program: function, function, function... , Semicolon
        ArrayList<ASTNode> nodes = new ArrayList<>();

        nodes.add(this.function());
        while (this.currentToken.type == TokenType.Dot) {
            nodes.add(this.leafNode(ASTNodeType.Dot));
            nodes.add(this.function());
        }
        nodes.add(this.leafNode(ASTNodeType.Semicolon));

        return new ASTNode(ASTNodeType.Statement, nodes, this.currentToken.line, this.currentToken.column);
    }

    private ASTNode block() {
        // program: BlockName, Colon, statement, statement, statement...
        ArrayList<ASTNode> nodes = new ArrayList<>();
        nodes.add(this.leafNode(ASTNodeType.BlockName));
        nodes.add(this.leafNode(ASTNodeType.Colon));

        while (this.currentToken.type == TokenType.FunctionName) {
            nodes.add(this.statement());
        }

        return new ASTNode(ASTNodeType.Block, nodes, this.currentToken.line, this.currentToken.column);
    }

    private ASTNode program() {
        // program: block, block, block...
        ArrayList<ASTNode> nodes = new ArrayList<>();

        while (this.currentToken.type != TokenType.EndOfInput) {
            nodes.add(this.block());
        }

        return new ASTNode(ASTNodeType.Program, nodes, this.currentToken.line, this.currentToken.column);
    }

    public ASTNode parse() {
        return this.program();
    }
}
