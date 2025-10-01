package simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fila {

    private int id;
    private String nome;

    private final int capacidade;
    private final int servidores;
    private final double tempoServicoMin;
    private final double tempoServicoMax;

    private double chegadaMin = 0.0;
    private double chegadaMax = 0.0;

    private int clientes = 0;
    private final Map<Integer, Double> tempoPorOcupacao = new HashMap<>();
    private int perdas = 0;

    private final List<Rota> rotas = new ArrayList<>();

    public Fila(int id, int capacidade, int servidores,
                double tMin, double tMax) {
        this.id = id;
        this.capacidade = capacidade;
        this.servidores = servidores;
        this.tempoServicoMin = tMin;
        this.tempoServicoMax = tMax;
        tempoPorOcupacao.put(0, 0.0);
    }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setChegadaMin(double chegadaMin) { this.chegadaMin = chegadaMin; }
    public void setChegadaMax(double chegadaMax) { this.chegadaMax = chegadaMax; }

    public int getId() { return id; }
    public int getClientes() { return clientes; }
    public int getServidores() { return servidores; }
    public Map<Integer, Double> getTempoPorOcupacao() { return tempoPorOcupacao; }
    public int getPerdas() { return perdas; }
    public List<Rota> getRotas() { return rotas; }

    public void addRota(Rota r) {
        rotas.add(r);
    }

    public boolean tentarEntrar() {
        if (capacidade != -1 && clientes >= capacidade) {
            perdas++;
            return false;
        }
        clientes++;
        tempoPorOcupacao.putIfAbsent(clientes, 0.0);
        return true;
    }

    public void sair() {
        if (clientes > 0) {
            clientes--;
            tempoPorOcupacao.putIfAbsent(clientes, 0.0);
        }
    }

    public void acumulaTempo(double elapsed) {
        tempoPorOcupacao.merge(clientes, elapsed, Double::sum);
    }

    public double sorteiaTempoServico(Gerador rng) {
        return tempoServicoMin + (tempoServicoMax - tempoServicoMin) * rng.next();
    }

    public double sorteiaTempoChegada(Gerador rng) {
        return chegadaMin + (chegadaMax - chegadaMin) * rng.next();
    }
}