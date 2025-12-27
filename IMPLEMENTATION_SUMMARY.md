# Resumo das Alterações - Sistema de Visualização de Rotas

## Data: 2025

## Objetivo
Implementar sistema completo para armazenar e visualizar as rotas iniciais e finais dos veículos nos arquivos de resultados, permitindo análise visual da evolução do algoritmo genético.

---

## Arquivos Modificados

### 1. `src/main/App.java`

#### Novas Variáveis Estáticas
```java
public static Individual initialBestIndividual = null;
public static Individual finalBestIndividual = null;
```
- Armazenam o melhor indivíduo antes e depois da evolução
- Permitem recuperar as rotas em qualquer momento

#### Novos Métodos Implementados

##### a) `copyIndividual(Individual source)`
- **Propósito**: Criar cópia profunda de um indivíduo
- **Detalhes**: Copia ID, todos os valores de fitness e matriz completa de rotas
- **Uso**: Preservar estado do indivíduo sem compartilhar referências

##### b) `formatRoutesForFile(Individual individual, List<Client> clients, String label)`
- **Propósito**: Formatar rotas de forma legível para arquivo
- **Saída**: String com:
  - Sequência de visitas: Depósito → Cliente(X) → ... → Depósito
  - Estatísticas por veículo: clientes atendidos, demanda, distância
  - Totais: veículos usados, distância total
- **Formato**:
  ```
  Veículo X: Depósito(0) -> Cliente(Y) -> Cliente(Z) -> Depósito(0)
      Clientes: N | Demanda: XX/200 | Distância: YY.YY
  ```

#### Modificações em Métodos Existentes

##### `runMultiObjectiveAlgorithm()`
Adicionadas 3 capturas de dados:

1. **Após cálculo de fitness inicial (linha ~431)**:
```java
initialBestIndividual = population.getSubPopPonderation().stream()
    .min(Comparator.comparingDouble(Individual::getFitness))
    .map(ind -> copyIndividual(ind))
    .orElse(null);
```

2. **Após 3000 gerações (linha ~456)**:
```java
finalBestIndividual = population.getSubPopPonderation().stream()
    .min(Comparator.comparingDouble(Individual::getFitness))
    .map(ind -> copyIndividual(ind))
    .orElse(null);
```

3. **Chamada de salvamento modificada (linha ~458)**:
```java
saveResultsToFile(generationsList, distanceFitness, timeFitness, 
                  fuelFitness, ponderationFitness, instance.getClients());
```

##### `saveResultsToFile()`
- **Assinatura modificada**: Adicionado parâmetro `List<Client> clients`
- **Nova funcionalidade**: Inclui seções de rotas no final do arquivo
```java
if (initialBestIndividual != null && finalBestIndividual != null) {
    writer.println("\n");
    writer.println(formatRoutesForFile(initialBestIndividual, clients, 
                   "ROTAS INICIAIS (Antes da Evolução)"));
    writer.println("\n");
    writer.println(formatRoutesForFile(finalBestIndividual, clients, 
                   "ROTAS FINAIS (Após 3000 Gerações)"));
}
```

---

## Arquivos Criados

### 2. `scripts/plot_route_maps.py`

#### Funcionalidades
- Lê arquivos `evo_*.txt` com formato atualizado
- Extrai coordenadas dos clientes das instâncias Solomon
- Gera mapas PNG com matplotlib

#### Métodos Principais

##### `read_instance_file(instance_path)`
- Lê arquivo Solomon (formato: ID X Y DEMAND TIME_WINDOW...)
- Retorna dicionário: `{client_id: (x, y)}`

##### `parse_routes_from_evo_file(evo_file_path)`
- Busca seções "ROTAS INICIAIS" e "ROTAS FINAIS"
- Usa regex para extrair rotas: `Veículo (\d+): Depósito...`
- Retorna dois dicionários: `{vehicle_id: [0, client1, client2, ..., 0]}`

