# Correções de Bugs Críticos - Dezembro 2025

## Problema Inicial

O algoritmo genético multi-objetivo estava **perdendo clientes** durante o processo evolutivo. Especificamente:
- Instância C102: deveria ter **100 clientes** atendidos
- Resultado final: apenas **34-90 clientes** nas rotas finais
- Sintoma: "há varios clientes que não foram atendidos na rota final"

## Investigação e Bugs Descobertos

### 1. 🔴 BUG CRÍTICO: `denormalizeRoute()` em Crossover.java

**Localização**: `src/genetic/Crossover.java`, método `denormalizeRoute()`

**Problema**: 
O método parava de copiar clientes ao encontrar o **primeiro zero** no array:

```java
// CÓDIGO BUGADO (ANTIGO)
for (c = 0; c < App.numClients; c++) {
    if (route[v][c] == 0 && c > 0) {
        break;  // ❌ Para aqui!
    }
    denormalizedRoute[v][c] = route[v][c];
}
```

**Por que isso causava perda de clientes?**

1. `repairRoute()` criava rotas normalizadas: `[0, cliente1, cliente2, 0, 0, ...]`
2. `insertClientAnywhere()` adicionava clientes faltantes em **qualquer posição vazia**
3. Resultado: `[0, 5, 10, 15, 0, **82**, 0, ...]` ← cliente 82 **depois** do zero!
4. `denormalizeRoute()` parava na posição 4 (primeiro zero) → cliente 82 **nunca era copiado**
5. Cliente perdido permanentemente! 💀

**Solução Aplicada**:
Algoritmo de **compactação** que varre o array inteiro:

```java
// CÓDIGO CORRIGIDO (NOVO)
int pos = 0;
for (int c = 0; c < App.numClients; c++) {
    if (route[v][c] != 0 && route[v][c] != -1) {
        denormalizedRoute[v][pos++] = route[v][c];  // ✅ Compacta tudo!
    }
}
// Preenche o resto com -1
while (pos < App.numClients) {
    denormalizedRoute[v][pos++] = -1;
}
```

**Impacto**: ✅ Todos os 100 clientes agora são preservados durante o crossover


---

### 2. 🔴 BUG CRÍTICO: `copyIndividual()` em App.java

**Localização**: `src/main/App.java`, método `copyIndividual()`

**Problema**:
Ordem **errada** dos parâmetros no construtor de `Individual` embaralhava todos os valores de fitness:

```java
// CÓDIGO BUGADO (ANTIGO)
Individual copy = new Individual(
    source.getId(),
    source.getFitnessDistance(),  // ❌ Errado! Isso vai para 'fitness'
    source.getFitnessTime(),      // ❌ Errado! Isso vai para 'fitnessDistance'
    source.getFitnessFuel(),      // ❌ Errado! Isso vai para 'fitnessTime'
    source.getFitness()           // ❌ Errado! Isso vai para 'fitnessFuel'
);
```

**Construtor correto de Individual**:
```java
public Individual(int id, double fitness, double fitnessDistance, 
                  double fitnessTime, double fitnessFuel)
```

**Solução Aplicada**:

```java
// CÓDIGO CORRIGIDO (NOVO)
Individual copy = new Individual(
    source.getId(),
    source.getFitness(),          // ✅ Correto!
    source.getFitnessDistance(),  // ✅ Correto!
    source.getFitnessTime(),      // ✅ Correto!
    source.getFitnessFuel()       // ✅ Correto!
);
```

**Impacto**: ✅ Fitness dos indivíduos agora são copiados corretamente


---

### 3. 🟡 BUG: Confusão com `App.numClients`

**Problema**:
Confusão sobre o significado de `App.numClients`:
- **É o tamanho do array** (101 posições: índices 0-100)
- **NÃO é a contagem de clientes** (que são 100: IDs 1-100)
- Índice 0 = depósito
- Índices 1-100 = clientes reais

**Bugs relacionados**:
- Loops usando `<= App.numClients` causavam `ArrayIndexOutOfBoundsException`
- Validações verificando clientes 1 a 101 em vez de 1 a 100

**Solução Aplicada**:
- Todos os loops corrigidos para `< App.numClients`
- Validações corrigidas para verificar clientes de `1` até `App.numClients - 1`
- Arrays mantidos como `new int[numVehicles][numClients]` (101 posições)

**Impacto**: ✅ Sem mais erros de índice fora dos limites


---

### 4. 🟢 BUG DE APRESENTAÇÃO: Seleção do melhor indivíduo final

**Localização**: `src/main/App.java`, linha ~451

**Problema**:
Pegava o melhor indivíduo da **subpopulação errada** para exibir no resultado final:

