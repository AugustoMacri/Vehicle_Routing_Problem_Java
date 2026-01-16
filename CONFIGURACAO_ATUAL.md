# Configuração Atual do Projeto VRP

**Última Atualização:** 15 de Janeiro de 2026

---

## 📋 Visão Geral do Algoritmo

Este projeto implementa um **Algoritmo Evolutivo Multi-Objetivo com Multi-Treino (AEMMT)** para resolver o Vehicle Routing Problem with Time Windows (VRPTW) usando instâncias benchmark de Solomon.

### Características Principais

- **Abordagem Multi-Objetivo**: Otimiza simultaneamente distância, tempo e combustível
- **Inicialização Inteligente**: Combinação de Solomon I1 (conservador) e clustering
- **Operadores Genéticos Avançados**: Mutação intra-rota e inter-rota com validação de capacidade
- **4 Subpopulações Especializadas**: Uma para cada objetivo (distância, tempo, combustível, ponderado)
- **Validação Rigorosa**: 100% de soluções válidas (260/260 testes bem-sucedidos)

---

## ⚙️ Parâmetros Atuais

### Parâmetros do Algoritmo Genético
```java
// Em: src/main/App.java
pop_size = 900                    // População total
sub_pop_size = 300                // Tamanho de cada subpopulação (900/3)
elitismRate = 0.1                 // 10% de elitismo
mutationRate = 0.01               // 1% de probabilidade de mutação
interRouteMutationRate = 1.0      // Sempre aplica mutação inter-rota quando houver mutação
numGenerations = 5000             // Número de gerações
```

### Parâmetros de Fitness
```java
WEIGHT_DISTANCE = 1.0             // Peso da distância
WEIGHT_TIME = 0.5                 // Peso do tempo
WEIGHT_FUEL = 0.75                // Peso do combustível
WEIGHT_NUM_VIOLATIONS = 1000.0    // Penalidade alta para violações de janelas de tempo
VEHICLE_SPEED = 1                 // Velocidade padrão Solomon (distância = tempo)
```

### Parâmetros de Crossover e Seleção
```java
CROSSOVER_PROBABILITY = 1.0       // Sempre aplica crossover
tournamentSize = 2                // Seleção por torneio de 2
QUANTITYSELECTEDTOURNAMENT = 2    // Quantidade de indivíduos selecionados
```

---

## 🧬 Inicialização da População

### Método Solomon I1 (Conservador)

**Arquivo:** `src/genetic/SolomonInsertion.java`

**Características:**
- Heurística construtiva baseada no trabalho de Solomon (1987)
- **Modo Conservador:** Só aceita inserções **factíveis** (sem violações de janela de tempo)
- Cria nova rota quando não há inserção válida
- Garante soluções 100% válidas desde o início

**Funcionamento:**

1. **Seleção do Cliente Semente:**
   - Cliente não visitado mais distante do depósito
   - Critério c1: maximiza distância `d(0,u) - c2 * t(0,u)`
   
2. **Inserção Sequencial:**
   - Para cada cliente não visitado, calcula critério c2:
     ```
     c2(i,u,j) = λ * d(i,u) + (1-λ) * d(u,j) - μ * d(i,j) + β * ΔT(u)
     ```
   - λ = 1.0, μ = 1.0, β = 1.0 (valores padrão Solomon)
   - **CONSERVADOR:** Retorna apenas inserções com `ΔT >= 0` (sem violação)
   
3. **Nova Rota:**
   - Se nenhuma inserção é válida, cria novo veículo

**Código-chave (linha 273):**
```java
return feasibleInsertions; // Retorna apenas inserções válidas
```

---

## 🔄 Operadores Genéticos

### 1. Mutação Intra-Rota

**Arquivo:** `src/genetic/Mutation.java` - Método `mutate()`

**Funcionamento:**
- Troca posições de **dois clientes dentro da mesma rota**
- Probabilidade de aplicação: `mutationRate` (1%)
- Só atua em rotas com 4+ clientes

**Algoritmo:**
```java
1. Decide se mutação ocorre (1% de chance)
2. Para cada veículo:
   - Identifica clientes válidos (ignora depósito e posições vazias)
   - Se >= 4 clientes:
     - Seleciona dois clientes aleatórios diferentes
     - Troca suas posições
```

