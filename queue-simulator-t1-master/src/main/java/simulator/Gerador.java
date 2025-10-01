package simulator;

import java.util.ArrayList;
import java.util.List;

public class Gerador {

    private final long a;
    private final long c;
    private final double m;
    private double semente;

    private final List<Double> predefinidos = new ArrayList<>();
    private int indice = 0;

    private long usados = 0; // contador

    public Gerador(long a, long c, double m, double semente) {
        this.a = a;
        this.c = c;
        this.m = m;
        this.semente = semente;
    }

    public Double next() {
        double valor;
        if (indice < predefinidos.size()) {
            valor = predefinidos.get(indice++);
        } else {
            semente = (a * semente + c) % m;
            valor = semente / m;
        }
        usados++;
        return valor;
    }

    public long getUsados() {
        return usados;
    }
}
