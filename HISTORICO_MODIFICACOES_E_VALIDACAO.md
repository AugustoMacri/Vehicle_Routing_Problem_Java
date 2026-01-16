# Histórico de Modificações e Validação do Algoritmo VRP

**Documento para Contexto de IA**  
**Última Atualização:** 15 de Janeiro de 2026

---

## 🎯 Objetivo deste Documento

Este documento serve como **contexto completo** para assistentes de IA (como Claude Code) que precisem modificar ou entender este projeto. Contém:

1. **Histórico completo de bugs** e suas correções
2. **Como o algoritmo funciona** em detalhes
3. **Processo de validação** que comprova os resultados atuais
4. **Estado atual do código** e parâmetros

---

## 📖 Contexto Histórico: Jornada de Correções

### Timeline de Desenvolvimento

#### Dezembro 2025: Descoberta de Bugs Críticos

**Problema Inicial Reportado:**
> "O algoritmo está perdendo clientes durante a evolução. C102 deveria ter 100 clientes, mas só tem 34-90 nas rotas finais."

**Investigação Revelou 3 Bugs Críticos:**

##### Bug #1: Perda de Clientes no Crossover

**Arquivo:** `src/genetic/Crossover.java`  
**Método:** `denormalizeRoute()`  
**Linhas:** ~180-200

**O Problema:**
```java
// CÓDIGO BUGADO (ANTIGO)
for (c = 0; c < App.numClients; c++) {
    if (route[v][c] == 0 && c > 0) {
        break;  // ❌ PARA NO PRIMEIRO ZERO!
    }
    denormalizedRoute[v][c] = route[v][c];
}
```

**Por Que Isso Perdia Clientes:**

1. `repairRoute()` criava rotas normalizadas: `[0, 5, 10, 15, 0, 0, ...]`
2. `insertClientAnywhere()` adicionava clientes faltantes em posições vazias
3. Resultado possível: `[0, 5, 10, 15, 0, **82**, 0, ...]` ← cliente 82 **depois do zero**
4. `denormalizeRoute()` parava na posição 4 (primeiro zero)
5. Cliente 82 **nunca era copiado** para a rota final → perdido permanentemente! 💀

**Solução:**
```java
// CÓDIGO CORRIGIDO (NOVO)
int pos = 0;
for (int c = 0; c < App.numClients; c++) {
    if (route[v][c] != 0 && route[v][c] != -1) {
        denormalizedRoute[v][pos++] = route[v][c];  // ✅ Compacta TUDO
    }
}
while (pos < App.numClients) {
    denormalizedRoute[v][pos++] = -1;
}
```

**Resultado:** 100% dos clientes preservados durante crossover ✅

##### Bug #2: Embaralhamento de Fitness

**Arquivo:** `src/main/App.java`  
**Método:** `copyIndividual()`  
**Linha:** ~157

**O Problema:**
```java
// CÓDIGO BUGADO
Individual copy = new Individual(
    source.getId(),
    source.getFitnessDistance(),  // ❌ Vai para 'fitness'
    source.getFitnessTime(),      // ❌ Vai para 'fitnessDistance'
    source.getFitnessFuel(),      // ❌ Vai para 'fitnessTime'
    source.getFitness()           // ❌ Vai para 'fitnessFuel'
);

// Construtor correto é:
// Individual(int id, double fitness, double fitnessDistance, 
//            double fitnessTime, double fitnessFuel)
```

**Por Que Isso Era Grave:**
- Indivíduo com distância 1200, tempo 5000, combustível 700, fitness 2500
- Após cópia: distância 5000, tempo 700, combustível 2500, fitness 1200
- **Ordenação e seleção completamente erradas!**

**Solução:**
```java
// CÓDIGO CORRIGIDO
Individual copy = new Individual(
    source.getId(),
    source.getFitness(),          // ✅ Correto!
    source.getFitnessDistance(),  // ✅ Correto!
    source.getFitnessTime(),      // ✅ Correto!
    source.getFitnessFuel()       // ✅ Correto!
);
```

**Resultado:** Fitness copiados corretamente, seleção funciona ✅

##### Bug #3: Velocidade Incorreta do Veículo

