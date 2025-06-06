# Vehicle Routing Problem - Java (VRP)

Este projeto implementa uma solução baseada em Algoritmos Genéticos para o **Vehicle Routing Problem (VRP)**, um problema clássico de otimização combinatória onde o objetivo é determinar as melhores rotas para uma frota de veículos atender um conjunto de clientes, minimizando custos como distância, tempo, combustível ou penalidades.

## Funcionalidades

- **Algoritmos Genéticos:**  
  - **Multi-Objetivo:** Otimização simultânea de distância, tempo, combustível e critério ponderado
  - **Mono-Objetivo:** Interface preparada para implementação futura

- **População e Subpopulações:**  
  A população é dividida em subpopulações especializadas em diferentes objetivos.

- **Cruzamento (Crossover):**  
  Implementação de cruzamento de um ponto, com normalização e desnormalização das rotas.

- **Mutação:**  
  Troca de clientes aleatórios nas rotas dos veículos, respeitando restrições.

- **Elitismo:**  
  Seleção dos melhores indivíduos para a próxima geração, garantindo que as melhores soluções sejam preservadas.

- **Seleção por Torneio:**  
  Seleção de pais baseada em torneios dentro das subpopulações.

- **Cálculo de Fitness:**  
  Critérios separados para distância, tempo, combustível e ponderado, com penalidades para violações de restrições.

- **Leitura de Instâncias Solomon:**  
  Suporte para leitura de arquivos de instância padrão Solomon.

- **Visualização Gráfica:**  
  Script Python para visualizar a evolução do fitness ao longo das gerações.

## Estrutura dos Principais Arquivos

- `src/main/App.java`  
  Ponto de entrada do programa, inicializa instâncias, populações e executa o ciclo evolutivo.

- `src/genetic/Population.java`  
  Gerencia a população, subpopulações, inicialização, atualização e comparação de indivíduos.

- `src/genetic/Individual.java`  
  Representa um indivíduo (solução), com rotas, fitness e métodos utilitários.

- `src/genetic/Crossover.java`  
  Implementa o cruzamento de um ponto com normalização/desnormalização das rotas.

- `src/genetic/Mutation.java`  
  Implementa a mutação de clientes nas rotas dos veículos.

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