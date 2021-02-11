import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
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
        Map<String, String> outputAddressMap = new HashMap<>();
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
        HashSet<String> addresses = new HashSet<>();
        addresses.addAll(graph.getVertices());
        for (Object n : addresses) {
            String node = n.toString();
            if (outputAddressMap.containsKey(node)) {
                replaceVertex(graph, node, outputAddressMap.get(node));
                count++;
            }
        }
        System.out.println(count + " transaction outputs have been replaced with their addresses.");
        return graph;
    }

    void parseTransactions(DirectedSparseGraph<String, WeightedEdge> graph,
                           Map<String, String> outputAddressMap,
                           List<Transaction> transactions) {


        for (Transaction tr : transactions) {
            try {
                if (tr.isCoinBase()) {
                    for (TransactionOutput output : tr.getOutputs()) {
                        String address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
                        graph.addVertex(address);
                    }
                }
                List<TransactionInput> inputs = tr.getInputs();
                List<TransactionOutput> outputs = tr.getOutputs();
                String txNodeId = tr.getTxId().toString();
                graph.addVertex(txNodeId);
                for (TransactionInput input : inputs) {
                    String trInputNodeId = input.getParentTransaction().getTxId().toString() + "_" + input.getIndex();
                    graph.addEdge(new WeightedEdge(-1, ++edgeCount), trInputNodeId, txNodeId);
                }
                for (TransactionOutput output : outputs) {
                    String trOutputNodeId = txNodeId + "_" + output.getIndex();
                    String address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
                    graph.addEdge(new WeightedEdge(output.getValue().value, ++edgeCount), txNodeId, address);
                    outputAddressMap.put(trOutputNodeId, address);
                }
            } catch (ScriptException e) {
                System.out.println("Skipping " + tr.hashCode() + " due to script errors: " + tr.toString());
            }
        }
    }

    private void replaceVertex(DirectedGraph<String, WeightedEdge> graph, String node, String newNode) {
        graph.addVertex(newNode);
        for (WeightedEdge edge : graph.getInEdges(node)) {
            String inNeighbor = graph.getSource(edge);
            graph.addEdge(new WeightedEdge(edge.getAmount(), ++edgeCount), inNeighbor, newNode);
        }
        for (WeightedEdge edge : graph.getOutEdges(node)) {
            String outneighbor = graph.getSource(edge);
            graph.addEdge(new WeightedEdge(edge.getAmount(), ++edgeCount), newNode, outneighbor);
        }
        graph.removeVertex(node);
    }


}