**Arquivos:** `src/main/App.java` e `src/configuration/Config.java`  
**Variável:** `VEHICLE_SPEED`

**O Problema:**
```java
// CÓDIGO BUGADO
public static int VEHICLE_SPEED = 50;  // ❌ 50x mais rápido!
```

**Por Que Isso Invalidava Tudo:**

Padrão Solomon: `tempo_viagem = distância / velocidade`

- Com VEHICLE_SPEED = 50:
  - Distância 100 → tempo = 100/50 = **2 unidades**
- Com VEHICLE_SPEED = 1 (correto):
  - Distância 100 → tempo = 100/1 = **100 unidades**

**Impacto Real:**
- Tempos calculados **50x menores** que o correto
- Janelas de tempo **nunca violadas** (falso positivo!)
- Soluções pareciam válidas mas estavam **completamente erradas**

**Solução:**
```java
// CÓDIGO CORRIGIDO
public static int VEHICLE_SPEED = 1;  // ✅ Padrão Solomon
```

**Resultado:** Cálculo de tempo correto, validação real ✅

---

#### Janeiro 2026: Refinamentos de Validação

Após corrigir os 3 bugs acima, novas execuções revelaram problemas sutis:

##### Bug #4: Solomon I1 Não-Conservador

**Arquivo:** `src/genetic/SolomonInsertion.java`  
**Método:** `getBestInsertion()`  
**Linha:** ~273

**O Problema:**
```java
// CÓDIGO PROBLEMÁTICO (tinha código morto)
// Tinha código para 'bestWithViolation' que nunca era usado
// Mas método retornava TODAS as inserções, incluindo inviáveis

return allInsertions;  // ❌ Inclui inserções que violam time windows!
```

**Por Que Isso Era Problemático:**
- Solomon I1 deveria criar **apenas soluções factíveis**
- Inserções com `ΔT < 0` (violam janela) eram retornadas
- Resultado: Soluções iniciais com violações de time window
- AG tentava corrigir, mas penalização era insuficiente

**Solução:**
```java
// CÓDIGO CORRIGIDO
return feasibleInsertions;  // ✅ Apenas inserções SEM violação!
```

**Código Completo (linhas 250-275):**
```java
List<Insertion> feasibleInsertions = new ArrayList<>();
List<Insertion> allInsertions = new ArrayList<>();

for (int i = 0; i < route.size() - 1; i++) {
    // ... cálculos de c2, distâncias, tempos ...
    
    double deltaTime = newServiceTime - currentServiceTime;
    
    Insertion ins = new Insertion(i, c2Value, deltaTime, 
                                  newDistance, newServiceTime);
    allInsertions.add(ins);
    
    if (deltaTime >= 0) {  // Sem violação de time window
        feasibleInsertions.add(ins);
    }
}

// MODO CONSERVADOR: Retorna apenas inserções factíveis
return feasibleInsertions;  // ✅ Garante 0 violações desde o início
```

**Resultado:** Soluções iniciais 100% válidas ✅

##### Bug #5: Mutação Inter-Rota Sem Validação de Capacidade

**Arquivo:** `src/genetic/Mutation.java`  
**Método:** `mutateInterRoute()`  
**Linhas:** 140-162

**O Problema:**
```java
// CÓDIGO BUGADO (ANTIGO)
// Selecionava 2 veículos e 2 clientes
int temp = route[vehicle1][client1Pos];
route[vehicle1][client1Pos] = route[vehicle2][client2Pos];
route[vehicle2][client2Pos] = temp;
// ❌ TROCA SEM VERIFICAR CAPACIDADE!
```

**Manifestação do Bug:**

Após 5000 gerações, C101 exec01-10:
```
✅ 100 clientes visitados
✅ 0 violações de time window
❌ Rota 3 SEMPRE com violação: 230/200 (em TODAS as 10 execuções!)
```

**Por Que Isso Acontecia:**

1. Solomon I1 criava solução inicial válida (13 veículos, todas rotas OK)
2. AG evoluía por 5000 gerações
3. Mutação inter-rota (taxa 60%!) trocava clientes entre veículos
4. Exemplo:
   - Veículo 1: demanda 180/200, tem cliente com demanda 30
   - Veículo 3: demanda 190/200, tem cliente com demanda 10
   - Mutação troca: cliente(30) ↔ cliente(10)
   - Resultado: Veículo 1: 160/200 ✅, Veículo 3: **210/200** ❌
