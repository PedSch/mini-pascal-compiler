import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public final class Driver {
    public static void main(String[] args) throws FileNotFoundException {
        //Creates array list and then also begins lex analysis
        ArrayList<Token> tokenArrayList = LexAnalyzer.scan(new File(args[0]));
        //begins parser witht he array list
        Parser.beginParse(tokenArrayList);
        //instructions stored in a byte array
        Byte[] instructions = Parser.parse();
        //Sends byte array to code genertor to generate P-code
        CodeGenerator.setInstructions(instructions);
        CodeGenerator.simulate();
    }
}