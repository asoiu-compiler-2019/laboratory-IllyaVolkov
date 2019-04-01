import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class ConnectInfo {
    public String host;
    public String login;
    public String password;

    public ConnectInfo(String host, String login, String password) {
        this.host = host;
        this.login = login;
        this.password = password;
    }
}


public class SemanticAnalyser extends ASTNodeVisitor {
    private final static String[] VALID_BLOCK_NAMES = {"!connect", "!mentors", "!users"};
    private final static String[] CONNECT_FUNCTION_NAMES = {"host", "login", "password"};
    private final static String[] CREATE_FUNCTION_NAMES = {"get", "add"};
    private final static String[] MODIFY_FUNCTION_NAMES = {"min", "max", "filter", "set"};
    private final static String[] PROCESS_FUNCTION_NAMES = {"print", "count", "save", "delete"};
    private final static String[] VALID_FUNCTION_NAMES = {"host", "login", "password", "get", "add", "min", "max", "filter", "set", "print", "count", "save", "delete"};

    private ConnectInfo connectInfo = null;

    public SemanticAnalyser(ASTNode root) {
        super(root);
    }

    public void analyse() {
        this.visit(this.root);
        System.out.println("Analysis complete! No issues were found.");
    }

    @Override
    protected ArrayList<BlockValue> visitProgram(ASTNode node) {
        ArrayList<BlockValue> blocks = super.visitProgram(node);
        return blocks;
    }

    @Override
    protected BlockValue visitBlock(ASTNode node) {
        BlockValue block = super.visitBlock(node);
        if (Arrays.asList(VALID_BLOCK_NAMES).indexOf(block.name) == -1) {
            this.error(node, MessageFormat.format("Unknown block: {0}", block.name));
        }

        if (block.name.equals("!connect")) {
            String host = null;
            String login = null;
            String password = null;

            if (block.statements.size() != 1) {
                this.error(node, "Invalid !connect statements number.");
            }
            for (FunctionValue function : block.statements.get(0)) {
                switch (function.name) {
                    case "host": host = function.valueParam; break;
                    case "login": login = function.valueParam; break;
                    case "password": password = function.valueParam; break;
                    default: this.error(node, MessageFormat.format("Invalid !connect function {0}.", function.name));
                }
            }

            if (host != null && login != null && password != null) {
                this.connectInfo = new ConnectInfo(host, login, password);
            } else {
                this.error(node, "Invalid !connect statement");
            }
        } else {
            if (this.connectInfo == null) {
                this.error(node, MessageFormat.format("Missing connection info for {0} block.", block.name));
            }
        }
        return block;
    }

    @Override
    protected ArrayList<FunctionValue> visitStatement(ASTNode node) {
        ArrayList<FunctionValue> functions = super.visitStatement(node);
        List<String> functionNames = functions.stream().map((FunctionValue function) -> function.name).collect(Collectors.toList());

        if (functionNames.containsAll(Arrays.asList(CONNECT_FUNCTION_NAMES))) {
            if (functionNames.size() != 3) {
                this.error(node, "Invalid !connect functions number.");
            }
        } else {
            for (int i = 0; i < functionNames.size(); i++) {
                String functionName = functionNames.get(i);

                if (i == 0) {
                    if (!Arrays.asList(CREATE_FUNCTION_NAMES).contains(functionName)) {
                        this.error(node, MessageFormat.format("Invalid create instance function {0}.", functionName));
                    }
                } else if (i == functions.size() - 1) {
                    if (!Arrays.asList(PROCESS_FUNCTION_NAMES).contains(functionName)) {
                        this.error(node, MessageFormat.format("Invalid process instance function {0}.", functionName));
                    }
                } else if (!Arrays.asList(MODIFY_FUNCTION_NAMES).contains(functionName)) {
                    this.error(node, MessageFormat.format("Invalid modify instance function {0}.", functionName));
                }
            }
        }
        return functions;
    }

    @Override
    protected FunctionValue visitFunction(ASTNode node) {
        FunctionValue function = super.visitFunction(node);
        if (Arrays.asList(VALID_FUNCTION_NAMES).indexOf(function.name) == -1) {
            this.error(node, MessageFormat.format("Unknown function: {0}", function.name));
        }
        return function;
    }


    private Object error(ASTNode node, String message) {
        throw new Error(MessageFormat.format("Semantic error at line {0} and column {1}:\n{2}", node.line, node.column, message));
    }
}
