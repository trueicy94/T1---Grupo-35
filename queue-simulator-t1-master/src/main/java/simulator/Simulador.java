
package simulator;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

public class Simulador {

    private double tempoGlobal = 0.0;
    private final PriorityQueue<Event> agenda = new PriorityQueue<>();
    private final ArrayList<Fila> filas = new ArrayList<>();
    private final Map<String, Integer> nomeParaId = new HashMap<>();
    private int maxEventos = 10;

    private Gerador gerador;

    public void setGerador(Gerador gerador) {
        this.gerador = gerador;
    }


    public void addFila(String nome, Fila f) {
        nomeParaId.put(nome, filas.size());
        f.setId(filas.size());
        filas.add(f);
    }

    public void agendarChegadaExterna(int idFila, double tempoInicial) {
        agenda.add(new Event(tempoInicial, Event.Type.CHEGADA, -1, idFila));
    }

    public void executar() {
        while (!agenda.isEmpty() && gerador.getUsados() < maxEventos) {
            Event ev = agenda.poll();

            double elapsed = ev.getTime() - tempoGlobal;

            for (Fila f : filas) {
                f.acumulaTempo(elapsed);
            }


            tempoGlobal = ev.getTime();

            switch (ev.getType()) {
                case CHEGADA -> chegada(ev);
                case SAIDA -> saida(ev);
                case PASSAGEM -> passagem(ev);
            }
        }
    }


    private void passagem(Event e) {
        Fila destino = filas.get(e.getDestino());

        if (destino.tentarEntrar()) {
            if (destino.getClientes() <= destino.getServidores()) {
                agenda.add(new Event(
                        tempoGlobal + destino.sorteiaTempoServico(gerador),
                        Event.Type.SAIDA,
                        destino.getId(),
                        destino.getId()
                ));
            }
        }
    }

    private void chegada(Event e) {
        Fila f = filas.get(e.getDestino());
        if (f.tentarEntrar()) {
            if (f.getClientes() <= f.getServidores()) {
                agenda.add(new Event(
                        tempoGlobal + f.sorteiaTempoServico(gerador),
                        Event.Type.SAIDA, f.getId(), f.getId()
                ));
            }
        }
        if (e.getOrigem() == -1) {
            double dt = f.sorteiaTempoChegada(gerador);
            agenda.add(new Event(tempoGlobal + dt, Event.Type.CHEGADA, -1, f.getId()));
        }
    }

    private void saida(Event e) {
        Fila origem = filas.get(e.getOrigem());
        origem.sair();

        double u = gerador.next();
        double acumulado = 0.0;
        for (Rota rota : origem.getRotas()) {
            acumulado += rota.probabilidade();
            if (u < acumulado) {
                int destinoId = rota.destinoId();
                if (destinoId >= 0) {
                    agenda.add(new Event(
                            tempoGlobal,
                            Event.Type.PASSAGEM,
                            origem.getId(),
                            destinoId
                    ));
                }
                break;
            }
        }

        if (origem.getClientes() >= origem.getServidores()) {
            agenda.add(new Event(
                    tempoGlobal + origem.sorteiaTempoServico(gerador),
                    Event.Type.SAIDA,
                    origem.getId(),
                    origem.getId()
            ));
        }
    }


    public void carregarConfigComSeed(Map<String, Object> root, int seed) {
        this.maxEventos = ((Number) root.get("rndnumbersPerSeed")).intValue();

        Map<String, Object> arrivals = (Map<String, Object>) root.get("arrivals");
        Map<String, Map<String, Object>> queues = (Map<String, Map<String, Object>>) root.get("queues");

        for (Map.Entry<String, Map<String, Object>> entry : queues.entrySet()) {
            String nome = entry.getKey();
            Map<String, Object> props = entry.getValue();

            int servidores = ((Number) props.get("servers")).intValue();
            int capacidade = props.containsKey("capacity") ? ((Number) props.get("capacity")).intValue() : -1;
            double servicoMin = ((Number) props.get("minService")).doubleValue();
            double servicoMax = ((Number) props.get("maxService")).doubleValue();

            Fila fila = new Fila(-1, capacidade, servidores, servicoMin, servicoMax);

            if (props.containsKey("minArrival") && props.containsKey("maxArrival")) {
                double chegadaMin = ((Number) props.get("minArrival")).doubleValue();
                double chegadaMax = ((Number) props.get("maxArrival")).doubleValue();
                fila.setChegadaMin(chegadaMin);
                fila.setChegadaMax(chegadaMax);
            }

            fila.setNome(nome);
            addFila(nome, fila);
        }

        List<Map<String, Object>> network = (List<Map<String, Object>>) root.get("network");
        for (Map<String, Object> rota : network) {
            String origem = (String) rota.get("source");
            String destino = (String) rota.get("target");
            double prob = ((Number) rota.get("probability")).doubleValue();

            int origemId = nomeParaId.get(origem);
            int destinoId = destino.equals("EXIT") ? -1 : nomeParaId.get(destino);
            filas.get(origemId).addRota(new Rota(destinoId, prob));
        }

        for (Map.Entry<String, Object> entry : arrivals.entrySet()) {
            String nome = entry.getKey();
            double tempoInicial = ((Number) entry.getValue()).doubleValue();
            agendarChegadaExterna(nomeParaId.get(nome), tempoInicial);
        }

        long a = 96;
        long c = 6000;
        double m = 74644981083.0;
        Gerador gerador = new Gerador(a, c, m, seed);
        this.setGerador(gerador);
    }

    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();
        InputStream in = Simulador.class.getResourceAsStream("/config.yml");
        Map<String, Object> root = yaml.load(in);

        List<Integer> seeds = (List<Integer>) root.get("seeds");
        int numFilas = ((Map<String, Object>) root.get("queues")).size();

        List<Map<Integer, Double>> acumuladosPorFila = new ArrayList<>();
        List<Integer> perdasPorFila = new ArrayList<>();
        for (int i = 0; i < numFilas; i++) {
            acumuladosPorFila.add(new HashMap<>());
            perdasPorFila.add(0);
        }

        for (int seed : seeds) {
            Simulador sim = new Simulador();
            sim.carregarConfigComSeed(root, seed);
            sim.executar();

            System.out.printf("Seed %d - Tempo total da simulação: %.4f%n", seed, sim.tempoGlobal);
            for (int i = 0; i < sim.filas.size(); i++) {
                Fila f = sim.filas.get(i);
                Map<Integer, Double> acumulado = acumuladosPorFila.get(i);
                for (Map.Entry<Integer, Double> entry : f.getTempoPorOcupacao().entrySet()) {
                    acumulado.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
                perdasPorFila.set(i, perdasPorFila.get(i) + f.getPerdas());
            }
        }

        System.out.println("============================================================");
        for (int i = 0; i < acumuladosPorFila.size(); i++) {
            Map<Integer, Double> tempos = acumuladosPorFila.get(i);
            double total = tempos.values().stream().mapToDouble(Double::doubleValue).sum();

            System.out.printf("Fila %d - Tempo total: %.4f%n", i, total);
            for (Map.Entry<Integer, Double> entry : tempos.entrySet()) {
                double prob = (entry.getValue() / total) * 100.0;
                System.out.printf("Estado %d: Tempo %.4f, Probabilidade %.2f%%%n", entry.getKey(), entry.getValue(), prob);
            }
            System.out.printf("Perdas: %d%n", perdasPorFila.get(i));
            System.out.println("--------------------------------------------------");
        }
    }
}