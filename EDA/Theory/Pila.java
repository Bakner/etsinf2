package EDA;

public interface Pila<E> {
    E pop() throws Exception;
    E top() throws Exception;
    void push(E element);
    boolean esVacía();
}