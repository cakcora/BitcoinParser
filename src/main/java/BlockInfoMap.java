import org.bitcoinj.core.Coin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockInfoMap {
    final int chainletDimension =20;
    Map<Integer, Map<Integer,Integer>> occurrenceHolder;
    Map<Integer, Map<Integer, Long>> amountHolder;
    static final int BTC = 10 ^ 8;

    List<Coin> fees;
    private final List<String> minerAddressList;
    private String blockHash;
    private String parentHash;
    private Timestamp blockTimestamp;
    private final HashMap<String, Long> whales;

    String printOccurrence(){
        StringBuffer bf = new StringBuffer();
        for(int i=1;i<=chainletDimension;i++){
            if(!occurrenceHolder.containsKey(i)) continue;
            for(int j=1;j<=chainletDimension;j++){
                if(occurrenceHolder.get(i).containsKey(j)) {
                    bf.append(i+"-"+j+":"+occurrenceHolder.get(i).get(j)+" ");
                }
            }
        }
        return bf.toString();
    }
    String printAmount(){
        StringBuffer bf = new StringBuffer();
        for(int i=1;i<=chainletDimension;i++){
            if(!amountHolder.containsKey(i)) continue;
            for(int o=1;o<=chainletDimension;o++){
                if(amountHolder.get(i).containsKey(o)) {
                    bf.append(i+"-"+o+":"+amountHolder.get(i).get(o).toString()+" ");
                }
            }
        }
        return bf.toString();
    }
    BlockInfoMap(){
        occurrenceHolder = new HashMap<>();
        amountHolder =  new HashMap<>();
        fees = new ArrayList<Coin>();
        minerAddressList = new ArrayList();
        whales = new HashMap<String, Long>();
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    int convertToChainletDimension(int i) {
        if (i > chainletDimension) {
            i = chainletDimension;
        }
        return i;
    }
    public void addOccurrence(int inputSize, int outputSize) {
        int i = convertToChainletDimension(inputSize);
        int o= convertToChainletDimension(outputSize);
        if(!occurrenceHolder.containsKey(i)){
            occurrenceHolder.put(i,new HashMap<>());
        }
        Map<Integer, Integer> occMapOfInput = occurrenceHolder.get(i);
        if(!occMapOfInput.containsKey(o)){
            occMapOfInput.put(o,0);
        }
        int count = occMapOfInput.get(o);
        occMapOfInput.put(o,count+1);
    }
    public void addAmount(int inputSize, int outputSize, Coin coin) {
        int i = convertToChainletDimension(inputSize);
        int o= convertToChainletDimension(outputSize);
        if(!amountHolder.containsKey(i)){
            amountHolder.put(i,new HashMap<>());
        }
        Map<Integer, Long> occMapOfInput = amountHolder.get(i);
        if(!occMapOfInput.containsKey(o)){
            occMapOfInput.put(o,coin.value);
        }
        else{
            long oldCoinAmount = occMapOfInput.get(o);
            occMapOfInput.put(o,coin.value+oldCoinAmount);
        }
    }

    public void addFee(Coin fee) {
        fees.add(fee);
    }

    public void addMinerAddress(String address) {
        this.minerAddressList.add(address);
    }

    public String getBlockHash() {
        return blockHash;
    }

    public Map<Integer, Map<Integer, Integer>> getOccurrences() {
        return occurrenceHolder;
    }

    public Map<Integer, Map<Integer, Long>> getAmounts() {
        return amountHolder;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setTimestamp(Timestamp time) {
        this.blockTimestamp = time;
    }

    public Timestamp getBlockTimeStamp() {
        return this.blockTimestamp;
    }

    public void addWhaleAmount(String address, long value) {
        if (!whales.containsKey(address)) {
            whales.put(address, 0L);
        }
        whales.put(address, whales.get(address) + value);
    }

    public int getWhaleCount() {
        return whales.size();
    }

    public int getChainletCount() {
        int count = 0;
        for (int i : occurrenceHolder.keySet()) {
            for (int j : occurrenceHolder.get(i).keySet()) {
                count += occurrenceHolder.get(i).get(j);
            }
        }
        return count;
    }

    public long getChainletAmount() {
        long sum = 0;
        for (int i : amountHolder.keySet()) {
            for (int j : amountHolder.get(i).keySet()) {
                sum += amountHolder.get(i).get(j);
            }
        }
        return sum;
    }

    public String[] getMaxWhale() {
        long max = 0;
        String whalest = null;
        for (String whale : whales.keySet()) {
            Long max1 = whales.get(whale);
            if (max1 > max) {
                whalest = whale;
                max = max1;
            }
        }


        return new String[]{whalest, String.valueOf(max)};
    }
}