```java
// CÓDIGO BUGADO (ANTIGO)
finalBestIndividual = population.getSubPopPonderation().stream()  // ❌ Errado!
        .min(Comparator.comparingDouble(Individual::getFitness))
        .map(ind -> copyIndividual(ind))
        .orElse(null);
```

**Solução Aplicada**:

```java
// CÓDIGO CORRIGIDO (NOVO)
finalBestIndividual = population.getSubPopDistance().stream()  // ✅ Correto!
        .min(Comparator.comparingDouble(Individual::getFitnessDistance))
        .map(ind -> copyIndividual(ind))
        .orElse(null);
```

**Impacto**: 
- ✅ Agora exibe o melhor indivíduo em **distância** (não ponderação)
- ✅ Valor `subPopDistance` bate com `Distância total` no arquivo de resultados
- ⚠️ **NÃO afeta a evolução** - apenas a apresentação do resultado


---

### 5. 🔴 BUG CRÍTICO: Cálculo de fitness para veículos com 1 cliente

**Data de Descoberta**: 27/12/2025 após primeira rodada de validação

**Localização**: 
- `src/genetic/DistanceFitnessCalculator.java`, método `calculateFitness()`
- `src/genetic/FuelFitnessCalculator.java`, método `calculateFitness()`

**Problema**:
Quando um veículo tinha **apenas 1 cliente**, o cálculo de fitness **ignorava completamente esse veículo**, não incluindo as distâncias depot→cliente→depot:

```java
// CÓDIGO BUGADO (ANTIGO)
for (int c = 0; c < App.numClients - 1; c++) {
    int currentClientId = individual.getRoute()[v][c];
    int nextClientId = individual.getRoute()[v][c + 1];
    
    if (currentClientId == -1 || nextClientId == -1)
        break;  // ❌ Para antes de salvar firstClient!
    
    if (firstClient == null) {
        firstClient = currentClient;
    }
    lastClient = nextClient;
    // ...
}

if (firstClient != null) {  // ❌ Nunca executa para veículo com 1 cliente!
    vehicleDistance += calculateDistance(depot, firstClient);
    vehicleDistance += calculateDistance(lastClient, depot);
}
```

**Por que isso causava discrepância?**

Exemplo real de C101 exec01:
```
Veículo 1: Depósito(0) -> Cliente(80) -> Depósito(0)
    Distância: 102,96
```

Array da rota: `[80, -1, -1, ...]`
- Loop começa: c=0, `currentClientId=80`, `nextClientId=-1`
- **Break imediatamente!** antes de salvar `firstClient`
- `firstClient` permanece `null`
- `if (firstClient != null)` não executa
- **Distância de 102,96 não é somada!**

**Resultado**: 
- `subPopDistance` = 951,23 (sem os 102,96 do veículo 1)
- `Distância total` calculada manualmente = 1054,19
- **Diferença = 102,96** (exatamente a distância do veículo com 1 cliente!)

**Solução Aplicada**:
Novo algoritmo que **primeiro coleta todos os clientes**, depois calcula distâncias:

```java
// CÓDIGO CORRIGIDO (NOVO)
// Collect all clients in this vehicle's route
List<Integer> routeClients = new ArrayList<>();
for (int c = 0; c < App.numClients - 1; c++) {
    int clientId = individual.getRoute()[v][c];
    if (clientId == -1)
        break;
    routeClients.add(clientId);
}

// If vehicle has no clients, skip
if (routeClients.isEmpty()) {
    continue;
}

// Calculate distance: depot -> first client
Client firstClient = clients.get(routeClients.get(0));
vehicleDistance += calculateDistance(depot, firstClient);

// Calculate distances between consecutive clients
for (int i = 0; i < routeClients.size() - 1; i++) {
    Client currentClient = clients.get(routeClients.get(i));
    Client nextClient = clients.get(routeClients.get(i + 1));
    vehicleDistance += calculateDistance(currentClient, nextClient);
}

// Calculate distance: last client -> depot
Client lastClient = clients.get(routeClients.get(routeClients.size() - 1));
vehicleDistance += calculateDistance(lastClient, depot);
```

**Impacto**: 
- ✅ Todos os veículos agora são contabilizados corretamente
- ✅ `subPopDistance` agora bate com `Distância total`
- ✅ Valores de fitness são **precisos** durante toda a evolução

**Nota**: O `TimeFitnessCalculator` já estava correto, pois calculava a distância depot→first **antes** do loop principal.


---

## Validação dos Fixes

### Antes das Correções (Bugs 1-4):
```
--- ROTAS FINAL ---
  ❌ INVÁLIDO - 34/100 clientes atendidos
     Clientes faltando: 66
```

