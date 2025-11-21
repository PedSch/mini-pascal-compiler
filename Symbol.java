public class Symbol {
    String name = "";
    String tokenType = "";
    Parser.TYPE dataType = null;
    int address;
    int returnAddress; //return address
    Object low; //Low array value
    Object high; //High Array Value
    Parser.TYPE indexType; //index type for array
    Parser.TYPE valueType; //value type for array
    Symbol next; //Next Symbol

    public Symbol(String name, String tokenType, Parser.TYPE dataType, int address){
        this.name = name;
        this.tokenType = tokenType;
        this.dataType = dataType;
        this.address = address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getReturnAddress() {
        return returnAddress;
    }

    public void setIndexType(Parser.TYPE indexType) {
        this.indexType = indexType;
    }

    public Parser.TYPE getValueType() {
        return valueType;
    }

    public void setReturnAddress(int returnAddress) {
        this.returnAddress = returnAddress;
    }

    public Object getLow() {
        return low;
    }

    public void setLow(Object low) {
        this.low = low;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parser.TYPE getDataType() {
        return dataType;
    }

    public int getAddress() {
        return address;
    }

    public Object getHigh() {
        return high;
    }

    public void setHigh(Object high) {
        this.high = high;
    }

    public Parser.TYPE getIndexType() {
        return indexType;
    }

    public void setValueType(Parser.TYPE valueType) {
        this.valueType = valueType;
    }
}