### 2. Mutação Inter-Rota (COM VALIDAÇÃO DE CAPACIDADE)

**Arquivo:** `src/genetic/Mutation.java` - Método `mutateInterRoute()`

**Características:**
- Move clientes **entre veículos diferentes**
- **CRÍTICO:** Valida capacidade ANTES de aceitar a troca
- Essencial para escapar de ótimos locais

**Algoritmo:**
```java
1. Decide se mutação ocorre (baseado em mutationRate)
2. Seleciona 2 veículos diferentes com clientes
3. Seleciona 1 cliente de cada veículo
4. **NOVO:** Calcula demandas atuais:
   - demand1 = soma demandas veículo 1
   - demand2 = soma demandas veículo 2
5. **NOVO:** Calcula demandas após troca:
   - newDemand1 = demand1 - clientDemand1 + clientDemand2
   - newDemand2 = demand2 - clientDemand2 + clientDemand1
6. **NOVO:** Valida capacidade:
   - Se newDemand1 > vehicleCapacity OU newDemand2 > vehicleCapacity:
     - REJEITA a mutação (return)
   - Senão:
     - Executa a troca
```

**Código-chave (linhas 140-162):**
```java
// Calculate current demands for both vehicles
double demand1 = 0, demand2 = 0;
for (int c = 0; c < App.numClients - 1; c++) {
    if (route[vehicle1][c] != -1 && route[vehicle1][c] != 0) {
        demand1 += clients.get(route[vehicle1][c]).getDemand();
    }
    if (route[vehicle2][c] != -1 && route[vehicle2][c] != 0) {
        demand2 += clients.get(route[vehicle2][c]).getDemand();
    }
}

// Get demands of clients to swap
int clientId1 = route[vehicle1][client1Pos];
int clientId2 = route[vehicle2][client2Pos];
double clientDemand1 = clients.get(clientId1).getDemand();
double clientDemand2 = clients.get(clientId2).getDemand();

// Calculate new demands after swap
double newDemand1 = demand1 - clientDemand1 + clientDemand2;
double newDemand2 = demand2 - clientDemand2 + clientDemand1;

// Only swap if both vehicles remain within capacity
if (newDemand1 > App.vehicleCapacity || newDemand2 > App.vehicleCapacity) {
    return; // Skip mutation to preserve capacity constraint
}
```

### 3. Mutação Combinada

**Arquivo:** `src/genetic/Mutation.java` - Método `mutateCombined()`

**Estratégia:**
```java
1. SEMPRE aplica mutação intra-rota (mutate)
2. Com probabilidade interRouteMutationRate (100%):
   - Aplica mutação inter-rota (mutateInterRoute)
```

**Resultado Prático:**
- Quando um indivíduo é selecionado para mutação (1%):
  - 100% recebe mutação intra-rota
  - 100% recebe TAMBÉM mutação inter-rota

### 4. Crossover de Um Ponto

**Arquivo:** `src/genetic/Crossover.java`

**Características:**
- Combina rotas de dois pais
- Ponto de corte aleatório
- **CORREÇÃO APLICADA:** Normalização e desnormalização corrigidas (não perde clientes)

**Algoritmo:**
```java
1. Seleciona ponto de corte aleatório
2. Copia rotas do Pai1 até o ponto de corte
3. Copia rotas do Pai2 após o ponto de corte
4. repairRoute(): Remove duplicatas e clientes inválidos
5. insertClientAnywhere(): Reinsere clientes faltantes
6. denormalizeRoute(): Compacta rotas (CORRIGIDO - não perde clientes!)
```

**BUG CORRIGIDO em `denormalizeRoute()`:**
```java
// ANTES (BUGADO): Parava no primeiro zero, perdia clientes
for (c = 0; c < App.numClients; c++) {
    if (route[v][c] == 0 && c > 0) break; // ❌
    denormalizedRoute[v][c] = route[v][c];
}

// DEPOIS (CORRETO): Compacta todos os clientes
int pos = 0;
for (int c = 0; c < App.numClients; c++) {
    if (route[v][c] != 0 && route[v][c] != -1) {
        denormalizedRoute[v][pos++] = route[v][c]; // ✅
    }
}
while (pos < App.numClients) {
    denormalizedRoute[v][pos++] = -1;
}
```

