# m4-simulation

Primeira versao de simulador de fila por eventos discretos para a atividade M4.

```bash
javac SimuladorFila.java
java SimuladorFila
```

O programa executa os cenarios G/G/1/5 e G/G/2/5, com chegadas uniformes em
[3, 5], atendimento uniforme em [4, 5], primeira chegada no tempo 3.0 e limite
de 100.000 numeros pseudoaleatorios. Nesta versao, `K` e a capacidade total do
sistema (clientes em espera e em atendimento).
