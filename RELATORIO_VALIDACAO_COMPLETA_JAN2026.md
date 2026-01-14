# Relatório de Validação - Instâncias Solomon (Janeiro 2026)

**Data:** 13 de Janeiro de 2026  
**Algoritmo:** AEMMT (Algoritmo Evolutivo Multi-objetivo com Multi-Treino)  
**Parâmetros:**
- Gerações: 5.000
- Taxa de Mutação Inter-rota: 0.6
- Peso de Violações: 1.000
- Velocidade do Veículo: 1 (padrão Solomon)

---

## ✅ RESULTADO GERAL: 100% DE SOLUÇÕES VÁLIDAS

**Total de instâncias testadas:** 26 (C1: 9, R1: 9, RC1: 8)  
**Total de execuções:** 260 (10 execuções por instância)  
**Soluções válidas:** 260/260 (100%)

Todas as soluções respeitam as restrições de:
- ✅ **Capacidade dos veículos** (limite: 200)
- ✅ **Janelas de tempo** (hard constraints)
- ✅ **Cobertura completa** (todos os clientes atendidos exatamente uma vez)

---

## 📊 Análise por Classe de Instâncias

### Classe C1 (Clustered Customers - Janelas Curtas)

| Instância | Válidas | Melhor | Média | Veículos | Best-Known | Gap (%) |
|-----------|---------|--------|-------|----------|------------|---------|
| C101      | 10/10   | 1260.16 | 1264.15 | 13 | 828.94 (10v) | 52.02% |
| C102      | 10/10   | 1298.93 | 1316.40 | 11 | 828.94 (10v) | 56.70% |
| C103      | 10/10   | 1401.71 | 1420.55 | 12 | 828.06 (10v) | 69.28% |
| C104      | 10/10   | 1236.74 | 1253.93 | 10 | 824.78 (10v) | 49.95% |
| C105      | 10/10   | 1372.32 | 1386.58 | 12 | 828.94 (10v) | 65.55% |
| C106      | 10/10   | 1328.70 | 1361.80 | 11 | 828.94 (10v) | 60.29% |
| C107      | 10/10   | 1306.28 | 1381.20 | 13 | 828.94 (10v) | 57.58% |
| C108      | 10/10   | 1268.72 | 1320.09 | 12 | 828.94 (10v) | 53.05% |
| C109      | 10/10   | 1264.51 | 1292.53 | 11 | 828.94 (10v) | 52.55% |

**Gap Médio C1: 57.44%**

### Classe R1 (Random Customers - Janelas Curtas)

| Instância | Válidas | Melhor | Média | Veículos | Best-Known | Gap (%) |
|-----------|---------|--------|-------|----------|------------|---------|
| R101      | 10/10   | 1944.56 | 1959.05 | 22 | 1650.80 (19v) | 17.80% |
| R102      | 10/10   | 1862.84 | 1883.15 | 20 | 1486.12 (17v) | 25.35% |
| R103      | 10/10   | 1613.87 | 1613.87 | 17 | 1292.68 (13v) | 24.85% |
| R104      | 10/10   | 1296.73 | 1317.62 | 12 | 1007.31 (9v) | 28.73% |
| R105      | 10/10   | 1706.78 | 1728.57 | 17 | 1377.11 (14v) | 23.94% |
| R106      | 10/10   | 1573.49 | 1598.73 | 16 | 1252.03 (12v) | 25.68% |
| R107      | 10/10   | 1400.69 | 1412.42 | 13 | 1104.66 (10v) | 26.80% |
| R108      | 10/10   | 1238.95 | 1257.27 | 12 | 960.88 (9v) | 28.94% |
| R109      | 10/10   | 1610.99 | 1637.26 | 15 | 1194.73 (11v) | 34.84% |

**Gap Médio R1: 27.33%**

### Classe RC1 (Random-Clustered - Janelas Curtas)

| Instância | Válidas | Melhor | Média | Veículos | Best-Known | Gap (%) |
|-----------|---------|--------|-------|----------|------------|---------|
| RC101     | 10/10   | 2192.90 | 2236.98 | 20 | 1696.95 (14v) | 29.23% |
| RC102     | 10/10   | 1888.57 | 1904.30 | 16 | 1554.75 (12v) | 21.47% |
| RC103     | 10/10   | 1628.71 | 1667.19 | 14 | 1261.67 (11v) | 29.09% |
| RC104     | 10/10   | 1446.21 | 1474.08 | 12 | 1135.48 (10v) | 27.37% |
| RC105     | 10/10   | 1875.01 | 1911.07 | 20 | 1629.44 (13v) | 15.07% |
| RC106     | 10/10   | 1836.61 | 1879.26 | 16 | 1424.73 (11v) | 28.91% |
| RC107     | 10/10   | 1744.98 | 1782.04 | 14 | 1230.48 (11v) | 41.81% |
| RC108     | 10/10   | 1536.05 | 1548.49 | 13 | 1139.82 (10v) | 34.76% |

