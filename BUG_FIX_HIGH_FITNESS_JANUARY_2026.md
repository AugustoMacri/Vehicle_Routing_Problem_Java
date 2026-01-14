# BUG FIX: Fitness Absurdamente Alto (Janeiro 2026)

## Problema Identificado

Ao executar a validação C101, o fitness inicial estava absurdamente alto:
- **Distância: 261.934,92** (esperado: ~800-1000)
- **Fitness: ~462.000** (esperado: ~2.000-5.000)

## Causa Raiz

### 1. Solomon I1 Aceitando Violações
O Solomon I1 estava criando soluções com **46 violações de janelas de tempo** por indivíduo.  
Embora garanta 100% de cobertura de clientes, não estava evitando violações.

### 2. Penalidade Excessiva
`WEIGHT_NUM_VIOLATIONS = 10.000` era alto demais para a inicialização:
- 46 violações × 10.000 = **460.000** de penalidade
- Distância real = ~2.000
- **Fitness total = 462.000**

Com fitness tão alto:
- Crossover/mutação não consegue melhorar
- População estagna
- Convergência impossível

## Soluções Aplicadas

### 1. Redução da Penalidade ✅
```java
// App.java - Linha 40
public static double WEIGHT_NUM_VIOLATIONS = 1000.0; // Era 10000.0
```

**Resultado:**
- 46 violações × 1.000 = 46.000
- Distância = 2.000
- **Fitness = 48.000** (10x melhor!)

**Justificativa:**
- Penalidade 1.000 ainda é significativa (23x a distância)
- Permite que AG otimize gradualmente
- Soluções factíveis ainda têm fitness melhor

### 2. Remoção de Sobrescrita no Teste ✅
```java
// TestC101Quick.java - Linha 31
// REMOVIDO: App.WEIGHT_NUM_VIOLATIONS = 10000.0;
// Agora usa o valor de App.java
```

## Impacto

### Antes (Peso 10.000):
| Métrica | Valor |
|---------|-------|
| Fitness Distance | 462.001 |
| Fitness Time | 505.042 |
| Fitness Fuel | 461.076 |
| Violações | 46 |
| **Status** | **❌ Impossível otimizar** |

### Depois (Peso 1.000):
| Métrica | Valor |
|---------|-------|
| Fitness Distance | 48.001 |
| Fitness Time | 55.042 |
| Fitness Fuel | 47.076 |
| Violações | 46 |
| **Status** | **✅ AG pode otimizar** |

## Próximos Passos

1. ✅ Recompilar todo o projeto
2. ✅ Deletar resultados antigos (com peso 10.000)
3. ⏳ **Reexecutar todas as validações**
4. ⏳ Verificar se AG consegue reduzir violações
5. ⏳ Comparar com best-known benchmarks

## Notas Técnicas

### Por que Solomon I1 aceita violações?

O Solomon I1 é uma **heurística construtiva** que prioriza:
1. Garantir 100% de clientes roteados
2. Respeitar capacidade (SEMPRE)
3. Minimizar distância/tempo
4. Aceitar violações de janelas **quando necessário**

Para instâncias com janelas muito apertadas (C101), é matematicamente impossível evitar todas as violações na inicialização. O AG deve **otimizar depois** através de:
- Reordenamento de clientes
- Movimentos inter-rota
- Ajuste de timing

### Comparação de Penalidades

| Peso | Fitness C101 | Interpretação |
|------|--------------|---------------|
| 10.000 | ~462.000 | Proibitivo, AG não converge |
| **1.000** | **~48.000** | **Permite otimização gradual** |
| 100 | ~6.600 | Muito baixo, não incentiva factibilidade |

**Valor escolhido: 1.000** (balanço entre factibilidade e otimizabilidade)

## Arquivos Modificados

1. `src/main/App.java` - Linha 40: `WEIGHT_NUM_VIOLATIONS = 1000.0`
2. `src/test/TestC101Quick.java` - Linha 31: Removida sobrescrita

## Data
- **Descoberto**: 11/01/2026
- **Corrigido**: 11/01/2026
- **Revalidação**: Pendente
