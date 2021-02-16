import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.utils.BlockFileLoader;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.bitcoinj.core.Context.getOrCreate;

public class GraphExtractor {

    // Location of block files. This is where your blocks are located.
    // Check the documentation of Bitcoin Core if you are using
    // it, or use any other directory with blk*dat files.
    static String BitcoinBlockDirectory = "C:/Users/etr/AppData/Roaming/Bitcoin/blocks/";
    static NetworkParameters np = new MainNetParams();
    long edgeCount = 0;
    char keySepChar = '_';
    private static List<File> lastFiles(int datNumberId) {
        ArrayList<File> f = new ArrayList<>();
        for (int i = datNumberId; true; i++) {
            File file = new File(BitcoinBlockDirectory + String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists()) {
                System.out.println(file.toString() + " does not exist (this is normal).");
                break;
            }
            f.add(file);
        }


        return f;
    }

    // A simple method with everything in it
    public Graph extractGraphFor(LocalDateTime historyStartsFrom, LocalDateTime historyEndsAt, int datFileHint) throws SQLException, ClassNotFoundException {
        // Just some initial setup

        getOrCreate(MainNetParams.get());

        if (datFileHint < 0)
            datFileHint = 0;
        BlockFileLoader loader = new BlockFileLoader(np, lastFiles(datFileHint));

        // bitcoinj does all the magic: from the list of files in the loader
        // it builds a list of blocks. We iterate over it using the following
        // for loop
        DirectedSparseGraph<String, WeightedEdge> graph = new DirectedSparseGraph();
        Map<String, Output> outputAddressMap = new HashMap<>();
        for (Block block : loader) {

            // This gives you an idea of the progress
            //System.out.println("Analysing block " + blockCounter + "\t" + block.getHashAsString() + "\t" + block.getTime());

            LocalDateTime blockTime = block.getTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            if (historyStartsFrom != null && blockTime.compareTo(historyStartsFrom) < 0) {
                //we do not care about earlier blocks
                continue;
            }
            if (blockTime.compareTo(historyEndsAt) > 0) {
                continue;
            }
            // we will both create graph and keep a map of output->address
            parseTransactions(graph, outputAddressMap, Objects.requireNonNull(block.getTransactions()));
        } // End of iteration over blocks


        int count = 0;
        HashSet<String> addresses = new HashSet<>(graph.getVertices());
        for (String node : addresses) {
            if (outputAddressMap.containsKey(node)) {
                replaceVertex(graph, node, outputAddressMap.get(node));
                count++;
            }
        }
        System.out.println(count + " transaction outputs have been replaced with their addresses. Graph order is " + graph.getVertexCount());
        return graph;
    }

    void parseTransactions(DirectedSparseGraph<String, WeightedEdge> graph,
                           Map<String, Output> outputAddressMap,
                           List<Transaction> transactions) {


        for (Transaction tr : transactions) {
            try {
                List<TransactionInput> inputs = tr.getInputs();
                List<TransactionOutput> outputs = tr.getOutputs();
                String txNodeId = tr.getTxId().toString();
                graph.addVertex(txNodeId);

                if (tr.isCoinBase()) {
                    txNodeId = "CB_" + txNodeId;
                    for (TransactionOutput output : tr.getOutputs()) {
                        String address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
                        graph.addVertex(address);
                    }
                } else {
                    for (TransactionInput input : inputs) {
                        String prevTxHash = input.getOutpoint().getHash().toString();
                        long index = input.getOutpoint().getIndex();

                        String trInputNodeId = prevTxHash + keySepChar + index;
                        Output promised = new Output(prevTxHash, (int) index);
                        WeightedEdge edge = new WeightedEdge(promised, ++edgeCount);
                        graph.addEdge(edge, trInputNodeId, txNodeId);
                    }
                }

                for (TransactionOutput output : outputs) {
                    String trOutputNodeId = txNodeId + keySepChar + output.getIndex();
                    String address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
                    Coin value = output.getValue();
                    Output promised = new Output(output);
                    graph.addEdge(new WeightedEdge(promised, ++edgeCount), txNodeId, address);
                    Output otp = new Output(txNodeId, address, output.getIndex(), value);
                    outputAddressMap.put(trOutputNodeId, otp);
                }
            } catch (ScriptException e) {
                System.out.println("Skipping " + tr.hashCode() + " due to script errors: " + tr.toString());
            }
        }
    }

    private void replaceVertex(DirectedGraph<String, WeightedEdge> graph, String node, Output output) {
        String newNode = output.getAddress();
        graph.addVertex(newNode);
        Collection<WeightedEdge> e = graph.getOutEdges(node);
        if (e.size() > 1)
            System.out.println("Error: One output seems to be spent more than once: " + output.toString());
        WeightedEdge ed = e.iterator().next();
        long value = output.getValue();
        Output o2 = ed.getOutput();
        o2.setValue(value);
        o2.setAddress(newNode);
        Pair<String> endpoints = graph.getEndpoints(ed);
        String txAddress = endpoints.getSecond();
        WeightedEdge newEdge = new WeightedEdge(o2, ++edgeCount);
        graph.addEdge(newEdge, newNode, txAddress);
        graph.removeVertex(node);
    }


}