import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(readFile("code.txt", Charset.defaultCharset()));
        Parser parser = new Parser(lexer);
        ASTNode astRoot = parser.parse();
        SemanticAnalyser analyser = new SemanticAnalyser(astRoot);
        Interpreter interpreter = new Interpreter(astRoot);
        analyser.analyse();
        interpreter.execute();
    }

    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
