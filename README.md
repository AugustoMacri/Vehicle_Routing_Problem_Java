# Vehicle Routing Problem - Java (VRP)

Este projeto implementa uma solução baseada em Algoritmos Genéticos para o **Vehicle Routing Problem (VRP)**, um problema clássico de otimização combinatória onde o objetivo é determinar as melhores rotas para uma frota de veículos atender um conjunto de clientes, minimizando custos como distância, tempo, combustível ou penalidades.

## 🎯 Funcionalidades Principais

### Algoritmo Genético Multi-Objetivo
- **Otimização Simultânea:** Distância, tempo, combustível e critério ponderado
- **População Híbrida:** 70% K-means clustering + 30% Gillet-Miller
- **4 Subpopulações Especializadas:** Uma para cada objetivo
- **Ponderação Multi-Objetivo:** Distância×1.0, Tempo×0.5, Combustível×0.75

### Operadores Genéticos
- **Cruzamento:** Cruzamento de um ponto com normalização de rotas
- **Mutação:** Troca aleatória de clientes respeitando restrições
- **Elitismo:** Preservação dos melhores indivíduos
- **Seleção:** Torneio dentro das subpopulações

### Inicialização Inteligente
- **K-means Clustering:** Agrupamento geográfico para 70% da população
- **Gillet-Miller:** Construção sequencial para 30% (diversidade)
- **Resultado:** Redução de 30-40% na distância inicial

### Sistema de Visualização
- **Armazenamento de Rotas:** Rotas iniciais e finais nos arquivos de resultado
- **Mapas Coloridos:** Visualização gráfica das rotas por veículo
- **Comparação Visual:** Análise da evolução das soluções
- **Alta Resolução:** Exportação em PNG 300dpi

### Automação e Validação
- **Scripts Bash:** Execução automatizada de instâncias
- **Validação Sistemática:** Framework para 10 execuções por instância
- **Estatísticas:** Melhor, médio, desvio padrão
- **Benchmark Solomon:** Suporte para C, R e RC instances

## 📁 Estrutura do Projeto

### Código Principal

- `src/main/App.java`  
  Entrada do programa, CLI, execução do algoritmo genético

- `src/genetic/Population.java`  
  Gerenciamento de população e subpopulações

- `src/genetic/Individual.java`  
  Representação de soluções (rotas dos veículos)

- `src/genetic/KMeansClusteringInitializer.java`  
  Inicialização inteligente com K-means++

- `src/genetic/Crossover.java`  
  Operador de cruzamento com normalização

- `src/genetic/Mutation.java`  
  Operador de mutação de clientes

- `src/genetic/SelectionUtils.java`  
  Métodos de seleção de elite e seleção por torneio.

- `src/genetic/*FitnessCalculator.java`  
  Implementações específicas para cada critério de fitness (distância, tempo, combustível, ponderado).

- `src/vrp/BenchMarkReader.java`, `src/vrp/Client.java`, `src/vrp/ProblemInstance.java`  
  Utilitários para leitura e representação das instâncias do problema.

- `visualize_results.py`  
  Script Python para visualização gráfica dos resultados.

## Como Executar

### Pré-requisitos

- **Java 11 ou superior**
- **Python 3.6 ou superior** (para visualização dos resultados)
- **Biblioteca Matplotlib** (para visualização dos resultados)

### Compilação e Execução do Java

1. **Compilação:**  
   Compile todos os arquivos Java do projeto:
   ```sh
   javac -d bin src/**/*.java