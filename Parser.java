import java.nio.ByteBuffer;
import java.util.*;
/*
    HOW THIS FILE IS ARRAGED:
    At the top, Pcode operations, hashmaps, and TYPE are declared and initalized. TYPE is a single enum type that we will use for Switch statement cases.
    Then we have Stack ops, such as accesses and saves. These contain instructions that access the stack to load and emit addresses that the p-code will use.
    Also in this general area, are operations such as Assign and the like.
    Finally, we have the basis of the compiler, Expression, F, T, etc. These have one charcter names for the purpose of using Cases.
    Emit is also at the bottom, this is a general method that will output the p-code.

    This method of parser really takes advantage of cases, which makes it very simple to add functions down the road.
*/

public final class Parser {
    enum TYPE {
        I, R, B, C, S, P, L, A     //integer, real, boolean, char, string, procedure, label, array
    }
    static int dp = 0; //data pointer for vars
    static final HashMap<String, TYPE> STRING_TYPE_HASH_MAP;
    static {
        STRING_TYPE_HASH_MAP = new HashMap<>();
        STRING_TYPE_HASH_MAP.put("integer", TYPE.I);
        STRING_TYPE_HASH_MAP.put("real", TYPE.R);
        STRING_TYPE_HASH_MAP.put("boolean", TYPE.B);
        STRING_TYPE_HASH_MAP.put("char", TYPE.C);
        STRING_TYPE_HASH_MAP.put("string", TYPE.S);
        STRING_TYPE_HASH_MAP.put("array", TYPE.A);

    }
    //P-Code operations for Cases
    enum OP_CODE {
        PUSHI, PUSH, POP,PUSHF, JMP, JFALSE, JTRUE, CVR, CVI, DUP, XCHG, REMOVE, ADD, SUB, MULT, DIV, NEG, OR, AND,
        FADD, FSUB, FMULT, FDIV, FNEG, EQL, NEQL, GEQ, LEQ, GTR, LSS, FGTR, FLSS, HALT, PRINT_INT, PRINT_CHAR, PRINT_BOOL,
        PRINT_REAL, PRINT_NEWLINE, GET, PUT
    }
    static int ADDRESS_SIZE = 4;
    static Token currentToken;
    static Iterator<Token> it;
    static int INSTRUCTION_SIZE = 1000;
    static Byte[] byteArray = new Byte[INSTRUCTION_SIZE];
    static int ip = 0;
    public static Byte[] parse() {
        getToken(); //Get initial token

        match("PROGRAM");
        match("IDENTIFIER");
        match("SEMI_COLON");

        program();

        return byteArray;
    }
    public static void program() {
        declarations();
        begin();
    }
    public static void declarations() {
        while (true) {
            switch (currentToken.getTokenType()) {
                case "VAR":
                    variableC();
                    break;
                case "PROCEDURE":
                    procDeclaration();
                    break;
                case "LABEL":
                    labelDeclarations();
                    break;
                case "BEGIN":
                    return;
            }
        }
    }
     static void labelDeclarations() {
        while(true) {
            if ("LABEL".equals(currentToken.getTokenType())) {
                match("LABEL");
            } else {
                //currentToken is not "LABEL"
                break;
            }
            //Store labels in a list
            ArrayList<Token> labelsArrayList = new ArrayList<>();
            while ("IDENTIFIER".equals(currentToken.getTokenType())) {
                currentToken.setTokenType("A_LABEL");
                labelsArrayList.add(currentToken);

                match("A_LABEL");

                if ("COMMA".equals(currentToken.getTokenType())) {
                    match("COMMA");
                }
            }
            //insert all labels into SymbolTable
            for (Token label : labelsArrayList) {
                Symbol symbol = new Symbol(label.getTokenValue(), "A_LABEL", TYPE.L, 0);
                if (SymbolTable.lookupS(label.getTokenValue()) == null) {
                    SymbolTable.insert(symbol);
                }
            }
            match("SEMI_COLON");
        }
    }
     static void procDeclaration() {
        //declaration
        if (currentToken.getTokenType().equals("PROCEDURE")) {
            match("PROCEDURE");
            currentToken.setTokenType("A_PROC");

            String procedureName = currentToken.getTokenValue();

            match("A_PROC");
            match("SEMI_COLON");

            //generate hole to jump past the body
            genOpCode(OP_CODE.JMP);
            int hole = ip;
            genAddress(0);

            Symbol symbol = new Symbol(procedureName,"A_PROC",TYPE.P,ip);
            match("BEGIN");
            statements();
            match("END");
            match("SEMI_COLON");
            genOpCode(OP_CODE.JMP);
            symbol.setReturnAddress(ip);
            genAddress(0);

            if (SymbolTable.lookupS(procedureName) == null) {
                SymbolTable.insert(symbol);
            }
            int save = ip;

            ip = hole;
            genAddress(save);
            ip = save;
        }
    }
    public static void variableC() {
        while(true) {
            if ("VAR".equals(currentToken.getTokenType())) {
                match("VAR");
            } else {
                //currentToken is not "VAR"
                break;
            }
            //Store variables in a list
            ArrayList<Token> variablesArrayList = new ArrayList<>();
            while ("IDENTIFIER".equals(currentToken.getTokenType())) {
                currentToken.setTokenType("A_VAR");
                variablesArrayList.add(currentToken);
                match("A_VAR");
                if ("COMMA".equals(currentToken.getTokenType())) {
                    match("COMMA");
                }
            }
            match("COLON");
            String dataType = currentToken.getTokenType();
            match(dataType);
            //Add the correct datatype for each identifier and insert into symbol table
            for (Token var : variablesArrayList) {
                Symbol symbol = new Symbol(var.getTokenValue(),
                        "A_VAR",
                        STRING_TYPE_HASH_MAP.get(dataType.toLowerCase().substring(3)),
                        dp);

                dp += 4;
                if (SymbolTable.lookupS(var.getTokenValue()) == null) {
                    SymbolTable.insert(symbol);
                }
            }

            if (dataType.equals("ARRAY")){
                arrayDeclaration(variablesArrayList);
            }

            match("SEMI_COLON");

        }
    }
     static void arrayDeclaration(ArrayList<Token> variablesArrayList) {
        match("OPEN_SQUARE_BRACKET");
        String v1 = currentToken.getTokenValue();
        TYPE indexType1 = getLitType(currentToken.getTokenType());
        match(currentToken.getTokenType());
        match("RANGE");
        String v2 = currentToken.getTokenValue();
        TYPE indexType2 = getLitType(currentToken.getTokenType());
        match(currentToken.getTokenType());
        match("CLOSE_SQUARE_BRACKET");
        match("OF");
        String valueType = currentToken.getTokenType();
        match(valueType);
        if (indexType1 != indexType2){
            throw new Error(String.format("Array index LHS type (%s) is not equal to RHS type: (%s)", indexType1, indexType2));
        } else {
            assert indexType1 != null;
            switch (indexType1) {
                case I:
                    int i1 = Integer.valueOf(v1);
                    int i2 = Integer.valueOf(v2);
                    if (i1 > i2){
                        throw new Error(String.format("Array range is invalid: %d..%d", i1, i2));
                    }
                    Symbol firstIntArray = SymbolTable.lookupS(variablesArrayList.get(0).getTokenValue());
                    if (firstIntArray != null) {
                        dp = firstIntArray.getAddress();
                    }
                    for (Token var: variablesArrayList) {
                        Symbol symbol = SymbolTable.lookupS(var.getTokenValue());
                        if (symbol != null){

                            int elementSize = 4;
                            int size = elementSize*(i2 - i1 + 1);

                            symbol.setAddress(dp);
                            symbol.setLow(i1);
                            symbol.setHigh(i2);
                            symbol.setTokenType("AN_ARRAY");
                            symbol.setIndexType(TYPE.I);
                            symbol.setValueType(STRING_TYPE_HASH_MAP.get(valueType.toLowerCase().substring(3)));

                            dp += size;
                        }
                    }
                    break;
                case C:
                    char c1 = v1.toCharArray()[0];
                    char c2 = v2.toCharArray()[0];
                    if (c1 > c2){
                        throw new Error(String.format("Array range is invalid: %c..%c", c1, c2));
                    }

                    Symbol firstCharArray = SymbolTable.lookupS(variablesArrayList.get(0).getTokenValue());
                    if (firstCharArray != null) {
                        dp = firstCharArray.getAddress();
                    }

                    for (Token var: variablesArrayList) {
                        Symbol symbol = SymbolTable.lookupS(var.getTokenValue());
                        if (symbol != null){
                            int size = c2 - c1 + 1;

                            symbol.setAddress(dp);
                            symbol.setLow(c1);
                            symbol.setHigh(c2);
                            symbol.setTokenType("AN_ARRAY");
                            symbol.setIndexType(TYPE.C);
                            symbol.setValueType(STRING_TYPE_HASH_MAP.get(valueType.toLowerCase().substring(3)));

                            dp += size;
                        }
                    }

                    break;
                case R:
                    throw new Error("Array index type: real is invalid");
            }

        }

    }
    public static void begin(){
        match("BEGIN");
        statements();
        match("END");
        match("DOT");
        match("EOF");
        genOpCode(OP_CODE.HALT);
    }
    public static void statements(){
        while(!currentToken.getTokenType().equals("END")) {
            switch (currentToken.getTokenType()) {
                case "CASE":
                    caseStat();
                    break;
                case "GOTO":
                    goToStat();
                    break;
                case "WHILE":
                    whileStat();
                    break;
                case "REPEAT":
                    repeatStat();
                    break;
                case "IF":
                    ifStat();
                    break;
                case "FOR":
                    forStat();
                    break;
                case "WRITELN":
                    writeStat();
                    break;
                case "IDENTIFIER":
                    Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
                    if (symbol != null) {
                        //assign token type to be var, proc, or label
                        currentToken.setTokenType(symbol.getTokenType());
                    }
                    break;
                case "A_VAR":
                    assignmentStat();
                    break;
                case "A_PROC":
                    procedureStat();
                    break;
                case "A_LABEL":
                    //labelStat(); // TODO: Method not implemented
                    break;
                case "AN_ARRAY":
                    arrayAssignmentStat();
                    break;
                case "SEMI_COLON":
                    match("SEMI_COLON");
                    break;
                default:
                    return;
            }
        }

    }
     static void procedureStat() {
        Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
        if (symbol != null) {
            int address = symbol.getAddress();
            match("A_PROC");
            match("SEMI_COLON");
            //call procedure
            genOpCode(OP_CODE.JMP);
            genAddress(address);
            int restore = ip;
            //fill in return hole and restore ip
            ip = symbol.getReturnAddress();
            genAddress(restore);
            ip = restore;
        }
    }
     static void goToStat() {
        match("GOTO");
        Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
        currentToken.setTokenType("A_LABEL");
        match("A_LABEL");
        genOpCode(OP_CODE.JMP);
        int hole = ip;
        genAddress(0);
        //hole for jump
        if (symbol != null){
            symbol.setAddress(hole);
        }
        match("SEMI_COLON");
    }
     static void forStat() {
        match("FOR");
        String varName = currentToken.getTokenValue();
        currentToken.setTokenType("A_VAR");
        assignmentStat();
        int target = ip;
        Symbol symbol = SymbolTable.lookupS(varName);
        if (symbol != null) {
            int address = symbol.getAddress();
            match("TO");
            //Generate op code for x <= <upper bound>
            genOpCode(OP_CODE.PUSH);
            genAddress(address);
            genOpCode(OP_CODE.PUSHI);
            genAddress(Integer.valueOf(currentToken.getTokenValue()));
            genOpCode(OP_CODE.LEQ);
            match("INTLIT");
            match("DO");
            genOpCode(OP_CODE.JFALSE);
            int hole = ip;
            genAddress(0);
            match("BEGIN");
            statements();
            match("END");
            match("SEMI_COLON");
            genOpCode(OP_CODE.PUSH);
            genAddress(address);
            genOpCode(OP_CODE.PUSHI);
            genAddress(1);
            genOpCode(OP_CODE.ADD);
            genOpCode(OP_CODE.POP);
            genAddress(address);
            genOpCode(OP_CODE.JMP);
            genAddress(target);
            int save = ip;
            ip = hole;
            genAddress(save);
            ip = save;
        }
    }
    static void repeatStat() {
        match("REPEAT");
        int target = ip;
        statements();
        match("UNTIL");
        C();
        genOpCode(OP_CODE.JFALSE);
        genAddress(target);
    }
    static void whileStat() {
        match("WHILE");
        int target = ip;
        C();
        match("DO");
        genOpCode(OP_CODE.JFALSE);
        int hole = ip;
        genAddress(0);
        match("BEGIN");
        statements();
        match("END");
        match("SEMI_COLON");
        genOpCode(OP_CODE.JMP);
        genAddress(target);
        int save = ip;
        ip = hole;
        genAddress(save);
        ip = save;
    }
    public static void ifStat(){
        match("IF");
        C();
        match("THEN");
        genOpCode(OP_CODE.JFALSE);
        int hole1 = ip;
        genAddress(0); //Holder value for the address
        statements();
        if(currentToken.getTokenType().equals("ELSE")) {
            genOpCode(OP_CODE.JMP);
            int hole2 = ip;
            genAddress(0);
            int save = ip;
            ip = hole1;
            genAddress(save); //JFALSE to this else statement
            ip = save;
            hole1 = hole2;
            statements();
            match("ELSE");
            statements();
        }
        int save = ip;
        ip = hole1;
        genAddress(save); //JFALSE to outside the if statement in if-then or JMP past the else statement in if-else
        ip = save;
    }
    public static void caseStat() {
        match("CASE");
        match("OPEN_PARENTHESIS");
        Token eToken = currentToken;
        TYPE t1 = E();
        if (t1 == TYPE.R) {
            throw new Error("Invalid type of real for case E");
        }
        match("CLOSE_PARENTHESIS");
        match("OF");
        ArrayList<Integer> labelsArrayList = new ArrayList<>();
        while(currentToken.getTokenType().equals("INTLIT") ||
                currentToken.getTokenType().equals("CHARLIT") ||
                currentToken.getTokenType().equals("BOOLLIT")) {
            TYPE t2 = E();
            emit("EQUAL", t1, t2);
            match("COLON");
            //hole for JFALSE to the next case label when the eql condition fails
            genOpCode(OP_CODE.JFALSE);
            int hole = ip;
            genAddress(0);
            statements();
            genOpCode(OP_CODE.JMP);
            labelsArrayList.add(ip);
            genAddress(0);
            //Fill JFALSE hole
            int save = ip;
            ip = hole;
            genAddress(save);
            ip = save;
            //PUSH the original eToken variable back to prepare for the next eql condition case label
            if (!currentToken.getTokenValue().equals("END")){
                Symbol symbol = SymbolTable.lookupS(eToken.getTokenValue());
                if (symbol != null) {
                    genOpCode(OP_CODE.PUSH);
                    genAddress(symbol.getAddress());
                }
            }
        }
        match("END");
        match("SEMI_COLON");
        int save = ip;
        for (Integer labelHole: labelsArrayList) {
            ip = labelHole;
            genAddress(save);
        }
        ip = save;
    }
    public static void writeStat(){
        match("WRITELN");
        match("OPEN_PARENTHESIS");
        while (true) {
            Symbol symbol =  SymbolTable.lookupS(currentToken.getTokenValue());
            TYPE t;
            if (symbol != null) {
                if (symbol.getDataType() == TYPE.A) {
                    currentToken.setTokenType("AN_ARRAY");
                    handleArrayAccess(symbol);
                    genOpCode(OP_CODE.GET);
                    t = symbol.getValueType();

                } else {
                    //variable
                    currentToken.setTokenType("A_VAR");
                    t = symbol.getDataType();
                    genOpCode(OP_CODE.PUSH);
                    genAddress(symbol.getAddress());
                    match("A_VAR");
                }
            } else {
                t = getLitType(currentToken.getTokenType());
                assert t != null;
                switch (t) {
                    case R:
                        genOpCode(OP_CODE.PUSHF);
                        genAddress(Float.valueOf(currentToken.getTokenValue()));
                        break;
                    case I:
                        genOpCode(OP_CODE.PUSHI);
                        genAddress(Integer.valueOf(currentToken.getTokenValue()));
                        break;
                    case B:
                        genOpCode(OP_CODE.PUSHI);
                        if (currentToken.getTokenValue().equals("true")) {
                            genAddress(1);
                        } else {
                            genAddress(0);
                        }
                        break;
                    case C:
                        genOpCode(OP_CODE.PUSHI);
                        genAddress((int)(currentToken.getTokenValue().charAt(0)));
                        break;
                }
                match(currentToken.getTokenType());
            }
            assert t != null;
            switch (t) {
                case I:
                    genOpCode(OP_CODE.PRINT_INT);
                    break;
                case C:
                    genOpCode(OP_CODE.PRINT_CHAR);
                    break;
                case R:
                    genOpCode(OP_CODE.PRINT_REAL);
                    break;
                case B:
                    genOpCode(OP_CODE.PRINT_BOOL);
                    break;
                default:
                    throw new Error("Cannot write unknown type");
            }
            switch (currentToken.getTokenType()) {
                case "COMMA":
                    match("COMMA");
                    break;
                case "CLOSE_PARENTHESIS":
                    match("CLOSE_PARENTHESIS");
                    genOpCode(OP_CODE.PRINT_NEWLINE);
                    return;
                default:
                    throw new Error(String.format("Current token type (%s) is neither COMMA nor CLOSE_PARENTHESIS", currentToken.getTokenType()));
            }

        }
    }
    public static void assignmentStat() {
        Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
        if (symbol != null) {
            TYPE lhsType = symbol.getDataType();
            int lhsAddress = symbol.getAddress();
            match("A_VAR");
            match("ASSIGNMENT");
            TYPE rhsType = E();
            if (lhsType == rhsType) {
                genOpCode(OP_CODE.POP);
                genAddress(lhsAddress);
            } else {
                throw new Error(String.format("LHS type (%s) is not equal to RHS type: (%s)", lhsType, rhsType));
            }
        }
    }
     static void arrayAssignmentStat() {
        Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
        if (symbol != null) {
            handleArrayAccess(symbol);
            match("ASSIGNMENT");
            TYPE rhsType = E();
            if (symbol.getValueType() == rhsType) {
                genOpCode(OP_CODE.PUT);
            }
        }
    }
     static void handleArrayAccess(Symbol symbol) {
        match("AN_ARRAY");
        match("OPEN_SQUARE_BRACKET");
        TYPE t;
        Symbol varSymbol = SymbolTable.lookupS(currentToken.getTokenValue());
        if (varSymbol != null) {
            t = varSymbol.getDataType();
            if (t != symbol.getIndexType()) {
                throw new Error(String.format("Incompatible index type: (%s, %s)", t, symbol.getIndexType()));
            }
            currentToken.setTokenType("A_VAR");
            genOpCode(OP_CODE.PUSH);
            genAddress(varSymbol.getAddress());
            match("A_VAR");
            match("CLOSE_SQUARE_BRACKET");
            genOpCode(OP_CODE.PUSHI);
            switch (t) {
                case I:
                    int i1 = (int) symbol.getLow();
                    int i2 = (int) symbol.getHigh();
                    genAddress(i1);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.SUB);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(4);
                    genOpCode(OP_CODE.MULT);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(OP_CODE.ADD);

                    break;
                case C:
                    char c1 = (char) symbol.getLow();
                    char c2 = (char) symbol.getHigh();
                    genAddress(c1);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.SUB);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(OP_CODE.ADD);
                    break;
            }
        } else {
            String index = currentToken.getTokenValue();
            t = E();
            if (t != symbol.getIndexType()) {
                throw new Error(String.format("Incompatible index type: (%s, %s)", t, symbol.getIndexType()));
            }
            match("CLOSE_SQUARE_BRACKET");
            genOpCode(OP_CODE.PUSHI);
            switch (t) {
                case I:
                    int i1 = (int) symbol.getLow();
                    int i2 = (int) symbol.getHigh();
                    if (Integer.valueOf(index) < i1 || Integer.valueOf(index) > i2) {
                        throw new Error(String.format("Index %d is not within range %d to %d",
                                Integer.valueOf(index), i1, i2));
                    }
                    genAddress(i1);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.SUB);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(4);
                    genOpCode(OP_CODE.MULT);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(OP_CODE.ADD);
                    break;
                case C:
                    char c1 = (char) symbol.getLow();
                    char c2 = (char) symbol.getHigh();
                    if (index.toCharArray()[0] < c1 || index.toCharArray()[0] > c2) {
                        throw new Error(String.format("Index %c is not within range %c to %c",
                                index.toCharArray()[0], c1, c2));
                    }
                    genAddress(c1);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.SUB);
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(OP_CODE.ADD);
                    break;
            }

        }
    }
    //EXPRESSION
    public static TYPE E(){
        TYPE t1 = T();
        while (currentToken.getTokenType().equals("PLUS") || currentToken.getTokenType().equals("MINUS")) {
            String op = currentToken.getTokenType();
            match(op);
            TYPE t2 = T();
            t1 = emit(op, t1, t2);
        }
        return t1;
    }
    //TERM
    public static TYPE T() {
        TYPE f1 = F();
        while (currentToken.getTokenType().equals("MULTIPLY") || currentToken.getTokenType().equals("DIVIDE") || currentToken.getTokenType().equals("DIV")) {
            String op = currentToken.getTokenType();
            match(op);
            TYPE f2 = F();
            f1 = emit(op, f1, f2);
        }
        return f1;
    }
    //CONDITOINAL 
    public static TYPE C(){
        TYPE e1 = E();
        //Huge while statement that makes sure that token type is a conditional
        while (currentToken.getTokenType().equals("LESS_THAN") || currentToken.getTokenType().equals("GREATER_THAN") || currentToken.getTokenType().equals("LESS_THAN_EQUAL") ||
                currentToken.getTokenType().equals("GREATER_THAN_EQUAL") || currentToken.getTokenType().equals("EQUAL") || currentToken.getTokenType().equals("NOT_EQUAL")) {
            String pred = currentToken.getTokenType();
            match(pred);
            TYPE e2 = T();
            e1 = emit(pred, e1, e2);
        }
        return e1;
    }
    //FACTOR
    public static TYPE F() {
        switch (currentToken.getTokenType()) {
            case "IDENTIFIER":
                Symbol symbol = SymbolTable.lookupS(currentToken.getTokenValue());
                if (symbol != null) {
                    if (symbol.getTokenType().equals("A_VAR")) {
                        //variable
                        currentToken.setTokenType("A_VAR");

                        genOpCode(OP_CODE.PUSH);
                        genAddress(symbol.getAddress());

                        match("A_VAR");
                        return symbol.getDataType();
                    } else if (symbol.getTokenType().equals("AN_ARRAY")) {
                        currentToken.setTokenType("AN_ARRAY");

                        handleArrayAccess(symbol);
                        genOpCode(OP_CODE.GET);

                        return symbol.getValueType();
                    }
                } else {
                    throw new Error(String.format("Symbol not found (%s)", currentToken.getTokenValue()));
                }
            case "INTLIT":
                genOpCode(OP_CODE.PUSHI);
                genAddress(Integer.valueOf(currentToken.getTokenValue()));

                match("INTLIT");
                return TYPE.I;
            case "BOOLLIT":
                genOpCode(OP_CODE.PUSHI);
                genAddress(Boolean.valueOf(currentToken.getTokenValue()) ? 1 : 0);

                match("BOOLLIT");
                return TYPE.B;
            case "FLOATLIT":
                genOpCode(OP_CODE.PUSHF);
                genAddress(Float.valueOf(currentToken.getTokenValue()));

                match("FLOATLIT");
                return TYPE.R;
            case "CHARLIT":
                genOpCode(OP_CODE.PUSHI);
                genAddress(currentToken.getTokenValue().charAt(0));

                match("CHARLIT");
                return TYPE.C;
            case "STRLIT":
                for (char c: currentToken.getTokenType().toCharArray()) {
                    genOpCode(OP_CODE.PUSHI);
                    genAddress(c);
                }
                match("STRLIT");
                return TYPE.S;
            case "NOT":
                match("NOT");
                return F();
            case "OPEN_PARENTHESIS":
                match("OPEN_PARENTHESIS");
                TYPE t = E();
                match("CLOSE_PARENTHESIS");
                return t;
            default:
                throw new Error("Unknown data type");
        }
    }
    public static TYPE emit(String op, TYPE t1, TYPE t2){
        switch (op) {
            case "PLUS":
                if (t1 == TYPE.I && t2 == TYPE.I) {
                    genOpCode(OP_CODE.ADD);
                    return TYPE.I;
                } else if (t1 == TYPE.I && t2 == TYPE.R) {
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FADD);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.I) {
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FADD);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.R) {
                    genOpCode(OP_CODE.FADD);
                    return TYPE.R;
                }
            case "MINUS":
                if (t1 == TYPE.I && t2 == TYPE.I) {
                    genOpCode(OP_CODE.SUB);
                    return TYPE.I;
                } else if (t1 == TYPE.I && t2 == TYPE.R) {
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FSUB);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.I) {
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FSUB);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.R) {
                    genOpCode(OP_CODE.FSUB);
                    return TYPE.R;
                }
            case "MULTIPLY":
                if (t1 == TYPE.I && t2 == TYPE.I) {
                    genOpCode(OP_CODE.MULT);
                    return TYPE.I;
                } else if (t1 == TYPE.I && t2 == TYPE.R) {
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FMULT);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.I) {
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FMULT);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.R) {
                    genOpCode(OP_CODE.FMULT);
                    return TYPE.R;
                }
            case "DIVIDE":
                if (t1 == TYPE.I && t2 == TYPE.I) {
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.FDIV);
                    return TYPE.R;
                } else if (t1 == TYPE.I && t2 == TYPE.R) {
                    genOpCode(OP_CODE.XCHG);
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FDIV);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.I) {
                    genOpCode(OP_CODE.CVR);
                    genOpCode(OP_CODE.FDIV);
                    return TYPE.R;
                } else if (t1 == TYPE.R && t2 == TYPE.R) {
                    genOpCode(OP_CODE.FDIV);
                    return TYPE.R;
                }
            case "DIV":
                if (t1 == TYPE.I && t2 == TYPE.I) {
                    genOpCode(OP_CODE.DIV);
                    return TYPE.I;
                }
            case "LESS_THAN":
                return emitBool(OP_CODE.LSS, t1, t2);
            case "GREATER_THAN":
                return emitBool(OP_CODE.GTR, t1, t2);
            case "LESS_THAN_EQUAL":
                return emitBool(OP_CODE.LEQ, t1, t2);
            case "GREATER_THAN_EQUAL":
                return emitBool(OP_CODE.GEQ, t1, t2);
            case "EQUAL":
                return emitBool(OP_CODE.EQL, t1, t2);
            case "NOT_EQUAL":
                return emitBool(OP_CODE.NEQL, t1, t2);
        }
        return null;
    }
    public static TYPE emitBool(OP_CODE pred, TYPE t1, TYPE t2) {
        if (t1 == t2) {
            genOpCode(pred);
            return TYPE.B;
        } else if (t1 == TYPE.I && t2 == TYPE.R) {
            genOpCode(OP_CODE.XCHG);
            genOpCode(OP_CODE.CVR);
            genOpCode(pred);
            return TYPE.B;
        } else if (t1 == TYPE.R && t2 == TYPE.I) {
            genOpCode(OP_CODE.CVR);
            genOpCode(pred);
            return TYPE.B;
        }
        return null;
    }
    public static void genOpCode(OP_CODE b){
        byteArray[ip++] = (byte)(b.ordinal());
    }
    public static void genAddress(int a){
        byte[] intBytes = ByteBuffer.allocate(ADDRESS_SIZE).putInt(a).array();
        for (byte b: intBytes) {
            byteArray[ip++] = b;
        }
    }
    public static void genAddress(float a){
        byte[] intBytes = ByteBuffer.allocate(ADDRESS_SIZE).putFloat(a).array();
        for (byte b: intBytes) {
            byteArray[ip++] = b;
        }
    }
    public static void getToken() {
        if (it.hasNext()) {
            currentToken =  it.next();
        }
    }
    public static void match(String tokenType) {
        if (!tokenType.equals(currentToken.getTokenType())) {
            throw new Error(String.format("Token type (%s) does not match current token type (%s)", tokenType, currentToken.getTokenType()));
        } else {
            getToken();
        }
    }
    public static TYPE getLitType(String tokenType) {
        switch (tokenType) {
            case "INTLIT":
                return TYPE.I;
            case "FLOATLIT":
                return TYPE.R;
            case "CHARLIT":
                return TYPE.C;
            case "BOOLLIT":
                return TYPE.B;
            default:
                return null;
        }
    }
    public static void beginParse(ArrayList<Token> tokenArrayList) {
        it = tokenArrayList.iterator();
    }
}