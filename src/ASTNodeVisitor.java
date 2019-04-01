import javafx.util.Pair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;


class FunctionValue {
    public String name;
    public String valueParam;
    public ArrayList<Pair<String, String>> namedParams;

    public FunctionValue(String name, String valueParam) {
        this.name = name;
        this.valueParam = valueParam;
    }

    public FunctionValue(String name, ArrayList<Pair<String, String>> namedParams) {
        this.name = name;
        this.namedParams = namedParams;
    }
}


class BlockValue {
    public String name;
    public ArrayList<ArrayList<FunctionValue>> statements;

    public BlockValue(String name, ArrayList<ArrayList<FunctionValue>> statements) {
        this.name = name;
        this.statements = statements;
    }
}


public class ASTNodeVisitor {
    ASTNode root;

    public ASTNodeVisitor(ASTNode root) {
        this.root = root;
    }

    protected Object visit(ASTNode node) {
        switch (node.type) {
            case Program: return this.visitProgram(node);
            case Block: return this.visitBlock(node);
            case BlockName: return this.visitBlockName(node);
            case Colon: return this.visitColon(node);
            case Statement: return this.visitStatement(node);
            case Semicolon: return this.visitSemicolon(node);
            case Function: return this.visitFunction(node);
            case Dot: return this.visitDot(node);
            case FunctionName: return this.visitFunctionName(node);
            case LeftParenthesis: return this.visitLeftParenthesis(node);
            case RightParenthesis: return this.visitRightParenthesis(node);
            case ParameterList: return this.visitParameterList(node);
            case Comma: return this.visitComma(node);
            case NamedParameter: return this.visitNamedParameter(node);
            case Parameter: return this.visitParameter(node);
            case Assign: return this.visitAssign(node);
            case ParameterValue: return this.visitParameterValue(node);
            default: throw new Error(MessageFormat.format("Node visitor: unsupported visit{0} method.", node.type));
        }
    }

    protected ArrayList<BlockValue> visitProgram(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        ArrayList<BlockValue> value = new ArrayList<>();

        while (childrenIterator.hasNext()) {
            value.add((BlockValue) this.visit(childrenIterator.next()));
        }
        return value;
    }

    protected BlockValue visitBlock(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        String blockName = (String) this.visit(childrenIterator.next());
        ArrayList<ArrayList<FunctionValue>> statements = new ArrayList<>();

        this.visit(childrenIterator.next());                                                                            // Colon
        while (childrenIterator.hasNext()) {
            statements.add((ArrayList<FunctionValue>) this.visit(childrenIterator.next()));
        }
        return new BlockValue(blockName, statements);
    }

    protected String visitBlockName(ASTNode node) {
        return node.value;
    }

    protected String visitColon(ASTNode node) {
        return node.value;
    }

    protected ArrayList<FunctionValue> visitStatement(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        ArrayList<FunctionValue> value = new ArrayList<>();

        while (childrenIterator.hasNext()) {
            value.add((FunctionValue) this.visit(childrenIterator.next()));
            this.visit(childrenIterator.next());                                                                        // Dot | Semicolon
        }
        return value;
    }

    protected String visitSemicolon(ASTNode node) {
        return node.value;
    }

    protected FunctionValue visitFunction(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        String functionName = (String) this.visit(childrenIterator.next());

        this.visit(childrenIterator.next());                                                                            // LeftParenthesis
        Object param = this.visit(childrenIterator.next());
        this.visit(childrenIterator.next());                                                                            // RightParenthesis

        if (param instanceof String) {
            return new FunctionValue(functionName, (String) param);
        } else if (param instanceof ArrayList) {
            return new FunctionValue(functionName, (ArrayList<Pair<String, String>>) param);
        }
        throw new Error(MessageFormat.format("Unknown parameter kind in function {0} line {1}, column {2}.", functionName, node.line, node.column));
    }

    protected String visitDot(ASTNode node) {
        return node.value;
    }

    protected String visitFunctionName(ASTNode node) {
        return node.value;
    }

    protected String visitLeftParenthesis(ASTNode node) {
        return node.value;
    }

    protected String visitRightParenthesis(ASTNode node) {
        return node.value;
    }

    protected ArrayList<Pair<String, String>> visitParameterList(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        ArrayList<Pair<String, String>> value = new ArrayList<>();

        if (childrenIterator.hasNext()) {
            value.add((Pair<String, String>) this.visit(childrenIterator.next()));
            while (childrenIterator.hasNext()) {
                this.visit(childrenIterator.next());                                                                    // Comma
                value.add((Pair<String, String>) this.visit(childrenIterator.next()));
            }
        }
        return value;
    }

    protected String visitComma(ASTNode node) {
        return node.value;
    }

    protected Pair<String, String> visitNamedParameter(ASTNode node) {
        Iterator<ASTNode> childrenIterator = node.children.iterator();
        String parameterName = (String) this.visit(childrenIterator.next());

        this.visit(childrenIterator.next());                                                                            // Assign
        String parameterValue = (String) this.visit(childrenIterator.next());
        return new Pair<>(parameterName, parameterValue);
    }

    protected String visitParameter(ASTNode node) {
        return node.value;
    }

    protected String visitAssign(ASTNode node) {
        return node.value;
    }

    protected String visitParameterValue(ASTNode node) {
        return node.value.substring(1, node.value.length() - 1);
    }
}