---

## 📊 Sistema de Subpopulações

### Estrutura

4 subpopulações especializadas (300 indivíduos cada):

1. **SubpopDistance**: Otimiza distância
2. **SubpopTime**: Otimiza tempo
3. **SubpopFuel**: Otimiza combustível
4. **SubpopPonderation**: Otimiza fitness ponderado

### Evolução Paralela

Cada subpopulação:
- Mantém seus próprios indivíduos
- Aplica seleção, crossover e mutação independentemente
- Ordena por seu objetivo específico
- Aplica elitismo (10% melhores preservados)

### Integração

- A cada geração, as subpopulações trocam indivíduos (migração implícita via seleção global)
- Resultado final: melhor indivíduo entre todas as subpopulações

---

## ✅ Validação de Soluções

### Validação Rigorosa Implementada

**Script:** `scripts/validate_solution_rigorous.py`

**Verificações:**

1. **Cobertura Completa:**
   - Todos os N clientes visitados exatamente uma vez
   - Nenhum cliente duplicado
   - Nenhum cliente faltando

2. **Capacidade dos Veículos:**
   - Para cada rota: `∑ demands <= vehicleCapacity`
   - Limite padrão: 200 unidades

3. **Janelas de Tempo (Hard Constraints):**
   - Para cada cliente i visitado no tempo t:
     - `readyTime[i] <= t <= dueDate[i]`
   - Considera tempo de viagem e tempo de serviço
   - **VELOCIDADE = 1** (padrão Solomon: distância = tempo)

4. **Cálculo Correto de Tempo de Chegada:**
   ```python
   arrival_time = max(
       departure_from_previous + travel_time,
       client.ready_time  # Pode esperar se chegar antes da janela
   )
   service_start = arrival_time
   departure = service_start + client.service_time
   ```

### Resultados da Validação

**Total testado:** 260 execuções (26 instâncias × 10 execuções)

| Categoria | Resultado |
|-----------|-----------|
| Soluções Válidas | **260/260 (100%)** ✅ |
| Violações de Capacidade | **0** ✅ |
| Violações de Time Window | **0** ✅ |
| Clientes Faltando | **0** ✅ |

**Conclusão:** Algoritmo produz apenas soluções factíveis!

---

## 🐛 Bugs Corrigidos (Histórico)

### 1. Perda de Clientes no Crossover (Dezembro 2025)

**Problema:**
- `denormalizeRoute()` parava no primeiro zero
- Clientes inseridos após zeros eram perdidos
- C102: 34-90 clientes ao invés de 100

**Solução:**
- Algoritmo de compactação que varre array completo
- Arquivo: `src/genetic/Crossover.java`, linha 180

### 2. Embaralhamento de Fitness em copyIndividual() (Dezembro 2025)

**Problema:**
- Ordem errada dos parâmetros no construtor
- Fitness trocados entre si

**Solução:**
- Corrigir ordem: `fitness, fitnessDistance, fitnessTime, fitnessFuel`
- Arquivo: `src/main/App.java`, linha 157

### 3. VEHICLE_SPEED Incorreto (Janeiro 2026)

**Problema:**
- `VEHICLE_SPEED = 50` ao invés de `1`
- Tempos calculados 50x menores
- Janelas de tempo nunca violadas (falso positivo)

**Solução:**
- `VEHICLE_SPEED = 1` (padrão Solomon)
- Arquivos: `src/main/App.java`, `src/configuration/Config.java`

### 4. Solomon I1 Não-Conservador (Janeiro 2026)

**Problema:**
- `getBestInsertion()` retornava inserções com violação
- Código morto `bestWithViolation` nunca usado
- Soluções iniciais inválidas

**Solução:**
- Retornar apenas `feasibleInsertions`
- Arquivo: `src/genetic/SolomonInsertion.java`, linha 273

### 5. Mutação Inter-Rota Sem Validação de Capacidade (Janeiro 2026)

**Problema:**
- Trocas entre veículos ignoravam capacidade
- Todas as execuções C101 tinham rota com 230/200
- Soluções finais inválidas

