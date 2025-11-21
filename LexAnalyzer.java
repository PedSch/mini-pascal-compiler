import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public final class LexAnalyzer {
     static String tokenName = "";
     static int lineRow = 0;
     static int lineCol = 0;
     static boolean readingString = false;
     static boolean readingNumber = false;
     static boolean isFloat = false;
     static boolean sciNotation = false;
     static boolean readingColon = false;
     static boolean readingBool = false;
     static boolean readingDot = false;

     static ArrayList<Token> tokenArrayList = new ArrayList<>();

    enum TYPE {
        LETTER, DIGIT, SPACE, OPERATOR, QUOTE
    }

     static final HashMap<String, String> KEYWORDS_TOKEN;
    static {
        KEYWORDS_TOKEN = new HashMap<>();
        String word;

        try {
            //Instead of declaring the keywords here, we use a File. I added more than is nessicary for the deliverables, because I just copied and pasted from a list of pascal's reserved words.
            Scanner sc = new Scanner(new File("keywords.txt")); 
            while(sc.hasNext()){
                word = sc.next();
                KEYWORDS_TOKEN.put(word, String.format("%s", word.toUpperCase()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

     static final HashMap<String, String> OPERATORS_TOKEN;
    static {
        OPERATORS_TOKEN = new HashMap<>();
        OPERATORS_TOKEN.put(":", "COLON");
        OPERATORS_TOKEN.put(";", "SEMI_COLON");
        OPERATORS_TOKEN.put("+", "PLUS");
        OPERATORS_TOKEN.put("-", "MINUS");
        OPERATORS_TOKEN.put("*", "MULTIPLY");
        OPERATORS_TOKEN.put("/", "DIVIDE");
        OPERATORS_TOKEN.put("<", "LESS_THAN");
        OPERATORS_TOKEN.put("<=", "LESS_THAN_EQUAL");
        OPERATORS_TOKEN.put(">", "GREATER_THAN");
        OPERATORS_TOKEN.put(">=", "GREATER_THAN_EQUAL");
        OPERATORS_TOKEN.put(":=", "ASSIGNMENT");
        OPERATORS_TOKEN.put(",", "COMMA");
        OPERATORS_TOKEN.put("=", "EQUAL");
        OPERATORS_TOKEN.put("<>", "NOT_EQUAL");
        OPERATORS_TOKEN.put("(", "OPEN_PARENTHESIS");
        OPERATORS_TOKEN.put(")", "CLOSE_PARENTHESIS");
        OPERATORS_TOKEN.put("[", "OPEN_SQUARE_BRACKET");
        OPERATORS_TOKEN.put("]", "CLOSE_SQUARE_BRACKET");
        OPERATORS_TOKEN.put(".", "DOT");
        OPERATORS_TOKEN.put("..", "RANGE");
    }

     static final HashMap<String, TYPE> CHAR_TYPE;
    static {
        CHAR_TYPE = new HashMap<>();
        for (int i = 65; i < 91; i++){
            String currentChar = String.valueOf(Character.toChars(i)[0]);
            CHAR_TYPE.put(currentChar, TYPE.LETTER);
            CHAR_TYPE.put(currentChar.toLowerCase(), TYPE.LETTER);
        }
        for (int i = 48; i < 58; i++){
            String currentChar = String.valueOf(Character.toChars(i)[0]);
            CHAR_TYPE.put(currentChar, TYPE.DIGIT);
        }
        for (int i = 1; i < 33; i++){
            String currentChar = String.valueOf(Character.toChars(i)[0]);
            CHAR_TYPE.put(currentChar, TYPE.SPACE);
        }

        for (String key: OPERATORS_TOKEN.keySet()) {
            CHAR_TYPE.put(key, TYPE.OPERATOR);
        }

        CHAR_TYPE.put(String.valueOf(Character.toChars(39)[0]), TYPE.QUOTE);
    }

    public static ArrayList<Token> scan(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file).useDelimiter("");

        while (sc.hasNext()) {
            char element = sc.next().toLowerCase().charAt(0);

            checkCharacter(element);
        }

        tokenName = "EOF";
        generateToken("EOF");

        return tokenArrayList;
    }

    public static void checkCharacter(char element){
        switch (CHAR_TYPE.get(String.valueOf(element))){

            case LETTER:
                if (!readingNumber) {
                    tokenName += element;
                }

                if (element == 'E' && readingNumber) {
                    tokenName += element;
                    sciNotation = true;
                }

                break;
            case DIGIT:
                if (tokenName.isEmpty()) {
                    readingNumber = true;
                }

                tokenName += element;

                break;
            case SPACE:
                if (readingString){
                    tokenName += element;
                } else if (readingColon) {
                    generateToken(OPERATORS_TOKEN.get(tokenName));

                    readingColon = false;

                } else if (readingBool) {
                    generateToken(OPERATORS_TOKEN.get(tokenName));

                    readingBool = false;

                } else if (!readingNumber) {
                    tokenName = endOfWord();

                    if (element == Character.toChars(10)[0]){
                        lineRow++;
                        lineCol = 0;
                    } else if (element == Character.toChars(9)[0]){
                        lineCol+=4;
                    } else if (element == Character.toChars(32)[0]){
                        lineCol++;
                    }
                } else {
                    handleNumber();
                }
                break;
            case OPERATOR:
                if (readingDot && element == '.') {
                    if (tokenName.equals(".")) {
                        tokenName = "";
                        generateToken("RANGE");
                    } else {
                        generateToken(tokenName.substring(0, tokenName.length()-2));
                        generateToken("DOT");
                        tokenName = "";
                    }
                    readingDot = false;

                } else if(readingString) {
                    tokenName += element;
                } else if (readingNumber) {
                    if (isFloat && element == '.') {
                        isFloat = false;
                        tokenName = tokenName.substring(0,tokenName.length()-1);
                        handleNumber();

                        generateToken("RANGE");
                        tokenName = "";

                    } else if (sciNotation && (element == '+' || element == '-')) {
                        tokenName += element;
                    } else if (element == '.') {
                        isFloat = true;
                        tokenName += element;
                    } else {
                        handleNumber();

                        generateToken(OPERATORS_TOKEN.get(String.valueOf(element)));
                    }
                } else if (readingColon && element == '=') {
                    tokenName += element;
                    generateToken(OPERATORS_TOKEN.get(tokenName));
                    readingColon = false;
                } else if (readingBool) {
                    if (tokenName.equals("<") && ((element == '=') || (element == '>'))) {
                        tokenName += element;
                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    } else if (tokenName.equals(">") && (element == '=')) {
                        tokenName += element;
                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    }

                    readingBool = false;
                } else {
                    if (element == ';') {
                        tokenName = endOfWord();

                        tokenName = ";";
                        generateToken(OPERATORS_TOKEN.get(String.valueOf(element)));
                    } else if (element == ':') {
                        tokenName = endOfWord();
                        readingColon = true;
                        tokenName += element;
                    } else if (element == '<' || element == '>') {
                        tokenName = endOfWord();
                        readingBool = true;
                        tokenName += element;
                    } else if (element == '.') {
                        tokenName += element;

                        if (tokenName.equals("end.")){
                            generateToken("END");
                            generateToken("DOT");
                        } else {
                            readingDot = true;
                        }
                    } else if (OPERATORS_TOKEN.containsKey(String.valueOf(element))) {
                        tokenName = endOfWord();

                        tokenName = String.valueOf(element);
                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    }
                }
                break;
            case QUOTE:
                readingString = !readingString;
                tokenName += element;

                if (!readingString) {
                    tokenName = tokenName.substring(1, tokenName.length()-1);
                    if (tokenName.length() == 1) {
                        generateToken("CHARLIT");
                    } else if (tokenName.length() > 1) {
                        generateToken("STRLIT");
                    }
                }
                break;
            default:
                throw new Error("Unhandled element scanned");
        }
    }

    public static String endOfWord(){
        if(KEYWORDS_TOKEN.containsKey(tokenName)){
            generateToken(KEYWORDS_TOKEN.get(tokenName));
        } else {
            if (tokenName.length() > 0) {

                if(tokenName.equals("true") || tokenName.equals("false")) {
                    generateToken("BOOLLIT");
                } else {
                    generateToken("IDENTIFIER");
                }
            }
        }

        clearStatuses();

        return tokenName;
    }

    public static void clearStatuses() {
        readingString = false;
        readingNumber = false;
        isFloat = false;
        sciNotation = false;
        readingColon = false;
        readingBool = false;
    }

    public static void generateToken(String tokenType) {
        Token t = new Token(tokenType, tokenName, lineCol, lineRow);
        tokenArrayList.add(t);

        lineCol += tokenName.length();

        tokenName = "";
    }

    public static void handleNumber() {
        readingNumber = false;
        if (isFloat) {
            generateToken("FLOATLIT");
            isFloat = false;
        } else {
            generateToken("INTLIT");
        }
    }
}