import java.util.Objects;

public class WeightedEdge {
    long amount;
    long id;

    WeightedEdge(long amount, long id) {
        this.amount = amount;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedEdge that = (WeightedEdge) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long getAmount() {
        return amount;
    }
}
