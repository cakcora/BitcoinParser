import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.utils.BlockFileLoader;

public class Parser {

    // Location of block files. This is where your blocks are located.
    // Check the documentation of Bitcoin Core if you are using
    // it, or use any other directory with blk*dat files.
    static String BitcoinBlockDirectory = "C:/Users/etr/AppData/Roaming/Bitcoin/blocks/";
    static NetworkParameters np = new MainNetParams();
    // A simple method with everything in it
    public static void start() {
        // Just some initial setup

        Context.getOrCreate(MainNetParams.get());

        // We create a BlockFileLoader object by passing a list of files.
        // The list of files is built with the method buildList(), see
        // below for its definition.
        // extract latest blocks
        BlockFileLoader loader = new BlockFileLoader(np,lastFile());
        // or extract all blocks
        //BlockFileLoader loader = new BlockFileLoader(np,buildList());

        // bitcoinj does all the magic: from the list of files in the loader
        // it builds a list of blocks. We iterate over it using the following
        // for loop

        for (Block block : loader) {

            // This gives you an idea of the progress
            //System.out.println("Analysing block " + blockCounter + "\t" + block.getHashAsString() + "\t" + block.getTime());

            // Extract the day from the block: we are only interested
            // in the day, not in the timeParser. Block.getTime() returns
            // a Date, which is here converted to a string.
            String time = new SimpleDateFormat("yyyy-MM-dd HH:ss").format(block.getTime());
            String blockHash = block.getHashAsString();
            String parentBlock = block.getPrevBlockHash().toString();
            BlockInfoMap blockInfoMap = ParserWithDatabase.parseTransactions(block.getTransactions());
            System.out.println(time+" "+blockHash+" "+blockInfoMap.printOccurrence());
            System.out.println(time+" "+blockHash+" "+blockInfoMap.printAmount());

        } // End of iteration over blocks
    }

    private static String extractOutputAddress(Script script){

        String address;
        if (ScriptPattern.isP2PKH(script) || ScriptPattern.isP2WPKH(script)
                || ScriptPattern.isP2SH(script))
            address= script.getToAddress(np).toString();
        else if (ScriptPattern.isP2PK(script))
            address=byteArrayToHex(ScriptPattern.extractKeyFromP2PK(script));
        else if (ScriptPattern.isSentToMultisig(script))
            address="unknownmultisig";
        else
            address="unknown";
        return address;
    }
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // The method returns a list of files in a directory according to a certain
    // pattern (block files have name blkNNNNN.dat)
    private static List<File> buildList() {
        List<File> list = new LinkedList<File>();
        for (int i = 0; true; i++) {
            File file = new File(BitcoinBlockDirectory + String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists()){
                System.out.println(file.toString()+" does not exist");
                break;
            }
            list.add(file);
        }
        return list;
    }
    private static List<File> lastFile() {
        File lastFile = null;
        for (int i = 0; true; i++) {
            File file = new File(BitcoinBlockDirectory + String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists()){
                System.out.println(file.toString()+" does not exist");
                break;
            }
            lastFile=file;
        }
        ArrayList<File> f=new ArrayList<File>();
        f.add(lastFile);
        return f;
    }

    // Main method: simply invoke everything
    public static void main(String[] args) {
        start();
    }

}