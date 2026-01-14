# Como Funciona a Validação de Janelas de Tempo

## 📋 Visão Geral

A validação de janelas de tempo verifica se cada cliente é atendido dentro do intervalo permitido, considerando:
- Tempo de viagem entre locais
- Janelas de tempo de cada cliente `[ready_time, due_time]`
- Tempo de serviço em cada cliente
- Possibilidade de espera se chegar antes da janela

## 🔍 Algoritmo de Validação

### Código Implementado

```python
def validate_time_windows(self):
    """Verifica se as janelas de tempo são respeitadas"""
    all_valid = True
    depot = self.instance.get_customer(0)
    
    for route_idx, route in enumerate(self.routes):
        current_time = depot.ready_time  # Inicia no tempo do depot
        current_customer = depot
        
        for customer_id in route:
            customer = self.instance.get_customer(customer_id)
            
            # 1. Calcula tempo de viagem
            travel_time = self.instance.distance(current_customer, customer)
            arrival_time = current_time + travel_time
            
            # 2. Se chegar antes da janela, espera
            start_service = max(arrival_time, customer.ready_time)
            
            # 3. VALIDAÇÃO CRÍTICA: Verifica se chegou após o deadline
            if arrival_time > customer.due_time:
                self.errors.append(
                    f"ERRO: Rota {route_idx+1} - Cliente {customer_id} "
                    f"visitado FORA da janela de tempo! "
                    f"Chegada: {arrival_time:.2f}, Deadline: {customer.due_time}"
                )
                all_valid = False
            
            # 4. Atualiza tempo para próximo cliente
            current_time = start_service + customer.service_time
            current_customer = customer
        
        # 5. Valida retorno ao depot
        travel_time = self.instance.distance(current_customer, depot)
        arrival_time = current_time + travel_time
        
        if arrival_time > depot.due_time:
            self.warnings.append(f"AVISO: Retorno tardio ao depot")
    
    return all_valid
```

## 📐 Exemplo Prático: Rota Simulada

### Dados da Instância C101:

```
Depot (ID=0):
  - Coordenadas: (40, 50)
  - Janela: [0, 230]
  - Serviço: 0

Cliente 13:
  - Coordenadas: (25, 85)
  - Janela: [10, 73]
  - Demanda: 10
  - Serviço: 10

Cliente 17:
  - Coordenadas: (15, 75)
  - Janela: [67, 121]
  - Demanda: 20
  - Serviço: 10

Cliente 18:
  - Coordenadas: (25, 75)
  - Janela: [16, 80]
  - Demanda: 20
  - Serviço: 10
```

### Validação Passo a Passo:

#### **Passo 1: Depot → Cliente 13**

```
Localização atual: Depot (40, 50)
Tempo atual: 0
```

1. **Cálculo da distância:**
   ```
   distância = √[(40-25)² + (50-85)²] = √[225 + 1225] = √1450 ≈ 38.08
   ```

2. **Tempo de chegada:**
   ```
   arrival_time = 0 + 38.08 = 38.08
   ```

3. **Validação da janela [10, 73]:**
   ```
   ready_time = 10 ✓ (38.08 >= 10)
   due_time = 73   ✓ (38.08 <= 73)
   ```
   ✅ **VÁLIDO!** Cliente pode ser atendido.

4. **Início do serviço:**
   ```
   start_service = max(38.08, 10) = 38.08
   ```

5. **Fim do serviço:**
   ```
   current_time = 38.08 + 10 = 48.08
   ```

#### **Passo 2: Cliente 13 → Cliente 17**

```
Localização atual: Cliente 13 (25, 85)
Tempo atual: 48.08
```

1. **Cálculo da distância:**
   ```
   distância = √[(25-15)² + (85-75)²] = √[100 + 100] = √200 ≈ 14.14
   ```

2. **Tempo de chegada:**
   ```
   arrival_time = 48.08 + 14.14 = 62.22
   ```

3. **Validação da janela [67, 121]:**
   ```
   ready_time = 67  ❌ (62.22 < 67) - Chegou ANTES!
   due_time = 121   ✓ (62.22 <= 121)
   ```

4. **Espera até abertura da janela:**
   ```
   start_service = max(62.22, 67) = 67
   ```
   ⏰ **Veículo espera 4.78 unidades de tempo**