##### `plot_routes(clients, routes, title, output_path)`
- Plota coordenadas dos clientes (círculos pretos)
- Destaca depósito (quadrado vermelho)
- Desenha rotas com cores diferentes por veículo
- Adiciona setas indicando direção
- Gera legenda com identificação dos veículos
- Salva em PNG (300 dpi)

##### `process_instance(instance_name, results_dir, instances_dir, output_dir)`
- Orquestra todo o processo para uma instância
- Busca arquivo evo mais recente
- Gera mapas inicial e final

#### Parâmetros de Linha de Comando
```bash
--instance       # Nome da instância (ex: C101) - opcional
--results-dir    # Diretório com resultados (padrão: results_validation_C1_previous)
--instances-dir  # Diretório com instâncias (padrão: src/instances/solomon)
--output-dir     # Diretório para mapas (padrão: mesmo que results-dir)
```

#### Exemplos de Uso
```bash
# Uma instância específica
python3 scripts/plot_route_maps.py --instance C101 \
    --results-dir resultsMulti \
    --output-dir resultsMulti/route_maps

# Todas as instâncias C1 (C101-C109)
python3 scripts/plot_route_maps.py \
    --results-dir results_validation_C1_previous
```

---

### 3. `ROUTE_VISUALIZATION_README.md`

Documentação completa incluindo:
- Descrição de todos os métodos
- Exemplos de uso dos scripts
- Formato dos arquivos de saída
- Guia de interpretação dos mapas
- Integração com pipeline de validação

---

## Formato dos Arquivos de Resultados

### Estrutura do `evo_*.txt` (Atualizado)

```
Subpopulação\Geração    g0      g100    g200    ...     g3000
subPopDistance          1034.13 988.36  988.36  ...     828.42
subPopTime              4498.48 3742.14 3742.14 ...     2956.78
subPopFuel              552.34  530.08  530.08  ...     443.21
subPopPonderation       6756.49 5423.14 5415.21 ...     4228.42


ROTAS INICIAIS (Antes da Evolução)
================================================================================

Veículo 0: Depósito(0) -> Cliente(5) -> Cliente(3) -> Cliente(7) -> Depósito(0)
    Clientes: 3 | Demanda: 45/200 | Distância: 125.43

Veículo 1: Depósito(0) -> Cliente(13) -> Cliente(17) -> Depósito(0)
    Clientes: 2 | Demanda: 30/200 | Distância: 89.12

[... outras rotas ...]

Total de veículos usados: 10
Distância total: 1034.13
================================================================================


ROTAS FINAIS (Após 3000 Gerações)
================================================================================

Veículo 0: Depósito(0) -> Cliente(5) -> Cliente(6) -> Cliente(7) -> Depósito(0)
    Clientes: 3 | Demanda: 45/200 | Distância: 98.56

Veículo 1: Depósito(0) -> Cliente(13) -> Cliente(17) -> Cliente(19) -> Depósito(0)
    Clientes: 3 | Demanda: 38/200 | Distância: 76.89

[... outras rotas ...]

Total de veículos usados: 9
Distância total: 828.42
================================================================================
```

---

## Arquivos de Saída dos Mapas

### Nomenclatura
- `route_map_c101_initial.png` - Rotas antes da evolução
- `route_map_c101_final.png` - Rotas após 3000 gerações

### Estrutura dos Diretórios
```
results_validation_C1_previous/
├── C101/
│   ├── evo_c101_exec01.txt
│   ├── evo_c101_exec02.txt
│   ├── ...
│   └── route_maps/
│       ├── route_map_c101_initial.png
│       └── route_map_c101_final.png
├── C102/
│   └── route_maps/
│       ├── route_map_c102_initial.png
│       └── route_map_c102_final.png
└── ...
```

---

## Elementos Visuais dos Mapas

### Símbolos
- **Quadrado Vermelho (■)**: Depósito (tamanho 12)
- **Círculos Pretos (●)**: Clientes (tamanho 6)
- **Números**: IDs dos clientes
- **Linhas Coloridas**: Rotas dos veículos
- **Setas**: Direção do percurso

