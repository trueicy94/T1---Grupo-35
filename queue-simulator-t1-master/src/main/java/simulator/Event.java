package simulator;

public class Event implements Comparable<Event> {

    public enum Type {
        CHEGADA, SAIDA, PASSAGEM
    }

    private final double time;   // instante do evento
    private final Type type;     // chegada, saída ou passagem
    private final int origem;    // -1 para “exterior”
    private final int destino;   // -1 para “exterior”

    public Event(double time, Type type, int origem, int destino) {
        this.time = time;
        this.type = type;
        this.origem = origem;
        this.destino = destino;
    }

    public double getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }

    public int getOrigem() {
        return origem;
    }

    public int getDestino() {
        return destino;
    }

    @Override
    public int compareTo(Event e) {
        return Double.compare(this.time, e.time);
    }
}