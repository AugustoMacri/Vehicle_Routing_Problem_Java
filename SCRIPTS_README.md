# Scripts de Execução VRP

Este diretório contém scripts para automatizar a execução do algoritmo genético em múltiplas instâncias do Solomon benchmark.

## 📁 Arquivos

- **`run_all_instances.sh`**: Executa todas as 26 instâncias especificadas automaticamente
- **`run_single_instance.sh`**: Executa uma única instância específica

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
└── stats/
    ├── stats_c101.txt    # Estatísticas C101
    ├── stats_c102.txt    # Estatísticas C102
    ├── ...
    └── stats_rc108.txt   # Estatísticas RC108
```

## ⚙️ Formato dos Arquivos de Saída

### Arquivo de Evolução (`evo_*.txt`)
Contém a evolução do fitness a cada 100 gerações:
- subPopDistance
- subPopTime
- subPopFuel
- subPopPonderation

### Arquivo de Estatísticas (`stats_*.txt`)
Contém estatísticas finais:
- Melhor Fitness
- Fitness Médio
- Desvio Padrão

## 🔧 Modificações no Código

O código foi modificado para:
1. Aceitar número da instância como argumento de linha de comando
2. Gerar nomes de arquivos baseados no nome da instância
3. Manter compatibilidade com execução interativa (sem argumentos)

## 💡 Dicas

- Para executar em background: `nohup ./run_all_instances.sh > output.log 2>&1 &`
- Para monitorar progresso: `tail -f output.log`
- Para executar apenas instâncias C: modifique o array `INSTANCES` no script