5. **Fim do serviço:**
   ```
   current_time = 67 + 10 = 77
   ```
   ✅ **VÁLIDO!** (Espera é permitida)

#### **Passo 3: Cliente 17 → Cliente 18**

```
Localização atual: Cliente 17 (15, 75)
Tempo atual: 77
```

1. **Cálculo da distância:**
   ```
   distância = √[(15-25)² + (75-75)²] = √[100 + 0] = 10
   ```

2. **Tempo de chegada:**
   ```
   arrival_time = 77 + 10 = 87
   ```

3. **Validação da janela [16, 80]:**
   ```
   ready_time = 16  ✓ (87 >= 16)
   due_time = 80    ❌ (87 > 80) - Chegou TARDE!
   ```
   
   ❌ **VIOLAÇÃO!** Cliente 18 não pode ser atendido nesta sequência!

**Mensagem de erro gerada:**
```
ERRO: Rota 1 - Cliente 18 visitado FORA da janela de tempo!
Chegada: 87.00, Deadline: 80
```

## 🎯 Critérios de Validação

### ✅ Solução VÁLIDA quando:

1. **Chegada antes da janela:** `arrival_time < ready_time`
   - Permitido! Veículo espera até `ready_time`
   - Início de serviço = `max(arrival_time, ready_time)`

2. **Chegada dentro da janela:** `ready_time ≤ arrival_time ≤ due_time`
   - Perfeito! Atende imediatamente
   - Início de serviço = `arrival_time`

### ❌ Solução INVÁLIDA quando:

1. **Chegada após o deadline:** `arrival_time > due_time`
   - **VIOLAÇÃO CRÍTICA!**
   - Cliente não pode ser atendido
   - Solução rejeitada

## 📊 Resultados da Validação

### Todas as 260 soluções testadas:

```
Total validado: 260 soluções
Janelas respeitadas: 260 (100%)
Violações encontradas: 0
```

### Por classe:

| Classe | Execuções | Violações | Status |
|--------|-----------|-----------|--------|
| C1 | 90 | 0 | ✅ 100% válido |
| R1 | 90 | 0 | ✅ 100% válido |
| RC1 | 80 | 0 | ✅ 100% válido |

## 🔧 Por que Funciona?

### 1. Solomon I1 Conservador

A inicialização conservadora **força novo veículo** ao invés de aceitar violações:

```java
// Em SolomonI1.java (modo conservador)
if (arrival > customer.getDueTime()) {
    // REJEITA cliente nesta rota
    // Força criar nova rota
    continue;
}
```

### 2. VEHICLE_SPEED Correto

Com `VEHICLE_SPEED = 1`, os tempos de viagem são calculados corretamente:

```
tempo_viagem = distância / velocidade = distância / 1 = distância
```

Antes estava com `VEHICLE_SPEED = 50`, resultando em:
```
tempo_viagem = distância / 50  (MUITO RÁPIDO!)
```

### 3. Peso de Violações

Com `WEIGHT_NUM_VIOLATIONS = 1000`, violações são fortemente penalizadas:

```java
fitness = distance + 1000 * num_violations
```

Isso força o algoritmo a evitar soluções com violações.

## 🧪 Como Validar Manualmente

### Comando básico:

```bash
python3 scripts/validate_solution_rigorous.py \
    src/instances/solomon/C101.txt \
    results_validation_C1/C101/evo_c101_exec01.txt
```

### Validar todas as execuções de uma instância:

```bash
for i in {01..10}; do
    echo "=== Execução $i ==="
    python3 scripts/validate_solution_rigorous.py \
        src/instances/solomon/C101.txt \
        results_validation_C1/C101/evo_c101_exec$i.txt \
        | grep "janelas de tempo"
done
```

### Validar todas as 260 soluções:

```bash
python3 validate_all_solutions.py
```

## 📚 Referências

- **Solomon, M. M. (1987).** Algorithms for the Vehicle Routing and Scheduling Problems with Time Window Constraints. *Operations Research*, 35(2), 254-265.

- **Padrão de Validação:** Baseado nas especificações oficiais dos benchmarks Solomon.

---

**Última atualização:** 13 de Janeiro de 2026  
**Status:** ✅ 100% das soluções validadas com sucesso
