import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Primeira versao de um simulador de eventos discretos para uma fila G/G/c/K.
 *
 * K e a capacidade total do sistema: clientes esperando + clientes em atendimento.
 * O primeiro cliente chega em t = 3.0, conforme o enunciado.
 */
public class SimuladorFila {
    private enum TipoEvento { CHEGADA, SAIDA }

    private static final class Evento implements Comparable<Evento> {
        final double tempo;
        final TipoEvento tipo;

        Evento(double tempo, TipoEvento tipo) {
            this.tempo = tempo;
            this.tipo = tipo;
        }

        @Override
        public int compareTo(Evento outro) {
            return Double.compare(tempo, outro.tempo);
        }
    }

    /** Gerador congruente linear que devolve valores no intervalo [0, 1). */
    private static final class GeradorCongruenteLinear {
        private final long a;
        private final long c;
        private final long m;
        private long seed;
        private int usados;

        GeradorCongruenteLinear(long a, long c, long m, long seed) {
            this.a = a;
            this.c = c;
            this.m = m;
            this.seed = seed;
        }

        double nextRandom() {
            seed = (a * seed + c) % m;
            usados++;
            return (double) seed / m;
        }

        int getUsados() {
            return usados;
        }
    }

    private static double uniforme(GeradorCongruenteLinear gerador, double minimo, double maximo) {
        return minimo + (maximo - minimo) * gerador.nextRandom();
    }

    /**
     * Executa uma simulacao. O limite conta cada chamada a NextRandom(), inclusive
     * as chamadas usadas para gerar intervalos de chegada e tempos de atendimento.
     */
    private static void simular(String nome, int servidores, int capacidade,
                                double chegadaMin, double chegadaMax,
                                double atendimentoMin, double atendimentoMax,
                                int limiteAleatorios) {
        GeradorCongruenteLinear gerador = new GeradorCongruenteLinear(
                1_664_525L, 1_013_904_223L, 4_294_967_296L, 123_456_789L);
        PriorityQueue<Evento> escalonador = new PriorityQueue<>();
        double[] tempoPorEstado = new double[capacidade + 1];
        int clientesNoSistema = 0;
        int servidoresOcupados = 0;
        int perdas = 0;
        double relogio = 0.0;

        // A primeira chegada e fixa; por isso nao consome numero aleatorio.
        escalonador.add(new Evento(3.0, TipoEvento.CHEGADA));

        while (!escalonador.isEmpty() && gerador.getUsados() < limiteAleatorios) {
            Evento evento = escalonador.poll();
            tempoPorEstado[clientesNoSistema] += evento.tempo - relogio;
            relogio = evento.tempo;

            if (evento.tipo == TipoEvento.CHEGADA) {
                if (clientesNoSistema < capacidade) {
                    clientesNoSistema++;
                    if (servidoresOcupados < servidores && gerador.getUsados() < limiteAleatorios) {
                        servidoresOcupados++;
                        double atendimento = uniforme(gerador, atendimentoMin, atendimentoMax);
                        escalonador.add(new Evento(relogio + atendimento, TipoEvento.SAIDA));
                    }
                } else {
                    perdas++;
                }

                if (gerador.getUsados() < limiteAleatorios) {
                    double intervalo = uniforme(gerador, chegadaMin, chegadaMax);
                    escalonador.add(new Evento(relogio + intervalo, TipoEvento.CHEGADA));
                }
            } else { // SAIDA
                clientesNoSistema--;
                servidoresOcupados--;
                // Se ha alguem esperando, ele inicia o atendimento imediatamente.
                if (clientesNoSistema >= servidores && gerador.getUsados() < limiteAleatorios) {
                    servidoresOcupados++;
                    double atendimento = uniforme(gerador, atendimentoMin, atendimentoMax);
                    escalonador.add(new Evento(relogio + atendimento, TipoEvento.SAIDA));
                }
            }
        }

        System.out.println("\n=== " + nome + " ===");
        System.out.printf("Aleatorios utilizados: %d%n", gerador.getUsados());
        System.out.printf("Tempo global da simulacao: %.6f%n", relogio);
        System.out.printf("Clientes perdidos: %d%n%n", perdas);
        System.out.println("Estado\tTempo acumulado\tProbabilidade");
        for (int estado = 0; estado <= capacidade; estado++) {
            double probabilidade = relogio == 0.0 ? 0.0 : tempoPorEstado[estado] / relogio;
            System.out.printf(Locale.US, "%d\t%.6f\t\t%.8f%n",
                    estado, tempoPorEstado[estado], probabilidade);
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        final int aleatorios = 100_000;

        simular("G/G/1/5", 1, 5, 3.0, 5.0, 4.0, 5.0, aleatorios);
        simular("G/G/2/5", 2, 5, 3.0, 5.0, 4.0, 5.0, aleatorios);
    }
}
