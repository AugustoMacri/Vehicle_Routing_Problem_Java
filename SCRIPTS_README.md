# Scripts de Execução VRP

Este diretório contém scripts para automatizar a execução do algoritmo genético em múltiplas instâncias do Solomon benchmark.

## 📁 Arquivos

- **`run_all_instances.sh`**: Executa todas as 26 instâncias especificadas automaticamente
- **`run_single_instance.sh`**: Executa uma única instância específica
- **`generate_route_maps.sh`**: Gera mapas visuais das rotas dos veículos

## 🚀 Uso

### Executar todas as instâncias

```bash
./run_all_instances.sh
```

Este script irá:
1. Compilar o projeto automaticamente
2. Executar o algoritmo para todas as 26 instâncias (C, R e RC)
3. Salvar os resultados em `resultsMulti/` com nomes padronizados

### Executar uma instância específica

```bash
./run_single_instance.sh <numero_da_instancia>
```

**Exemplos:**
```bash
./run_single_instance.sh 1   # Executa C101
./run_single_instance.sh 18  # Executa R101
./run_single_instance.sh 41  # Executa RC101
```

### Gerar mapas de rotas

Após executar uma ou mais instâncias, você pode gerar visualizações das rotas:

```bash
./generate_route_maps.sh <instance_name>
```

**Exemplos:**
```bash
./generate_route_maps.sh c101      # Gera mapas para C101
./generate_route_maps.sh r101      # Gera mapas para R101
./generate_route_maps.sh all_c1    # Gera mapas para todas C1 (C101-C109)
./generate_route_maps.sh all_r1    # Gera mapas para todas R1 (R101-R109)
./generate_route_maps.sh all_rc1   # Gera mapas para todas RC1 (RC101-RC108)
```

**Workflow completo:**
```bash
# 1. Executar instância
./run_single_instance.sh 1

# 2. Gerar mapas
./generate_route_maps.sh c101

# 3. Ver resultados em:
#    - resultsMulti/evo_c101.txt (dados numéricos)
#    - resultsMulti/route_maps/C101/route_maps/*.png (mapas visuais)
```

## 📊 Instâncias Disponíveis

### Cluster-based (C)
- 1: C101
- 2: C102
- 3: C103
- 4: C104
- 5: C105
- 6: C106
- 7: C107
- 8: C108
- 9: C109

### Random (R)
- 18: R101
- 19: R102
- 20: R103
- 21: R104
- 22: R105
- 23: R106
- 24: R107
- 25: R108
- 26: R109

### Random-Cluster (RC)
- 41: RC101
- 42: RC102
- 43: RC103
- 44: RC104
- 45: RC105
- 46: RC106
- 47: RC107
- 48: RC108

## 📂 Estrutura de Saída

Os resultados são salvos em:

```
resultsMulti/
├── evo_c101.txt      # Evolução fitness C101
├── evo_c102.txt      # Evolução fitness C102
├── ...
├── evo_rc108.txt     # Evolução fitness RC108
├── stats/
│   ├── stats_c101.txt    # Estatísticas C101
│   ├── stats_c102.txt    # Estatísticas C102
│   ├── ...
│   └── stats_rc108.txt   # Estatísticas RC108
└── route_maps/
    ├── C101/
    │   └── route_maps/
    │       ├── route_map_c101_initial.png
    │       └── route_map_c101_final.png
    ├── C102/
    │   └── route_maps/
    │       ├── route_map_c102_initial.png
    │       └── route_map_c102_final.png
    └── ...
```

## ⚙️ Formato dos Arquivos de Saída

### Arquivo de Evolução (`evo_*.txt`)

**Seção 1: Tabela de Fitness**
Contém a evolução do fitness a cada 100 gerações:
- subPopDistance
- subPopTime
- subPopFuel
- subPopPonderation

**Seção 2: Rotas Iniciais**
Detalhamento das rotas antes da evolução:
```
ROTAS INICIAIS (Antes da Evolução)
================================================================================

Veículo 0: Depósito(0) -> Cliente(5) -> Cliente(75) -> ... -> Depósito(0)
    Clientes: 12 | Demanda: 180/200 | Distância: 91.39

Veículo 1: Depósito(0) -> Cliente(57) -> Cliente(55) -> ... -> Depósito(0)
    Clientes: 8 | Demanda: 200/200 | Distância: 115.07
...
Total de veículos usados: 10
Distância total: 1034.13
================================================================================
```

**Seção 3: Rotas Finais**
Detalhamento das rotas após 3000 gerações (mesmo formato da Seção 2)

### Arquivo de Estatísticas (`stats_*.txt`)
Contém estatísticas finais:
- Melhor Fitness
- Fitness Médio
- Desvio Padrão

### Mapas de Rotas (`.png`)
Visualizações gráficas das rotas:
- **Initial**: Rotas antes da evolução genética
- **Final**: Rotas após 3000 gerações
- Cada veículo tem uma cor diferente
- Depósito em destaque (quadrado vermelho)
- Setas indicam direção das rotas

## 🔧 Modificações no Código

O código foi modificado para:
1. Aceitar número da instância como argumento de linha de comando
2. Gerar nomes de arquivos baseados no nome da instância
3. Manter compatibilidade com execução interativa (sem argumentos)

## 💡 Dicas

- Para executar em background: `nohup ./run_all_instances.sh > output.log 2>&1 &`
- Para monitorar progresso: `tail -f output.log`
- Para executar apenas instâncias C: modifique o array `INSTANCES` no script
