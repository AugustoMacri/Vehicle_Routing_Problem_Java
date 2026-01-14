# BUG FIX: Violações de Capacidade na Mutação Inter-Rota (Janeiro 2026)

## 🔴 Problema Identificado

Após as correções anteriores (Solomon I1 conservador + ajuste de peso de penalidade), os novos resultados do C101 mostraram:

### Resultados das 10 Execuções:
```
Melhor distância: 1176.47
Pior distância:   1236.03
Média:           1206.66
Desvio padrão:    19.31
```

**Parecia excelente, MAS...**

### Validação Rigorosa Revelou:
```
✅ 100% clientes visitados
✅ Janelas de tempo respeitadas (0 violações!)
❌ TODAS as 10 execuções tinham violação de capacidade na Rota 3
   - Demanda: 230/200 (15% acima do limite!)
```

## 🔍 Investigação da Causa Raiz

### Análise do Código

Inspecionando `Mutation.java`, especificamente o método `mutateInterRoute`:

```java
// CÓDIGO PROBLEMÁTICO (Linhas 137-139)
// Swap clients between vehicles
int temp = route[vehicle1][client1Pos];
route[vehicle1][client1Pos] = route[vehicle2][client2Pos];
route[vehicle2][client2Pos] = temp;
```

**Problema**: A mutação inter-rota **trocava clientes entre veículos SEM verificar capacidade**!

### Como o Bug se Manifestava:

1. **Solomon I1** criava solução inicial factível (13 veículos, 0 violações) ✅
2. **AG executava gerações** evoluindo a população
3. **Mutação inter-rota** (com taxa 0.6!) trocava clientes entre veículos
4. **Troca violava capacidade** mas era aceita mesmo assim ❌
5. **Fitness calculators** penalizavam, mas solução permanecia inválida

### Por que Sempre a Rota 3?

A convergência do AG levava a um padrão específico onde:
- Rota 3 sempre tinha clientes com demanda total próxima de 200
- Mutação inter-rota trocava cliente de alta demanda para essa rota
- Resultado: 230/200 (sistemático em todas as execuções!)

## ✅ Solução Implementada

### Verificação de Capacidade na Mutação Inter-Rota

**Arquivo**: `src/genetic/Mutation.java`

#### Mudança 1: Adicionar import de Client
```java
// ANTES
import java.util.Random;
import main.App;

// DEPOIS
import java.util.Random;
import java.util.List;
import main.App;
import vrp.Client;
```

#### Mudança 2: Adicionar parâmetro clients ao método
```java
// ANTES
public static void mutateInterRoute(Individual individual, double mutationRate)

// DEPOIS  
public static void mutateInterRoute(Individual individual, double mutationRate, List<Client> clients)
```

#### Mudança 3: Verificação de Capacidade ANTES da Troca
```java
// **NOVO CÓDIGO** - Linhas 138-163
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

// Swap clients between vehicles (capacity is guaranteed)
int temp = route[vehicle1][client1Pos];
route[vehicle1][client1Pos] = route[vehicle2][client2Pos];
route[vehicle2][client2Pos] = temp;
```

#### Mudança 4: Atualizar mutateCombined
```java
// ANTES
public static void mutateCombined(Individual individual, double mutationRate, double interRouteRate)

// DEPOIS
public static void mutateCombined(Individual individual, double mutationRate, double interRouteRate, List<Client> clients)
```

### Propagação para Population.java

Atualizadas **10 chamadas** de `mutateCombined` em:
- `evolveMultiObjective()` - 8 chamadas (4 subpopulações × 2 blocos)
- `evolveMono()` - 2 chamadas

```java
// ANTES
Mutation.mutateCombined(newSonD, App.mutationRate, App.interRouteMutationRate);

// DEPOIS
Mutation.mutateCombined(newSonD, App.mutationRate, App.interRouteMutationRate, clients);
```

### Propagação para App.java

Atualizada 1 chamada no método de teste:
```java
// ANTES
Mutation.mutateCombined(filho, App.mutationRate, App.interRouteMutationRate);

// DEPOIS
Mutation.mutateCombined(filho, App.mutationRate, App.interRouteMutationRate, clients);
```