5. Mutação era aceita mesmo violando capacidade
6. Convergência do AG levava ao padrão repetitivo (230/200 na rota 3)

**Solução:**

Adicionar **validação de capacidade ANTES da troca**:

```java
// CÓDIGO CORRIGIDO (NOVO) - Linhas 140-168

// 1. Calcular demandas atuais de ambos os veículos
double demand1 = 0, demand2 = 0;
for (int c = 0; c < App.numClients - 1; c++) {
    if (route[vehicle1][c] != -1 && route[vehicle1][c] != 0) {
        demand1 += clients.get(route[vehicle1][c]).getDemand();
    }
    if (route[vehicle2][c] != -1 && route[vehicle2][c] != 0) {
        demand2 += clients.get(route[vehicle2][c]).getDemand();
    }
}

// 2. Obter demandas dos clientes a trocar
int clientId1 = route[vehicle1][client1Pos];
int clientId2 = route[vehicle2][client2Pos];
double clientDemand1 = clients.get(clientId1).getDemand();
double clientDemand2 = clients.get(clientId2).getDemand();

// 3. Calcular novas demandas após a troca
double newDemand1 = demand1 - clientDemand1 + clientDemand2;
double newDemand2 = demand2 - clientDemand2 + clientDemand1;

// 4. VALIDAR capacidade antes de aceitar
if (newDemand1 > App.vehicleCapacity || newDemand2 > App.vehicleCapacity) {
    return;  // ✅ REJEITA mutação que viola capacidade!
}

// 5. Se passou na validação, executa a troca
int temp = route[vehicle1][client1Pos];
route[vehicle1][client1Pos] = route[vehicle2][client2Pos];
route[vehicle2][client2Pos] = temp;
```

**Mudanças Necessárias:**

1. **Adicionar parâmetro `clients`** ao método:
   ```java
   // ANTES
   public static void mutateInterRoute(Individual individual, double mutationRate)
   
   // DEPOIS
   public static void mutateInterRoute(Individual individual, double mutationRate, 
                                        List<Client> clients)
   ```

2. **Atualizar todas as chamadas** (10 locais em `Population.java`):
   ```java
   // ANTES
   Mutation.mutateCombined(newSon, App.mutationRate, App.interRouteMutationRate);
   
   // DEPOIS
   Mutation.mutateCombined(newSon, App.mutationRate, App.interRouteMutationRate, clients);
   ```

**Resultado:** 0 violações de capacidade em todas as 260 execuções ✅

---

## 🧬 Como o Algoritmo Funciona (Detalhado)

### Arquitetura Geral

```
┌─────────────────────────────────────────────────────────────┐
│                     INICIALIZAÇÃO                           │
│  Solomon I1 (Conservador) → 900 indivíduos válidos          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  4 SUBPOPULAÇÕES (300 cada)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐ │
│  │SubPop    │  │SubPop    │  │SubPop    │  │SubPop      │ │
│  │Distance  │  │Time      │  │Fuel      │  │Ponderation │ │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              LOOP DE EVOLUÇÃO (5000 gerações)               │
│                                                             │
│  Para cada subpopulação:                                    │
│    1. Seleção por Torneio (2 indivíduos)                    │
│    2. Crossover (100%)                                      │
│    3. Mutação Combinada:                                    │
│       - Intra-rota (1% chance)                              │
│       - Inter-rota (100% quando mutação ocorre)             │
│    4. Avaliar Fitness (4 objetivos)                         │
│    5. Ordenar por objetivo da subpopulação                  │
│    6. Elitismo (10% melhores preservados)                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  RESULTADO FINAL                            │
│  Melhor indivíduo entre todas as 4 subpopulações            │
└─────────────────────────────────────────────────────────────┘
```

### Detalhamento de Cada Fase

#### Fase 1: Inicialização (Solomon I1 Conservador)

**Arquivo:** `src/genetic/SolomonInsertion.java`

**Algoritmo:**

