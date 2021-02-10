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
    static String PREFIX = "C:/Users/etr/AppData/Roaming/Bitcoin/blocks/";
    static NetworkParameters np = new MainNetParams();
    // A simple method with everything in it
    public static void start() {
        // Just some initial setup

        Context.getOrCreate(MainNetParams.get());

        // We create a BlockFileLoader object by passing a list of files.
        // The list of files is built with the method buildList(), see
        // below for its definition.
        BlockFileLoader loader = new BlockFileLoader(np,lastFile());

        // We are going to store the results in a map of the form
        // day -> n. of transactions
        Map<String, Integer> dailyTotTxs = new HashMap<>();

        // A simple counter to have an idea of the progress
        int blockCounter = 0;

        // bitcoinj does all the magic: from the list of files in the loader
        // it builds a list of blocks. We iterate over it using the following
        // for loop

        for (Block block : loader) {

            // This gives you an idea of the progress
            //System.out.println("Analysing block " + blockCounter + "\t" + block.getHashAsString() + "\t" + block.getTime());

            // Extract the day from the block: we are only interested
            // in the day, not in the time. Block.getTime() returns
            // a Date, which is here converted to a string.
            String time = new SimpleDateFormat("yyyy-MM-dd HH:ss").format(block.getTime());
            String blockHash = block.getHashAsString();
            String parentBlock = block.getPrevBlockHash().toString();
            BlockInfoMap blockInfoMap = parseTransactions(block.getTransactions());
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
    private static BlockInfoMap parseTransactions(List<Transaction> transactions) {
        BlockInfoMap infoMap = new BlockInfoMap();
        for(Transaction tr:transactions){
            if(tr.isCoinBase()){
                for(TransactionOutput output: tr.getOutputs()){
                    String address =extractOutputAddress(output.getScriptPubKey());
                    infoMap.addMinerAddress(address);
                }
            }
           List<TransactionInput> inputs= tr.getInputs();
           List<TransactionOutput> outputs= tr.getOutputs();
           int inputSize= inputs.size();
           int outputSize= outputs.size();
            infoMap.addOccurrence(inputSize,outputSize);
            infoMap.addAmount(inputSize,outputSize,tr.getOutputSum());
            infoMap.addFee(tr.getFee());
        }
        return infoMap;
    }


    // The method returns a list of files in a directory according to a certain
    // pattern (block files have name blkNNNNN.dat)
    private static List<File> buildList() {
        List<File> list = new LinkedList<File>();
        for (int i = 0; true; i++) {
            File file = new File(PREFIX + String.format(Locale.US, "blk%05d.dat", i));
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
            File file = new File(PREFIX + String.format(Locale.US, "blk%05d.dat", i));
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