### Depois das Correções (Bugs 1-4):
```
--- ROTAS FINAL ---
  ✅ VÁLIDO - Todos os 100 clientes atendidos
     Total de menções: 100
     Clientes únicos: 100
```

### Antes da Correção do Bug 5 (Fitness):
```
C101 exec01:
  subPopDistance (g2999): 951,23
  Distância total (rota final): 1054,19
  ❌ DISCREPÂNCIA de 102,96 km!
```

### Depois da Correção do Bug 5 (Fitness):
```
C101 exec10:
  subPopDistance (g2999): 959,92
  Distância total (rota final): 959,92
  ✅ VALORES BATEM PERFEITAMENTE!
```


---

## Arquivos Modificados

1. **src/genetic/Crossover.java**
   - `denormalizeRoute()`: Reescrito completamente com algoritmo de compactação
   - `childRoute`: Inicialização corrigida (usa 0 em vez de -1)
   - `insertClientAtPosition()`: Verifica tanto 0 quanto -1
   - `insertClientAnywhere()`: Verifica tanto 0 quanto -1

2. **src/main/App.java**
   - `copyIndividual()`: Ordem de parâmetros corrigida
   - Seleção de `finalBestIndividual`: Mudado para `subPopDistance`

3. **src/genetic/Individual.java**
   - Loops de validação corrigidos (`<= App.numClients` → `< App.numClients`)

4. **src/genetic/Population.java**
   - 8+ loops corrigidos para evitar `ArrayIndexOutOfBoundsException`

5. **src/genetic/KMeansClusteringInitializer.java**
   - Loops de criação de clusters corrigidos

6. **src/genetic/DistanceFitnessCalculator.java** (Bug 5)
   - `calculateFitness()`: Reescrito para coletar clientes primeiro, depois calcular distâncias
   - Adiciona `import java.util.ArrayList`
   - **Garante que veículos com 1 cliente são contabilizados corretamente**

7. **src/genetic/FuelFitnessCalculator.java** (Bug 5)
   - `calculateFitness()`: Mesma correção que DistanceFitnessCalculator
   - Adiciona `import java.util.ArrayList`
   - **Garante cálculo correto de combustível para todos os veículos**

**Nota**: `TimeFitnessCalculator.java` **não** precisou de correção pois já estava implementado corretamente.


---

## Resumo Final

| Bug | Severidade | Status | Impacto |
|-----|-----------|--------|---------|
| 1. denormalizeRoute() | 🔴 CRÍTICO | ✅ CORRIGIDO | Perda de clientes durante crossover |
| 2. copyIndividual() | 🔴 CRÍTICO | ✅ CORRIGIDO | Fitness embaralhado na cópia |
| 3. App.numClients | 🟡 MODERADO | ✅ CORRIGIDO | ArrayIndexOutOfBounds |
| 4. finalBestIndividual | 🟢 BAIXO | ✅ CORRIGIDO | Exibição incorreta do resultado |
| 5. Fitness com 1 cliente | 🔴 CRÍTICO | ✅ CORRIGIDO | Fitness impreciso durante evolução |

**Resultado**: 
- ✅ **100/100 clientes** preservados em todas as gerações
- ✅ **Sem erros** de índice ou ArrayIndexOutOfBounds
- ✅ **Fitness preciso** durante toda a evolução
- ✅ **Validação bem-sucedida** em todas as instâncias C1 (C101-C109)
   - Loops e validações de array corrigidos

---

## Estado Final do Código

✅ **Todos os 100 clientes são preservados** durante todo o processo evolutivo  
✅ **Sem erros de ArrayIndexOutOfBounds**  
✅ **Fitness calculados e copiados corretamente**  
✅ **Resultados exibidos consistentemente**  
✅ **Validação automática passando**

---

## Comandos de Teste

```bash
# Compilar
javac -d bin -sourcepath src src/main/App.java

# Executar C102
bash run_single_instance.sh 2

# Validar rotas
python3 scripts/validate_routes.py resultsMulti/evo_c102.txt

# Gerar mapas
./generate_route_maps.sh c102
```

---

## Notas Importantes para Contexto Futuro

1. **`App.numClients = 101`** é o tamanho do array, NÃO a contagem de clientes
2. **Clientes reais**: IDs de 1 a 100 (índice 0 é o depósito)
3. **Valores vazios**: `-1` em rotas desnormalizadas, `0` em rotas normalizadas
4. **Crossover**: Processo envolve normalização → crossover → reparo → desnormalização
5. **O bug do `denormalizeRoute()`** era o mais crítico - perdia clientes aleatoriamente

---

## Data da Correção

**26 de Dezembro de 2025**

Todos os bugs foram identificados, corrigidos e validados com sucesso.