```
1. Inicializar solução vazia
2. Enquanto existirem clientes não visitados:
   
   2.1. Criar nova rota:
        - Selecionar cliente semente (mais distante do depósito)
        - Adicionar cliente à rota: [Depósito → Cliente → Depósito]
   
   2.2. Preencher rota com inserções sequenciais:
        LOOP:
          a. Para cada cliente não visitado:
             - Testar inserção em cada posição da rota
             - Calcular critério c2 = λ*d(i,u) + (1-λ)*d(u,j) - μ*d(i,j) + β*ΔT
             - Calcular ΔT (impacto no tempo de serviço)
             - Se ΔT >= 0: adicionar à lista de inserções factíveis
          
          b. Se lista factível está vazia:
             - BREAK (criar nova rota)
          
          c. Senão:
             - Selecionar inserção com menor c2
             - Inserir cliente na rota
             - Marcar cliente como visitado
   
   2.3. Adicionar rota à solução

3. Retornar solução completa (100% válida)
```

**Parâmetros:**
- λ = 1.0 (peso distância no critério c2)
- μ = 1.0 (peso economia)
- β = 1.0 (peso tempo)
- α1 = 0.0, α2 = 0.0 (não usados no modo conservador)

**Garantia:**
- **SEMPRE** retorna solução com 0 violações de time window
- **SEMPRE** respeita capacidade (200 unidades)
- **SEMPRE** visita todos os clientes exatamente uma vez

#### Fase 2: Evolução das Subpopulações

**Arquivo:** `src/genetic/Population.java`

**Para Cada Geração (5000 iterações):**

```java
// Pseudo-código
for (generation = 0; generation < 5000; generation++) {
    
    for (each subpopulation) {
        
        newGeneration = [];
        
        // Gerar novos indivíduos
        while (newGeneration.size() < 300) {
            
            // 1. SELEÇÃO
            parent1 = tournamentSelection(subpopulation, size=2);
            parent2 = tournamentSelection(subpopulation, size=2);
            
            // 2. CROSSOVER (100%)
            offspring = onePointCrossover(parent1, parent2);
            
            // 3. MUTAÇÃO (1% chance)
            if (random() < 0.01) {
                // Mutação intra-rota
                mutateIntraRoute(offspring);
                
                // Mutação inter-rota (100% quando mutação ocorre)
                if (random() < 1.0) {  // Sempre!
                    mutateInterRoute(offspring);  // Com validação de capacidade
                }
            }
            
            // 4. AVALIAR FITNESS
            offspring.fitnessDistance = calculateDistance(offspring);
            offspring.fitnessTime = calculateTime(offspring);
            offspring.fitnessFuel = calculateFuel(offspring);
            offspring.fitness = calculatePonderated(offspring);
            
            newGeneration.add(offspring);
        }
        
        // 5. ELITISMO (preservar 10% melhores da geração anterior)
        elite = top10Percent(subpopulation);
        newGeneration = merge(newGeneration, elite);
        
        // 6. ORDENAR por objetivo da subpopulação
        if (subpop == SubpopDistance)
            sort(newGeneration, by: fitnessDistance);
        else if (subpop == SubpopTime)
            sort(newGeneration, by: fitnessTime);
        // ... etc
        
        // 7. SELECIONAR 300 melhores
        subpopulation = top300(newGeneration);
    }
}
```

#### Fase 3: Cálculo de Fitness

**Arquivo:** `src/genetic/fitness/`

**4 Calculadoras de Fitness:**

1. **DistanceFitnessCalculator:**
   ```
   fitness = ∑(distâncias de todas as rotas)
   ```

2. **TimeFitnessCalculator:**
   ```
   fitness = ∑(tempos de todas as rotas)
   Onde: tempo_rota = tempo_viagem + tempo_serviço + tempo_espera
   ```

3. **FuelFitnessCalculator:**
   ```
   fitness = ∑(combustível de todas as rotas)
   Onde: combustível = distância × consumo_por_km
   ```

4. **DefaultFitnessCalculator (Ponderado):**
   ```
   fitness = (distância × 1.0) + 
             (tempo × 0.5) + 
             (combustível × 0.75) +
             (num_violations × 1000.0)
   ```

**Cálculo de Violações de Time Window:**

