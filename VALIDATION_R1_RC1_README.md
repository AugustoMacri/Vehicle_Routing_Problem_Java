# Scripts de Validação para Instâncias R1 e RC1

## Visão Geral

Scripts criados para executar validações completas do algoritmo AEMMT nas instâncias R1 (R101-R109) e RC1 (RC101-RC108) do benchmark Solomon.

## Estrutura de Arquivos

```
scripts/
├── run_validation_c1.py      # Validação instâncias C1 (C101-C109) ✓
├── run_validation_r1.py       # Validação instâncias R1 (R101-R109) ✓ NOVO
├── run_validation_rc1.py      # Validação instâncias RC1 (RC101-RC108) ✓ NOVO
├── generate_maps_r1.sh        # Gera mapas R1 ✓ NOVO
└── generate_maps_rc1.sh       # Gera mapas RC1 ✓ NOVO
```

---

## 1. Validação Instâncias R1

### Executar Validação

```bash
python3 scripts/run_validation_r1.py
```

### Mapeamento de Instâncias

| Instância | Número | Clientes | Características |
|-----------|--------|----------|-----------------|
| R101      | 18     | 100      | Dispersão geográfica aleatória |
| R102      | 19     | 100      | Dispersão geográfica aleatória |
| R103      | 20     | 100      | Dispersão geográfica aleatória |
| R104      | 21     | 100      | Dispersão geográfica aleatória |
| R105      | 22     | 100      | Dispersão geográfica aleatória |
| R106      | 23     | 100      | Dispersão geográfica aleatória |
| R107      | 24     | 100      | Dispersão geográfica aleatória |
| R108      | 25     | 100      | Dispersão geográfica aleatória |
| R109      | 26     | 100      | Dispersão geográfica aleatória |

### Processo de Execução

1. **Compilação**: Compila o projeto Java automaticamente
2. **Execução**: 10 execuções para cada instância (R101-R109)
3. **Extração**: Coleta a melhor distância de cada execução
4. **Armazenamento**: Salva arquivos em `results_validation_R1/`
5. **Estatísticas**: Gera resumo com média, desvio padrão, melhor e pior

### Estrutura de Saída

```
results_validation_R1/
├── R101/
│   ├── resultados_aemmt_YYYYMMDD_HHMMSS.txt
│   ├── evo_r101_exec01.txt
│   ├── evo_r101_exec02.txt
│   ├── ...
│   ├── stats_r101_exec01.txt
│   └── ...
├── R102/
├── R103/
├── R104/
├── R105/
├── R106/
├── R107/
├── R108/
└── R109/
```

### Gerar Mapas R1

```bash
bash scripts/generate_maps_r1.sh
```

Cria mapas visuais das rotas em `results_validation_R1/[INSTANCE]/maps/`

---

## 2. Validação Instâncias RC1

### Executar Validação

```bash
python3 scripts/run_validation_rc1.py
```

### Mapeamento de Instâncias

| Instância | Número | Clientes | Características |
|-----------|--------|----------|-----------------|
| RC101     | 41     | 100      | Misto: clustering + dispersão |
| RC102     | 42     | 100      | Misto: clustering + dispersão |
| RC103     | 43     | 100      | Misto: clustering + dispersão |
| RC104     | 44     | 100      | Misto: clustering + dispersão |
| RC105     | 45     | 100      | Misto: clustering + dispersão |
| RC106     | 46     | 100      | Misto: clustering + dispersão |
| RC107     | 47     | 100      | Misto: clustering + dispersão |
| RC108     | 48     | 100      | Misto: clustering + dispersão |

### Processo de Execução

1. **Compilação**: Compila o projeto Java automaticamente
2. **Execução**: 10 execuções para cada instância (RC101-RC108)
3. **Extração**: Coleta a melhor distância de cada execução
4. **Armazenamento**: Salva arquivos em `results_validation_RC1/`
5. **Estatísticas**: Gera resumo com média, desvio padrão, melhor e pior

### Estrutura de Saída

