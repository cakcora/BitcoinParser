import org.bitcoinj.core.Coin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockInfoMap {
    final int chainletDimension =20;
    Map<Integer, Map<Integer,Integer>> occurrenceHolder;
    Map<Integer, Map<Integer, Long>>  amountHolder;

    List<Coin> fees;
    private List<String> minerAddressList;

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
    }
    int convertToChainletDimension(int i){
        if(i>chainletDimension){
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
}