```java
int violations = 0;
for (each route) {
    currentTime = 0;  // Início no depósito
    
    for (each client in route) {
        travelTime = distance(previous, client) / VEHICLE_SPEED;
        arrivalTime = currentTime + travelTime;
        
        if (arrivalTime > client.dueDate) {
            violations++;  // Chegou após o fim da janela!
        }
        
        serviceStart = max(arrivalTime, client.readyTime);
        currentTime = serviceStart + client.serviceTime;
    }
}

return violations;
```

---

## ✅ Processo de Validação Rigorosa

### Framework de Validação

**Script Principal:** `scripts/validate_solution_rigorous.py`

#### Entrada

1. **Arquivo de Instância:** `src/instances/solomon/C101.txt`
   - Número de clientes
   - Coordenadas (X, Y)
   - Demandas
   - Janelas de tempo [readyTime, dueDate]
   - Tempo de serviço

2. **Arquivo de Solução:** `results_validation_C1/C101/evo_c101_exec01.txt`
   - Rotas finais após 5000 gerações
   - Formato: `Veículo X: Depósito(0) -> Cliente(N) -> ... -> Depósito(0)`

#### Verificações Realizadas

##### 1. Cobertura Completa de Clientes

```python
visited_clients = set()

for route in solution.routes:
    for client_id in route:
        if client_id == 0:  # Depósito
            continue
        
        # Verificar duplicata
        if client_id in visited_clients:
            ERROR: "Cliente {client_id} visitado mais de uma vez!"
        
        visited_clients.add(client_id)

# Verificar completude
if len(visited_clients) != num_clients:
    ERROR: "Clientes faltando! Esperado: {num_clients}, Visitados: {len(visited_clients)}"
```

**Resultado:** ✅ 100 clientes visitados em TODAS as 260 execuções

##### 2. Validação de Capacidade

```python
for vehicle, route in enumerate(solution.routes):
    total_demand = 0
    
    for client_id in route:
        if client_id == 0:  # Depósito
            continue
        total_demand += clients[client_id].demand
    
    if total_demand > VEHICLE_CAPACITY:
        ERROR: "Veículo {vehicle}: demanda {total_demand}/{VEHICLE_CAPACITY}"
```

**Resultado:** ✅ 0 violações de capacidade em TODAS as 260 execuções

##### 3. Validação de Janelas de Tempo

```python
VEHICLE_SPEED = 1  # ✅ CRÍTICO: Deve ser 1!

for vehicle, route in enumerate(solution.routes):
    current_time = 0  # Início no depósito no tempo 0
    prev_location = depot
    
    for client_id in route[1:]:  # Pula depósito inicial
        if client_id == 0:  # Retorno ao depósito
            break
        
        client = clients[client_id]
        
        # 1. Calcular tempo de viagem
        distance = euclidean_distance(prev_location, client)
        travel_time = distance / VEHICLE_SPEED  # ✅ Com SPEED=1, distance=time
        
        # 2. Calcular tempo de chegada
        arrival_time = current_time + travel_time
        
        # 3. VALIDAR JANELA DE TEMPO
        if arrival_time < client.ready_time:
            # Chegou cedo → espera abrir a janela
            service_start = client.ready_time
            # ⚠️ Não é violação, mas gera tempo de espera
        elif arrival_time <= client.due_date:
            # Chegou dentro da janela → atende imediatamente
            service_start = arrival_time
            # ✅ OK!
        else:
            # Chegou após a janela fechar
            ERROR: "Veículo {vehicle} chega no cliente {client_id} no tempo "
                   "{arrival_time}, mas janela fecha em {client.due_date}!"
            # ❌ VIOLAÇÃO!
        
        # 4. Atualizar tempo atual
        current_time = service_start + client.service_time
        prev_location = client
```

**Resultado:** ✅ 0 violações de time window em TODAS as 260 execuções

#### Sumário da Validação

**Estatísticas Finais (Janeiro 2026):**

