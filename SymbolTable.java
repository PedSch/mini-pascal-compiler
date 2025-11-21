public class SymbolTable {

    static class Scope {
        Symbol[] symbolTable = new Symbol[HASH_TABLE_SIZE]; //symbol table for the current scope
        Scope next = null; //pointer to the next outer scope
    }

     static final int HASH_TABLE_SIZE = 211;
     static Scope S = new Scope();

    public static int hash(String symbolName) {
        int h = 0;
        for (int i = 0; i < symbolName.length(); i++) {
            h = h + h + symbolName.charAt(i);
        }
        h = h % HASH_TABLE_SIZE;
        return h;
    }

    public static void insert(Symbol symbol) {
        int hashValue = hash(symbol.getName());

        Symbol current = S.symbolTable[hashValue];
        if (current == null) {
            S.symbolTable[hashValue] = symbol;
        } else {
            while (current.next != null) {
                current = current.next;
            }
            current.next = symbol;
        }
    }

    public static Symbol lookupS(String symbolName) {
        int hashValue = hash(symbolName);
        Symbol current = S.symbolTable[hashValue];
        Scope scopeCursor = S;
        while (scopeCursor != null) {
            while (current != null) {
                if (current.getName().equals(symbolName)) {
                    return current;
                }
                current = current.next;
            }
            scopeCursor = scopeCursor.next;
        }
        return null;
    }

    public static void addS() {
        Scope innerScope = new Scope();
        innerScope.next = S;
        S = innerScope;
    }

    public static void removeS() {
        S = S.next;
    }

    public static Scope getS() {
        return S;
    }
}