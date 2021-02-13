public class WeightedEdge {
    Output output;
    long edgeId;

    public WeightedEdge(Output output, long id) {
        this.edgeId = id;
        this.output = output;
    }

    public long getValue() {
        return output.getValue();
    }

    public void setValue(long value) {
        this.output.setValue(value);
    }

    public String getKey(char ch) {
        return output.getTxHashId() + ch + output.getIndex();
    }

    @Override
    public String toString() {
        return "WeightedEdge{" +
                "output=" + output.toString() +
                '}';
    }

    public Output getOutput() {
        return output;
    }
}