| Métrica | Valor | Status |
|---------|-------|--------|
| Instâncias testadas | 26 (C1:9, R1:9, RC1:8) | - |
| Execuções por instância | 10 | - |
| **Total de execuções** | **260** | - |
| Soluções válidas | **260/260** | ✅ 100% |
| Violações de capacidade | **0** | ✅ |
| Violações de time window | **0** | ✅ |
| Clientes faltando | **0** | ✅ |
| Clientes duplicados | **0** | ✅ |

**Conclusão Definitiva:**
> O algoritmo AEMMT com todas as correções aplicadas produz **APENAS** soluções factíveis. Todas as 260 soluções testadas respeitam:
> - Capacidade dos veículos (200 unidades)
> - Janelas de tempo (hard constraints)
> - Cobertura completa (todos os clientes visitados exatamente uma vez)

---

## 🔧 Parâmetros Atuais (Janeiro 2026)

### Principais Configurações

```java
// src/main/App.java

// População
pop_size = 900
sub_pop_size = 300                // 900/3
elitismRate = 0.1                 // 10% elite

// Mutação
mutationRate = 0.01               // 1% probabilidade de mutação
interRouteMutationRate = 1.0      // 100% quando mutação ocorre

// Evolução
numGenerations = 5000             // 5000 gerações

// Fitness
WEIGHT_DISTANCE = 1.0
WEIGHT_TIME = 0.5
WEIGHT_FUEL = 0.75
WEIGHT_NUM_VIOLATIONS = 1000.0    // Penalidade forte!

// Físico
VEHICLE_SPEED = 1                 // ✅ CRÍTICO: Padrão Solomon
vehicleCapacity = 200             // Capacidade padrão
```

### Comportamento da Mutação

**Com os parâmetros atuais:**

- A cada geração, um indivíduo tem **1% de chance** de sofrer mutação
- **Quando mutação ocorre** (aquele 1%):
  1. **SEMPRE** recebe mutação intra-rota (troca dentro da rota)
  2. **SEMPRE** recebe mutação inter-rota (move entre veículos)
     - ✅ Com validação de capacidade ANTES da troca

**Exemplo prático em uma população de 900:**
- ~9 indivíduos sofrem mutação por geração
- Todos os 9 recebem mutação intra-rota E inter-rota
- Mutações inter-rota rejeitadas se violarem capacidade

---

## 📚 Arquivos Importantes

### Código-Fonte Crítico

1. **src/main/App.java**
   - Parâmetros globais
   - Entrada do programa
   - **Linha 31:** `VEHICLE_SPEED = 1` ✅
   - **Linha 49:** `mutationRate = 0.01` ✅
   - **Linha 50:** `interRouteMutationRate = 1.0` ✅
   - **Linha 51:** `numGenerations = 5000` ✅

2. **src/genetic/Mutation.java**
   - Operadores de mutação
   - **Linhas 140-168:** Validação de capacidade ✅

3. **src/genetic/SolomonInsertion.java**
   - Inicialização conservadora
   - **Linha 273:** `return feasibleInsertions;` ✅

4. **src/genetic/Crossover.java**
   - Crossover de um ponto
   - **Linhas 180-195:** Compactação corrigida ✅

5. **src/genetic/Population.java**
   - Gerenciamento de subpopulações
   - 10 chamadas para `mutateCombined` com parâmetro `clients` ✅

### Scripts de Validação

1. **scripts/validate_solution_rigorous.py**
   - Validação completa individual
   - Verifica capacidade, time windows, cobertura

2. **scripts/run_validation_c1.py**
   - Executa 10x cada instância C1

3. **scripts/run_validation_r1.py**
   - Executa 10x cada instância R1

4. **scripts/run_validation_rc1.py**
   - Executa 10x cada instância RC1

---

## 🚨 Checklist para Modificações Futuras

Se você (IA ou humano) for modificar este código, **SEMPRE** verifique:

### Antes de Qualquer Mudança

- [ ] `VEHICLE_SPEED = 1` em **TODOS** os arquivos (App.java, Config.java)
- [ ] Solomon I1 retorna `feasibleInsertions` (não `allInsertions`)
- [ ] Mutação inter-rota VALIDA CAPACIDADE antes de trocar
- [ ] `mutateCombined` recebe parâmetro `clients`
- [ ] `denormalizeRoute` usa algoritmo de compactação (não para no primeiro zero)
- [ ] `copyIndividual` usa ordem correta: `fitness, distance, time, fuel`

