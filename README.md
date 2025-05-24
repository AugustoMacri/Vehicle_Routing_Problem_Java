# Vehicle Routing Problem - Java (VRP)

Este projeto implementa uma solução baseada em Algoritmos Genéticos para o **Vehicle Routing Problem (VRP)**, um problema clássico de otimização combinatória onde o objetivo é determinar as melhores rotas para uma frota de veículos atender um conjunto de clientes, minimizando custos como distância, tempo, combustível ou penalidades.

## Funcionalidades

- **Algoritmo Genético Multiobjetivo:**  
  O código suporta múltiplos critérios de otimização (distância, tempo, combustível, ponderado).
- **População e Subpopulações:**  
  A população é dividida em subpopulações especializadas em diferentes objetivos.
- **Cruzamento (Crossover):**  
  Implementação de cruzamento de um ponto, com normalização e desnormalização das rotas.
- **Mutação:**  
  Troca de clientes aleatórios nas rotas dos veículos, respeitando restrições.
- **Elitismo:**  
  Seleção dos melhores indivíduos para a próxima geração, tanto para mono quanto para multiobjetivo.
- **Seleção por Torneio:**  
  Seleção de pais baseada em torneios dentro das subpopulações.
- **Cálculo de Fitness:**  
  Critérios separados para distância, tempo, combustível e ponderado, com penalidades para violações de restrições.
- **Leitura de Instâncias Solomon:**  
  Suporte para leitura de arquivos de instância padrão Solomon.

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

- `src/genetic/FitnessCalculator.java`  
  Interface para cálculo de fitness.

- `src/genetic/DistanceFitnessCalculator.java`,  
  `src/genetic/TimeFitnessCalculator.java`,  
  `src/genetic/FuelFitnessCalculator.java`,  
  `src/genetic/DefaultFitnessCalculator.java`  
  Implementações específicas para cada critério de fitness.

- `src/vrp/BenchMarkReader.java`, `src/vrp/Client.java`, `src/vrp/ProblemInstance.java`  
  Utilitários para leitura e representação das instâncias do problema.

## Como Executar

1. **Pré-requisitos:**  
   - Java 11 ou superior.

2. **Compilação:**  
   Compile todos os arquivos Java do projeto:
   ```sh
   javac -d bin src/**/*.java