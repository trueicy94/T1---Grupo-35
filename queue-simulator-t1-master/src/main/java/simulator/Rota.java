package simulator;

public class Rota {
    private final int destinoId;
    private final double probabilidade;

    public Rota(int destinoId, double probabilidade) {
        this.destinoId = destinoId;
        this.probabilidade = probabilidade;
    }

    public int destinoId() {
        return destinoId;
    }

    public double probabilidade() {
        return probabilidade;
    }
}