**Solução:**
- Validação de capacidade ANTES da troca
- Rejeita mutações que violam capacidade
- Arquivo: `src/genetic/Mutation.java`, linhas 140-162

---

## 📂 Estrutura de Arquivos

### Código Principal

```
src/
├── main/
│   └── App.java                      # Configuração e entrada do programa
├── genetic/
│   ├── Population.java               # Gerenciamento de subpopulações
│   ├── Individual.java               # Representação de soluções
│   ├── Crossover.java                # Operador de crossover (CORRIGIDO)
│   ├── Mutation.java                 # Operadores de mutação (COM VALIDAÇÃO)
│   ├── SolomonInsertion.java         # Inicialização conservadora
│   └── fitness/
│       ├── DistanceFitnessCalculator.java
│       ├── TimeFitnessCalculator.java
│       ├── FuelFitnessCalculator.java
│       └── DefaultFitnessCalculator.java  # Fitness ponderado
├── vrp/
│   ├── Client.java                   # Modelo de cliente
│   └── Solution.java                 # Modelo de solução
└── configuration/
    └── Config.java                   # Configurações globais
```

### Scripts de Validação

```
scripts/
├── run_validation_c1.py             # Executa 10x instâncias C1
├── run_validation_r1.py             # Executa 10x instâncias R1
├── run_validation_rc1.py            # Executa 10x instâncias RC1
├── validate_solution_rigorous.py    # Validação rigorosa individual
├── validate_capacity.py             # Validação de capacidade
└── plot_route_maps.py               # Visualização de rotas
```

---

## 🚀 Como Executar

### Compilação

```bash
javac -d bin -sourcepath src src/main/App.java
```

### Execução Individual

```bash
java -cp bin main.App
# Escolha opção 1 (Multi-objetivo)
# Escolha instância (ex: 1 para C101)
```

### Execução em Lote (Validação C1)

```bash
cd scripts
python3 run_validation_c1.py
```

### Validação de Resultados

```bash
python3 scripts/validate_solution_rigorous.py \
    src/instances/solomon/C101.txt \
    results_validation_C1/C101/evo_c101_exec01.txt
```

---

## 📈 Resultados Atuais

### Gap em Relação ao Best-Known

| Classe | Gap Médio | Melhor Instância |
|--------|-----------|------------------|
| C1     | 57.44%    | C104 (49.95%)   |
| R1     | 27.33%    | R101 (17.80%)   |
| RC1    | 28.46%    | RC105 (15.07%)  |

### Observações

- **Classe R1:** Melhor performance (clientes aleatórios, mais flexibilidade)
- **Classe C1:** Maior gap (clusters com janelas muito restritas)
- **Estabilidade:** Desvio padrão baixo (~4-10 unidades)
- **Validade:** 100% de soluções factíveis

---

## 🔄 Versão NSGA-III (Paralela)

**Localização:** Pasta `VRP_NSGA_TCC` (se existir)

### Diferenças

- Usa framework JMetal
- Algoritmo NSGA-III ao invés de subpopulações customizadas
- **Mesmos parâmetros** aplicados:
  - `mutationRate = 0.01`
  - `interRouteMutationRate = 1.0`
  - `numGenerations = 5000`
  - `VEHICLE_SPEED = 1`
  - Mutação com validação de capacidade
  - Solomon I1 conservador

### Status

- Todos os bugs críticos corrigidos
- Compilação OK
- Pronto para execução comparativa

---

## 📝 Notas Importantes

1. **Sempre recompilar após mudanças:** `javac -d bin -sourcepath src src/main/App.java`
2. **Validar soluções:** Use `validate_solution_rigorous.py` para garantir factibilidade
3. **VEHICLE_SPEED = 1:** Crítico para cálculo correto de tempos
4. **Mutação inter-rota:** Essencial para qualidade, mas DEVE validar capacidade
5. **Solomon I1 conservador:** Garante soluções iniciais 100% válidas

---

**Documento Mantido Por:** Claude Code Assistant  
**Histórico de Bugs:** Ver seção "Bugs Corrigidos"  
**Validação:** 260/260 soluções válidas (Jan 2026)
