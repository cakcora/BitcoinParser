import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionOutput;

import java.util.Objects;

public class Output {
    private final String txHashId;
    private final int index;
    private long value;
    private String address;

    public Output(String txNodeId, String address, int index, Coin value) {
        this.txHashId = txNodeId;
        this.address = address;
        this.index = index;
        this.value = value.getValue();
    }

    public Output(String parentTxId, int parentIndex) {
        this.txHashId = parentTxId;
        this.index = parentIndex;
    }

    public Output(TransactionOutput output) {
        this.txHashId = output.getParentTransaction().getTxId().toString();
        this.value = output.getValue().getValue();
        this.index = output.getIndex();
        this.address = ChainletExtractor.extractOutputAddress(output.getScriptPubKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Output output = (Output) o;
        return index == output.index && Objects.equals(txHashId, output.txHashId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txHashId, index);
    }

    public String getAddress() {
        return address;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getTxHashId() {
        return txHashId;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Output{" +
                "txHashId='" + txHashId + '\'' +
                ", index=" + index +
                ", value=" + value +
                ", address='" + address + '\'' +
                '}';
    }
}
