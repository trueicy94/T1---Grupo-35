# 🧪 Simulador de Rede de Filas

Este projeto é um simulador de rede de filas com múltiplos servidores e roteamento probabilístico, desenvolvido em **Java 21**. Ele utiliza eventos discretos para modelar o fluxo de clientes entre diferentes filas, com base em configurações externas definidas via YAML.

---

## 🛠️ Estrutura do Projeto

- **Linguagem**: Java 21  
- **Biblioteca externa**: [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) para leitura do arquivo YAML  
- **Arquivo de configuração**: `config.yml` (deve estar em `src/main/resources` ou no diretório raiz do projeto)

---

## 📁 Arquivo `config.yml`

O arquivo define os parâmetros da simulação:

### 🔧 Parâmetros globais

```yaml
seeds: [42, 123, 999]           # Lista de sementes para o gerador aleatório
rndnumbersPerSeed: 100000       # Número máximo de eventos por execução
Filas (queues):
Nome da fila
Número de servidores
Capacidade (opcional)
Intervalo de tempo de serviço (minService, maxService)
Intervalo de chegada externa (minArrival, maxArrival) — se aplicável
Rede (network):
Roteamento entre filas com probabilidades
EXIT indica saída do sistema
Chegadas iniciais (arrivals):
Nome da fila e tempo inicial da primeira chegada
