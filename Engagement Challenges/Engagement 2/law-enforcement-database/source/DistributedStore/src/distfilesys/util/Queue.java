package distfilesys.util;

public class Queue<E> {
    
    private int inicio;
    
    private int fim;
    
    private int total;
    
    private transient E elementos[];
    
    public Queue() {
        this(200);
    }
    
    @SuppressWarnings("unchecked")
    public Queue(int initialCapacity) {
        inicio = fim = 0;
        total = 0;
        elementos = (E[]) new Object[initialCapacity];
    }
    
    public boolean isEmpty() {
        if (total == 0)
            return true;
        return false;
    }
    
    public boolean isFull() {
        if (total == elementos.length)
            return true;
        return false;
    }
    
    public int size() {
        return total;
    }
    
    public void enqueue(E item) throws TooManyElementsException {
        if (isFull())
            throw new TooManyElementsException();
        
        elementos[fim] = item;
        fim = (fim + 1) % elementos.length;
        total++;
    }
    

    
    public E dequeue() throws NoSuchElementException {
        if (isEmpty())
            throw new NoSuchElementException();
        
        E item;
        item = elementos[inicio];
        elementos[inicio] = null;
        inicio = (inicio + 1) % elementos.length;
        total--;
        return item;
    }
}
