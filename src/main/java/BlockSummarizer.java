import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.utils.BlockFileLoader;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockSummarizer {

    static final int BTC = 100000000;
    // Location of block files. This is where your blocks are located.
    // Check the documentation of Bitcoin Core if you are using
    // it, or use any other directory with blk*dat files.
    static String BitcoinBlockDirectory = "C:/Users/etr/AppData/Roaming/Bitcoin/blocks/";
    static NetworkParameters np = new MainNetParams();

    // A simple method with everything in it
    public static Block getLastBlock() throws SQLException, ClassNotFoundException {
        // Just some initial setup

        Context.getOrCreate(MainNetParams.get());

        BlockFileLoader loader = new BlockFileLoader(np, ChainletExtractor.lastFile());

        var blockLineage = new HashMap<String, String>();
        var blockMap = new HashMap<String, Block>();
        for (Block block : loader) {
            String blockHash = block.getHashAsString();
            String parentHash = block.getPrevBlockHash().toString();
            blockLineage.put(parentHash, blockHash);
            blockMap.put(blockHash, block);
        } // End of iteration over blocks

        Set<String> objects = blockLineage.keySet();
        Iterator<String> iterator = objects.iterator();
        String aBlock = iterator.next();
        while (blockLineage.containsKey(aBlock)) {
            aBlock = blockLineage.get(aBlock);
        }
        return blockMap.get(aBlock);
    }


    static BlockInfoMap parseTransactions(List<Transaction> transactions) {
        BlockInfoMap infoMap = new BlockInfoMap();

        for (Transaction tr : transactions) {
            try {
                List<TransactionInput> inputs = tr.getInputs();
                List<TransactionOutput> outputs = tr.getOutputs();
                if (tr.isCoinBase()) {
                    for (TransactionOutput output : tr.getOutputs()) {
                        String address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
                        infoMap.addMinerAddress(address);
                    }
                }
                for (TransactionOutput output : tr.getOutputs()) {

                    long value = output.getValue().getValue();
                    if (value > BTC) {
                        infoMap.addWhaleAmount(ChainletExtractor.extractOutputAddress(output.getScriptPubKey()), value);
                    }
                }

                int inputSize = inputs.size();
                int outputSize = outputs.size();
                infoMap.addOccurrence(inputSize, outputSize);
                infoMap.addAmount(inputSize, outputSize, tr.getOutputSum());
                infoMap.addFee(tr.getFee());
            } catch (ScriptException e) {
                System.out.println("Skipping " + tr.hashCode() + " due to script errors: " + tr.toString());
            }
        }
        return infoMap;
    }


    // Main method: simply invoke everything
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {

        String apikey = args[0];
        String apisecretkey = args[1];
        String accesstoken = args[2];
        String accesstokensecret = args[3];
        ConfigurationBuilder twitterConfiguration = new ConfigurationBuilder();
        twitterConfiguration.setDebugEnabled(true)
                .setOAuthConsumerKey(apikey)
                .setOAuthConsumerSecret(apisecretkey)
                .setOAuthAccessToken(accesstoken)
                .setOAuthAccessTokenSecret(accesstokensecret);
        Configuration build = twitterConfiguration.build();
        Block lastBlock = null;
        while (true) {

            Block newBlock = getLastBlock();
            if (!newBlock.equals(lastBlock)) {
                lastBlock = newBlock;
                BlockInfoMap infoMap = parseTransactions(Objects.requireNonNull(lastBlock.getTransactions()));
                String[] maxWhale = infoMap.getMaxWhale();
                double chainletAmount = infoMap.getChainletAmount() * 1.0 / BTC;
                double val = Double.parseDouble(maxWhale[1]) / (BTC);
                String s = lastBlock.getHash().toString();
                int i = 0;
                while (s.charAt(i) == '0') i++;
                s = "00.." + s.substring(i);
                String receiver = val + " BTC by the address " + maxWhale[0] + ".";
                if (maxWhale[0].equals("unknown"))
                    receiver = val + " BTC.";
                String content = "New Bitcoin block " + s + ": " +
                        infoMap.getChainletCount() + " transactions transferred " +
                        "a total of " + chainletAmount +
                        " BTC and " +
                        infoMap.getWhaleCount() + " addresses each received 1BTC or more. Max received amount was " +
                        receiver;
                System.out.println(content);
                Tweeter tweeter = new Tweeter();
                if (content.length() < 280) {
                    try {
                        tweeter.postTweet(content, build);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }
            TimeUnit.MINUTES.sleep(2);

        }

    }


}