**Gap Médio RC1: 28.46%**

---

## 🔍 Análise Comparativa

### Performance por Classe

1. **Classe R1** (Random): Melhor performance geral
   - Gap médio: **27.33%**
   - Mais consistente (todas < 35%)
   - Melhor resultado: R101 com 17.80% de gap

2. **Classe RC1** (Random-Clustered): Performance intermediária
   - Gap médio: **28.46%**
   - Destaque: RC105 com apenas 15.07% de gap
   - Variação moderada (15% a 42%)

3. **Classe C1** (Clustered): Maior desafio
   - Gap médio: **57.44%**
   - Janelas de tempo muito restritas causam maior dificuldade
   - Melhor resultado: C104 com 49.95% de gap

### Estabilidade do Algoritmo

**Desvio padrão médio das distâncias:**
- C101: 4.00 (muito estável)
- R101: 10.75 (estável)
- RC101: 31.98 (variação moderada)

**Destaque:** R103 teve todas as 10 execuções com a mesma distância (1613.87), mostrando extrema estabilidade nessa instância.

---

## 🎯 Correções Implementadas

As soluções válidas foram alcançadas após implementar três correções críticas:

### 1. Velocidade do Veículo (Dezembro 2025)
- **Problema:** Velocidade incorreta (50) causava tempos de viagem irreais
- **Solução:** Ajustada para 1 (padrão Solomon)

### 2. Solomon I1 Conservador (Dezembro 2025)
- **Problema:** Inicialização aceitava violações de janelas de tempo
- **Solução:** Implementado modo conservador que força novo veículo ao invés de violar

### 3. Validação de Capacidade na Mutação (Janeiro 2026)
- **Problema:** Mutação inter-rota trocava clientes sem verificar capacidade
- **Solução:** Adicionada validação que rejeita swaps que violam capacidade

**Resultado:** Todas as 260 soluções respeitam hard constraints (capacidade + janelas de tempo).

---

## 📈 Comparação com Best-Known

### Gap Relativo ao Best-Known

- **Menor gap:** RC105 (15.07%)
- **Maior gap:** C103 (69.28%)
- **Gap médio geral:** 37.74%

### Veículos Utilizados

Em geral, o algoritmo utiliza 1-3 veículos a mais que o best-known devido à estratégia conservadora do Solomon I1, que prioriza viabilidade sobre minimização de veículos.

**Exemplos:**
- C104: 10 veículos (igual ao best-known) ✅
- R101: 22 veículos vs 19 best-known (+3)
- RC101: 20 veículos vs 14 best-known (+6)

---

## ✅ Conclusões

1. **Viabilidade:** 100% das soluções são válidas (capacidade + janelas de tempo)
2. **Estabilidade:** Algoritmo robusto com baixa variação entre execuções
3. **Performance:** Melhores resultados em instâncias R1 e RC1 (~17-35% gap)
4. **Trade-off:** Estratégia conservadora garante viabilidade mas pode usar mais veículos

### Pontos Fortes
- Robustez: Zero violações de restrições
- Consistência: Baixa variação entre execuções
- Classe R1: Gaps competitivos (17-35%)

### Oportunidades de Melhoria
- Classe C1: Gaps elevados (50-69%) indicam desafio com janelas muito restritas
- Minimização de veículos: Usar menos veículos mantendo viabilidade
- Otimização local: Melhorar busca em espaços de solução restritos

---

## 🔄 Próximos Passos Sugeridos

1. **Ajuste de Parâmetros:**
   - Testar diferentes taxas de mutação inter-rota
   - Avaliar impacto de mais gerações (7.500 ou 10.000)

2. **Operadores Adicionais:**
   - Implementar busca local específica para C1
   - Adicionar operadores de minimização de veículos

3. **Benchmarking Estendido:**
   - Testar classes C2, R2, RC2 (janelas mais amplas)
   - Comparar com outros algoritmos da literatura

4. **Análise de Convergência:**
   - Plotar evolução das distâncias ao longo das gerações
   - Identificar se 5.000 gerações são suficientes

---

**Gerado automaticamente em:** 13 de Janeiro de 2026  
**Validação rigorosa executada para todas as 260 soluções**