### Cores
- Até 10 veículos: Cores da paleta TABLEAU
- Mais de 10: Rainbow colormap distribuída uniformemente

### Layout
- Grid com transparência 0.3
- Legenda no canto superior direito (2 colunas)
- Título em negrito (tamanho 14)
- Resolução: 300 dpi

---

## Fluxo de Trabalho Completo

### 1. Execução do Algoritmo
```bash
bash run_single_instance.sh 1  # C101
```
- Arquivo gerado: `resultsMulti/evo_c101.txt`
- Contém: fitness por geração + rotas inicial/final

### 2. Geração de Mapas
```bash
python3 scripts/plot_route_maps.py --instance C101 \
    --results-dir resultsMulti \
    --output-dir resultsMulti/route_maps
```
- Arquivos gerados:
  - `route_map_c101_initial.png`
  - `route_map_c101_final.png`

### 3. Análise Visual
Compare os mapas observando:
- Redução de cruzamentos entre rotas
- Melhor agrupamento geográfico
- Redução no número de veículos
- Diminuição do comprimento total das rotas

---

## Integração com Validação

### Possível Extensão do `run_validation_c1.py`

```python
# Após executar 10 vezes cada instância
import subprocess

for instance in ['C101', 'C102', ..., 'C109']:
    print(f"\nGerando mapas para {instance}...")
    subprocess.run([
        'python3', 'scripts/plot_route_maps.py',
        '--instance', instance,
        '--results-dir', f'results_validation_C1_previous/{instance}',
        '--output-dir', f'results_validation_C1_previous/{instance}/route_maps'
    ])
```

---

## Benefícios da Implementação

### 1. Análise Visual
- Compreensão imediata da qualidade das soluções
- Identificação de padrões e problemas (ex: cruzamentos)
- Comparação visual entre diferentes execuções

### 2. Validação
- Verificação de que o algoritmo está funcionando corretamente
- Detecção de rotas inválidas ou subótimas
- Confirmação de que a evolução está melhorando as soluções

### 3. Apresentação
- Materiais visuais para artigos e apresentações
- Demonstração clara da eficácia do algoritmo
- Comparação com trabalhos relacionados

### 4. Debugging
- Identificação de problemas na construção de rotas
- Verificação da correção das distâncias calculadas
- Análise do comportamento do K-means clustering

---

## Dependências

### Python
```bash
pip install matplotlib numpy
```

### Java
- JDK 11 ou superior
- Arquivos de instâncias Solomon em `src/instances/solomon/`

---

## Próximos Passos (Opcionais)

1. **Animações**: Gerar GIF mostrando evolução das rotas
2. **Comparações**: Plotar rotas inicial/final lado a lado
3. **Estatísticas Visuais**: Adicionar histogramas de distâncias
4. **Heatmaps**: Visualizar densidade de clientes atendidos
5. **Interatividade**: Versão web interativa com zoom/pan
6. **Métricas**: Calcular e exibir índice de qualidade das rotas

---

## Notas Técnicas

### Cálculo de Distâncias
- Euclidiana 2D: `sqrt((x2-x1)² + (y2-y1)²)`
- Inclui: depósito→primeiro + inter-clientes + último→depósito

### Capacidade dos Veículos
- Solomon C1: 200 unidades
- Verificação de capacidade na formatação

### Coordenadas
- Lidas diretamente dos arquivos Solomon
- Formato: `ID X Y DEMAND READY_TIME DUE_DATE SERVICE_TIME`
- Linha 0: Depósito

---

## Status: ✅ Implementação Completa

Todas as funcionalidades foram implementadas e testadas:
- [x] Variáveis para armazenar rotas
- [x] Método copyIndividual()
- [x] Método formatRoutesForFile()
- [x] Captura de rotas inicial e final
- [x] Modificação do saveResultsToFile()
- [x] Script Python plot_route_maps.py
- [x] Documentação completa
- [x] Exemplos de uso