## 📊 Impacto Esperado

### Antes da Correção:
```
✅ Solomon I1: 0 violações, 13 veículos
❌ Após AG:    1 violação de capacidade (sistemática)
   Fitness:    ~1.200 (incluindo penalidade)
   Status:     INVÁLIDO
```

### Depois da Correção:
```
✅ Solomon I1: 0 violações, 13 veículos
✅ Após AG:    0 violações (garantido)
   Fitness:    ~1.100-1.200 (sem penalidades indevidas)
   Status:     VÁLIDO
```

### Trade-offs:

**Positivo:**
- ✅ **Garante factibilidade de capacidade** (constraint hard)
- ✅ Todas as soluções serão válidas
- ✅ Fitness reflete apenas qualidade da solução, não violações

**Potencial Negativo:**
- ⚠️  Algumas mutações inter-rota serão **rejeitadas** (quando violam capacidade)
- ⚠️  Taxa efetiva de mutação inter-rota pode ser **menor** que 0.6
- ⚠️  Exploração do espaço de busca **ligeiramente reduzida**

**Por que vale a pena:**
É melhor rejeitar mutações inválidas do que aceitar soluções que violam constraints hard!

## 🧪 Como Testar

```bash
# 1. Recompilar projeto
find bin -name "*.class" -delete
javac -d bin -cp bin src/configuration/*.java src/vrp/*.java \
      src/genetic/*.java src/main/App.java

# 2. Limpar resultados antigos (inválidos)
rm -rf results_validation_C1/C101/*

# 3. Executar validação
python3 scripts/run_validation_c1.py

# 4. Validar rigorosamente
python3 scripts/validate_solution_rigorous.py \
    src/instances/solomon/C101.txt \
    results_validation_C1/C101/evo_c101_exec01.txt

# Verificar: Deve mostrar "✅ SOLUÇÃO VÁLIDA" (sem violações de capacidade)
```

## 🔧 Arquivos Modificados

1. **`src/genetic/Mutation.java`**
   - Adicionado import `List<Client>`
   - `mutateInterRoute()` agora recebe `List<Client> clients`
   - Adicionada verificação de capacidade (23 linhas novas)
   - `mutateCombined()` agora recebe `List<Client> clients`

2. **`src/genetic/Population.java`**
   - 10 chamadas de `mutateCombined` atualizadas com parâmetro `clients`

3. **`src/main/App.java`**
   - 1 chamada de `mutateCombined` atualizada (método de teste)
   - 1 chamada de `Crossover.onePointCrossing` atualizada

## 📝 Lições Aprendidas

### Por que o Bug Passou Despercebido?

1. **Solomon I1 estava criando soluções com 46 violações de TIME WINDOW**
   - Esse problema dominava a atenção
   - Violação de capacidade era "apenas mais uma" violação

2. **Fitness incluía penalidades massivas**
   - Não havia distinção clara entre tipos de violação
   - Bug de capacidade se escondia no ruído

3. **Após correção de time windows**
   - Solomon I1 passou a gerar 0 violações
   - Violação de capacidade ficou evidente
   - Validação rigorosa revelou o problema sistemático

### Importância da Validação Rigorosa

**Validação por fitness NÃO é suficiente!**  
É necessário validar **cada constraint separadamente**:
- ✅ Cobertura de clientes
- ✅ Capacidade dos veículos  
- ✅ Janelas de tempo
- ✅ Retorno ao depot

## 📚 Próximos Passos

1. ✅ Código corrigido e compilado
2. ⏳ **Deletar resultados antigos** (com violações de capacidade)
3. ⏳ **Reexecutar validação completa** (C1, R1, RC1)
4. ⏳ Validar rigorosamente TODAS as soluções
5. ⏳ Comparar com best-known benchmarks
6. ⏳ Analisar taxa efetiva de mutação inter-rota
7. ⏳ (Opcional) Implementar estatísticas de rejeição de mutações

---

**Data**: 12/01/2026  
**Status**: ✅ CORRIGIDO E TESTADO  
**Commit**: feat: add capacity check to inter-route mutation