### Após Qualquer Mudança

- [ ] Recompilar: `javac -d bin -sourcepath src src/main/App.java`
- [ ] Executar pelo menos 1 instância: `java -cp bin main.App`
- [ ] Validar resultado: `python3 scripts/validate_solution_rigorous.py ...`
- [ ] Verificar log: 0 violações de capacidade e time window

### Para Validação Completa

- [ ] Executar 10x pelo menos 1 instância de cada classe (C, R, RC)
- [ ] Todas as 30 execuções devem ter 0 violações
- [ ] Se qualquer execução tiver violação: **PARAR E INVESTIGAR**

---

## 🎯 Como Usar Este Documento

### Para Assistente de IA (Claude, GPT, etc.)

1. **Leia este documento INTEIRO** antes de fazer qualquer modificação
2. **Entenda o histórico de bugs** - eles podem se repetir!
3. **Use o checklist** antes e após modificações
4. **NUNCA** mude `VEHICLE_SPEED` de 1
5. **NUNCA** remova validação de capacidade da mutação inter-rota
6. **SEMPRE** valide resultados após mudanças

### Para Desenvolvedores Humanos

1. Este documento resume **1 mês de debugging intenso**
2. Cada bug corrigido causou dias de investigação
3. Os parâmetros atuais produzem **100% de soluções válidas**
4. Mudanças devem ser **testadas rigorosamente**
5. Validação não é opcional - é **obrigatória**

---

## 📊 Resultados de Referência

### Performance Atual (100% Válido)

| Classe | Instâncias | Execuções | Válidas | Gap Médio |
|--------|-----------|-----------|---------|-----------|
| C1     | 9         | 90        | 90/90   | 57.44%    |
| R1     | 9         | 90        | 90/90   | 27.33%    |
| RC1    | 8         | 80        | 80/80   | 28.46%    |
| **TOTAL** | **26** | **260** | **260/260** | **37.74%** |

### Exemplo de Resultado Válido

**C101 - Execução 01:**
```
Distância: 1260.16
Veículos: 13
Clientes: 100/100 ✅
Violações capacidade: 0 ✅
Violações time window: 0 ✅
Fitness ponderado: 15371.72
```

**Validação:**
```bash
$ python3 scripts/validate_solution_rigorous.py \
    src/instances/solomon/C101.txt \
    results_validation_C1/C101/evo_c101_exec01.txt

✅ Todos os 100 clientes visitados exatamente uma vez
✅ 0 violações de capacidade
✅ 0 violações de janelas de tempo
✅ SOLUÇÃO VÁLIDA!
```

---

## 🔄 Versão NSGA-III

**Localização:** `VRP_NSGA_TCC/` (se existir)

### Status

Todos os mesmos bugs foram corrigidos na versão NSGA-III:
- ✅ VEHICLE_SPEED = 1
- ✅ Solomon I1 conservador
- ✅ Mutação inter-rota com validação de capacidade
- ✅ Parâmetros: mutationRate=0.01, interRouteMutationRate=1.0, generations=5000

### Diferença Principal

- Usa framework JMetal
- Algoritmo NSGA-III ao invés de subpopulações customizadas
- Resto do código (mutação, crossover, inicialização) é **idêntico**

---

## 📖 Referências

### Artigos Base

- Solomon, M. M. (1987). "Algorithms for the Vehicle Routing and Scheduling Problems with Time Window Constraints"
- Deb, K., & Jain, H. (2014). "An Evolutionary Many-Objective Optimization Algorithm Using Reference-Point-Based Nondominated Sorting Approach" (NSGA-III)

### Instâncias Benchmark

- Solomon VRPTW Instances: https://www.sintef.no/projectweb/top/vrptw/solomon-benchmark/
- Formato: 100 clientes, capacidade 200, janelas de tempo variadas
- Classes: C (clustered), R (random), RC (random-clustered)
- Variantes: 1 (short time windows), 2 (long time windows)

---

**FIM DO DOCUMENTO**

**Última Revisão:** 15 de Janeiro de 2026  
**Validação:** 260/260 soluções válidas (100%)  
**Mantido por:** Claude Code Assistant
