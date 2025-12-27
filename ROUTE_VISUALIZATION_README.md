# Scripts de Visualização de Rotas

Este documento descreve os scripts criados para visualização e análise de rotas do VRP.

## plot_route_maps.py

Script Python para gerar mapas visuais das rotas dos veículos.

### Funcionalidades

- Lê arquivos de resultados `evo_*.txt` que contêm rotas iniciais e finais
- Extrai coordenadas dos clientes dos arquivos de instâncias Solomon
- Gera mapas coloridos mostrando:
  - Rotas iniciais (antes da evolução genética)
  - Rotas finais (após 3000 gerações)
  - Cada veículo com uma cor diferente
  - Setas indicando a direção das rotas
  - Depósito destacado em vermelho

### Uso

```bash
# Processar uma única instância
python3 scripts/plot_route_maps.py --instance C101 \
    --results-dir results_validation_C1_previous/C101 \
    --instances-dir src/instances/solomon \
    --output-dir results_validation_C1_previous/C101/route_maps

# Processar todas as instâncias C1 (C101-C109)
python3 scripts/plot_route_maps.py \
    --results-dir results_validation_C1_previous \
    --instances-dir src/instances/solomon \
    --output-dir results_validation_C1_previous
```

### Parâmetros

- `--instance`: Nome da instância a processar (ex: C101). Opcional.
- `--results-dir`: Diretório contendo os arquivos evo_*.txt. Padrão: `results_validation_C1_previous`
- `--instances-dir`: Diretório com arquivos das instâncias. Padrão: `src/instances/solomon`
- `--output-dir`: Diretório para salvar os mapas. Padrão: mesmo que results-dir

### Arquivos de Saída

Os mapas são salvos no formato PNG:
- `route_map_c101_initial.png` - Rotas antes da evolução
- `route_map_c101_final.png` - Rotas após 3000 gerações

### Requisitos

```bash
pip install matplotlib numpy
```

## Modificações em App.java

### Novos Métodos

1. **copyIndividual(Individual source)**
   - Cria uma cópia profunda de um indivíduo
   - Preserva todas as rotas e valores de fitness
   - Usado para armazenar estado inicial e final

2. **formatRoutesForFile(Individual individual, List<Client> clients, String label)**
   - Formata rotas de forma legível para salvar em arquivo
   - Mostra sequência de clientes: Depósito → Cliente(X) → ... → Depósito
   - Calcula estatísticas: distância, demanda, número de clientes
   - Retorna string formatada para inclusão no arquivo de resultados

### Variáveis Estáticas

- `initialBestIndividual` - Melhor indivíduo antes da evolução
- `finalBestIndividual` - Melhor indivíduo após 3000 gerações

### Fluxo de Captura de Rotas

1. Após inicialização e cálculo de fitness inicial:
   ```java
   initialBestIndividual = population.getSubPopPonderation().stream()
       .min(Comparator.comparingDouble(Individual::getFitness))
       .map(ind -> copyIndividual(ind))
       .orElse(null);
   ```

2. Após 3000 gerações:
   ```java
   finalBestIndividual = population.getSubPopPonderation().stream()
       .min(Comparator.comparingDouble(Individual::getFitness))
       .map(ind -> copyIndividual(ind))
       .orElse(null);
   ```

3. Salvamento em arquivo:
   ```java
   saveResultsToFile(..., instance.getClients());
   ```

### Formato do Arquivo evo_*.txt

O arquivo agora inclui duas novas seções ao final:

```
ROTAS INICIAIS (Antes da Evolução)
================================================================================

Veículo 0: Depósito(0) -> Cliente(5) -> Cliente(3) -> Cliente(7) -> Depósito(0)
    Clientes: 3 | Demanda: 45/200 | Distância: 125.43

Veículo 1: Depósito(0) -> Cliente(13) -> Cliente(17) -> Depósito(0)
    Clientes: 2 | Demanda: 30/200 | Distância: 89.12

Total de veículos usados: 10
Distância total: 1034.13
================================================================================


ROTAS FINAIS (Após 3000 Gerações)
================================================================================

Veículo 0: Depósito(0) -> Cliente(5) -> Cliente(6) -> Cliente(7) -> Depósito(0)
    Clientes: 3 | Demanda: 45/200 | Distância: 98.56

Veículo 1: Depósito(0) -> Cliente(13) -> Cliente(17) -> Cliente(19) -> Depósito(0)
    Clientes: 3 | Demanda: 38/200 | Distância: 76.89

Total de veículos usados: 9
Distância total: 828.42
================================================================================
```

## Integração com Scripts de Validação

O script `plot_route_maps.py` pode ser integrado ao `run_validation_c1.py` para gerar automaticamente os mapas após cada conjunto de execuções:

```python
# Após executar 10 vezes cada instância
import subprocess

for instance in instances:
    subprocess.run([
        'python3', 'scripts/plot_route_maps.py',
        '--instance', instance,
        '--results-dir', f'results_validation_C1_previous/{instance}',
        '--output-dir', f'results_validation_C1_previous/{instance}/route_maps'
    ])
```

## Exemplos de Uso

### Exemplo 1: Gerar Mapas Após Validação

```bash
# 1. Executar validação
python3 scripts/run_validation_c1.py

# 2. Gerar mapas para todas as instâncias
python3 scripts/plot_route_maps.py --results-dir results_validation_C1_previous
```

### Exemplo 2: Analisar Uma Instância Específica

```bash
# 1. Executar uma instância
bash run_single_instance.sh 1  # C101

# 2. Gerar mapa da execução
python3 scripts/plot_route_maps.py --instance C101 \
    --results-dir resultsMulti \
    --output-dir resultsMulti/route_maps
```

## Interpretação dos Mapas

### Elementos Visuais

- **Quadrado Vermelho**: Depósito (ponto de partida e chegada)
- **Círculos Pretos**: Clientes a serem atendidos
- **Linhas Coloridas**: Rotas de cada veículo
- **Setas**: Direção do percurso
- **Números**: Identificação dos clientes

### Análise Comparativa

Compare os mapas inicial e final para observar:
1. **Redução de cruzamentos** - Rotas finais devem ter menos interseções
2. **Agrupamento geográfico** - Clientes próximos atendidos na mesma rota
3. **Número de veículos** - Evolução pode reduzir veículos necessários
4. **Comprimento das rotas** - Rotas finais geralmente mais curtas

### Indicadores de Qualidade

- Rotas bem organizadas formam "pétalas" ao redor do depósito
- Poucos cruzamentos entre rotas diferentes
- Distribuição equilibrada de clientes por veículo
- Rotas compactas sem longos deslocamentos
