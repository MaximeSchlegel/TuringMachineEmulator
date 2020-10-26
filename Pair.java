public class Pair<U, V> {
    /* simple class to store a pair of value of any type */
    private U first;
    private V second;
    
    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return this.first;
    }

    public V getSecond() {
        return this.second;
    }

    @Override
    public String toString () {
        return "( " + this.first + " ; " + this.second + " )"; 
    }
}