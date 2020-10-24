public class Triplet<U, V, W>{
    /* simple class to store 3 values of any type */
    private U first;
    private V second;
    private W third;
    
    public Triplet(U first, V second, W third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public U getFirst() {
        return this.first;
    }

    public V getSecond() {
        return this.second;
    }

    public W getThird() {
        return this.third;
    }

    @Override
    public String toString () {
        return "( " + this.first + " ; "+ this.second + " ; " + this.third + " )";
    }
}