```
results_validation_RC1/
├── RC101/
│   ├── resultados_aemmt_YYYYMMDD_HHMMSS.txt
│   ├── evo_rc101_exec01.txt
│   ├── evo_rc101_exec02.txt
│   ├── ...
│   ├── stats_rc101_exec01.txt
│   └── ...
├── RC102/
├── RC103/
├── RC104/
├── RC105/
├── RC106/
├── RC107/
└── RC108/
```

### Gerar Mapas RC1

```bash
bash scripts/generate_maps_rc1.sh
```

Cria mapas visuais das rotas em `results_validation_RC1/[INSTANCE]/maps/`

---

## Formato dos Arquivos de Resultados

### resultados_aemmt_*.txt

```
Resultados AEMMT - R101
============================================================

Total de execuções: 10

Execução | Melhor Distância Encontrada
----------------------------------------
       1 |   1234.56
       2 |   1245.67
       ...
      10 |   1256.78

Estatísticas:
----------------------------------------
Melhor distância encontrada: 1234.56
Pior distância encontrada: 1267.89
Distância média geral: 1250.45
Desvio padrão das distâncias: 12.34
```

---

## Tempo de Execução Estimado

| Script | Instâncias | Execuções/Inst. | Tempo/Exec. | Total Estimado |
|--------|------------|-----------------|-------------|----------------|
| R1     | 9          | 10              | ~60s        | ~9 horas       |
| RC1    | 8          | 10              | ~60s        | ~8 horas       |

**Nota**: Os tempos são aproximados e variam conforme o hardware.

---

## Comparação de Tipos de Instâncias

| Tipo | Sigla | Características                        | Dificuldade |
|------|-------|----------------------------------------|-------------|
| C1   | C101-C109 | Clientes agrupados (clustering)   | Fácil       |
| R1   | R101-R109 | Clientes dispersos aleatoriamente  | Média       |
| RC1  | RC101-RC108 | Misto (clusters + dispersão)     | Difícil     |

---

## Solução de Problemas

### Erro: "Diretório de resultados não encontrado"
```bash
# Para mapas R1
python3 scripts/run_validation_r1.py

# Para mapas RC1
python3 scripts/run_validation_rc1.py
```

### Erro de Compilação
```bash
# Verificar estrutura de diretórios
ls -la src/main/App.java
ls -la bin/

# Limpar e recompilar
rm -rf bin/*
python3 scripts/run_validation_r1.py  # Compila automaticamente
```

### Script de Mapa Não Funciona
```bash
# Verificar se plot_map.py existe
ls -la scripts/plot_map.py

# Dar permissão de execução
chmod +x scripts/plot_map.py
```

---

## Comandos Rápidos

### Executar tudo para R1
```bash
# 1. Validação (9 instâncias × 10 execuções = 90 execuções)
python3 scripts/run_validation_r1.py

# 2. Gerar mapas
bash scripts/generate_maps_r1.sh
```

### Executar tudo para RC1
```bash
# 1. Validação (8 instâncias × 10 execuções = 80 execuções)
python3 scripts/run_validation_rc1.py

# 2. Gerar mapas
bash scripts/generate_maps_rc1.sh
```

### Ver resultados
```bash
# R1
cat results_validation_R1/R101/resultados_aemmt_*.txt

# RC1
cat results_validation_RC1/RC101/resultados_aemmt_*.txt
```

---

## Notas Importantes

1. **Execução em Background**: Para execuções longas, use `nohup`:
   ```bash
   nohup python3 scripts/run_validation_r1.py > validation_r1.log 2>&1 &
   ```

2. **Monitoramento**: Acompanhe o progresso:
   ```bash
   tail -f validation_r1.log
   ```

3. **Interrupção**: Para parar a execução use `Ctrl+C` (salva resultados até então)

4. **Espaço em Disco**: Cada conjunto completo ocupa ~50-100MB

---

## Integração com NSGA-III

Para comparar com NSGA-III:
```bash
# Executar NSGA-III para R1/RC1
cd VRP_NSGA_TCC
bash run_validation_nsga3_r1.sh  # (se existir)

# Comparar resultados
python3 scripts/compare_aemmt_nsga3.py --instances R1
```

---

**Última atualização**: 04/01/2026
**Autor**: Sistema de Validação AEMMT
