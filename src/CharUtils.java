public class CharUtils {
    public static boolean isWhitespaceOrNewLine(char c) {
        return "\r\n ".indexOf(c) != -1;
    }

    public static boolean isNewLine(char c) {
        return "\n".indexOf(c) != -1;
    }

    public static boolean isLetter(char c) {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) != -1;
    }

    public static boolean isPunctuation(char c) {
        return ".,:;=".indexOf(c) != -1;
    }

    public static boolean isParenthesis(char c) {
        return "()".indexOf(c) != -1;
    }
}
