# Vehicle Routing Problem - Java (VRP)

Este projeto implementa uma solução baseada em **Algoritmo Evolutivo Multi-Objetivo com Multi-Treino (AEMMT)** para o **Vehicle Routing Problem with Time Windows (VRPTW)**, um problema clássico de otimização combinatória onde o objetivo é determinar as melhores rotas para uma frota de veículos atender um conjunto de clientes, minimizando custos (distância, tempo, combustível) enquanto respeita janelas de tempo e capacidades dos veículos.

## 📚 Documentação Completa

- **[CONFIGURACAO_ATUAL.md](CONFIGURACAO_ATUAL.md)**: Parâmetros atuais, operadores genéticos, e estrutura do código
- **[HISTORICO_MODIFICACOES_E_VALIDACAO.md](HISTORICO_MODIFICACOES_E_VALIDACAO.md)**: Histórico de bugs corrigidos, funcionamento detalhado do algoritmo, e processo de validação

**👉 IMPORTANTE:** Leia os documentos acima para entender completamente o projeto e suas correções críticas!

## 🎯 Características Principais

### Validação Rigorosa ✅
- **260/260 soluções válidas** (100% de sucesso)
- **0 violações** de capacidade
- **0 violações** de janelas de tempo
- **100% de clientes** atendidos em todas as execuções

### Algoritmo Genético Multi-Objetivo
- **4 Subpopulações Especializadas:** Distância, Tempo, Combustível, Ponderado
- **Inicialização Solomon I1 (Conservador):** Garante soluções 100% factíveis desde o início
- **Mutação Combinada:** Intra-rota + Inter-rota com validação de capacidade
- **Elitismo:** 10% dos melhores indivíduos preservados

### Parâmetros Atuais
- População: 900 indivíduos (300 por subpopulação)
- Gerações: 5.000
- Mutação: 1% (sempre com mutação inter-rota quando ocorre)
- Crossover: 100%
- Velocidade do Veículo: 1 (padrão Solomon)

## � Como Executar

### Compilação
```bash
javac -d bin -sourcepath src src/main/App.java
```

### Execução Interativa
```bash
java -cp bin main.App
# Escolha opção 1 (Multi-objetivo)
# Escolha instância (ex: 1 para C101)
```

### Execução em Lote
```bash
# Executar 10x todas as instâncias C1
python3 scripts/run_validation_c1.py

# Executar 10x todas as instâncias R1
python3 scripts/run_validation_r1.py

# Executar 10x todas as instâncias RC1
python3 scripts/run_validation_rc1.py
```

### Validação de Resultados
```bash
python3 scripts/validate_solution_rigorous.py \
    src/instances/solomon/C101.txt \
    results_validation_C1/C101/evo_c101_exec01.txt
```

## 📊 Resultados Atuais (Janeiro 2026)

### Performance por Classe

| Classe | Instâncias | Gap Médio | Melhor Resultado |
|--------|-----------|-----------|------------------|
| C1 (Clustered) | 9 | 57.44% | C104: 49.95% |
| R1 (Random) | 9 | 27.33% | R101: 17.80% |
| RC1 (Random-Clustered) | 8 | 28.46% | RC105: 15.07% |

**Todas as 260 execuções produziram soluções 100% válidas! ✅**

## 🐛 Bugs Críticos Corrigidos

### 1. Perda de Clientes no Crossover (Dez 2025)
- **Problema:** `denormalizeRoute()` parava no primeiro zero, perdendo clientes
- **Solução:** Algoritmo de compactação completa

### 2. Velocidade do Veículo Incorreta (Jan 2026)
- **Problema:** `VEHICLE_SPEED = 50` causava cálculo de tempo errado (50x menor)
- **Solução:** `VEHICLE_SPEED = 1` (padrão Solomon)

### 3. Solomon I1 Não-Conservador (Jan 2026)
- **Problema:** Retornava inserções com violações de time window
- **Solução:** Retornar apenas `feasibleInsertions`

### 4. Mutação Sem Validação de Capacidade (Jan 2026)
- **Problema:** Mutação inter-rota trocava clientes sem verificar capacidade
- **Solução:** Validação de capacidade ANTES de aceitar troca

**Detalhes completos:** Ver [HISTORICO_MODIFICACOES_E_VALIDACAO.md](HISTORICO_MODIFICACOES_E_VALIDACAO.md)

## 📂 Estrutura do Projeto

```
src/
├── main/
│   └── App.java                      # Parâmetros e entrada
├── genetic/
│   ├── Population.java               # 4 subpopulações
│   ├── Individual.java               # Representação de soluções
│   ├── Crossover.java                # Crossover (corrigido)
│   ├── Mutation.java                 # Mutação (com validação)
│   ├── SolomonInsertion.java         # Inicialização conservadora
│   └── fitness/                      # Calculadoras de fitness
├── vrp/
│   ├── Client.java                   # Modelo de cliente
│   └── Solution.java                 # Modelo de solução
└── instances/solomon/                # Instâncias benchmark

scripts/
├── run_validation_c1.py             # Execução automática C1
├── run_validation_r1.py             # Execução automática R1
├── run_validation_rc1.py            # Execução automática RC1
├── validate_solution_rigorous.py    # Validação rigorosa
└── plot_route_maps.py               # Visualização
```

## 🔧 Tecnologias

- **Linguagem:** Java
- **Benchmark:** Solomon VRPTW Instances
- **Validação:** Python 3
- **Visualização:** Matplotlib

## 📖 Referências

- Solomon, M. M. (1987). "Algorithms for the Vehicle Routing and Scheduling Problems with Time Window Constraints"
- Solomon Benchmark: https://www.sintef.no/projectweb/top/vrptw/solomon-benchmark/

---

**Desenvolvido com validação rigorosa: 260/260 soluções válidas (100%)** ✅
